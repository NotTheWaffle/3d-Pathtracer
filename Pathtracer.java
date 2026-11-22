import Game.Game;
import Game.Input;
import Math.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

/**
 * An implemenation of game which utilizes virtual threads to pathtrace a provided Environment.
 */
public class Pathtracer extends Game{


	// used for modifying the scene
	private static final float LATERAL_SPEED = 1.25f; // units per sec
	private static final float ROTATIONAL_SPEED = FloatMath.PI/2; // rad per sec
	private int selectedObjectIndex;

	// these represent the entire scene
	private Viewport camera;
	private final Environment env;

	// used for rendering scenes
	private final static int MAX_THREADS = 16;
	private boolean pathtrace;	// whether the current render style is rasterization or pathtracing
	private final Pixel[][] pixelBuffer;
	private volatile int activeThreads;
	private synchronized void inc(){activeThreads++;}
	private synchronized void dec(){activeThreads--;}

	// statistics
	private volatile int modCount;
	private List<Long> timeSamples;

	public Pathtracer(Viewport camera, Environment env){
		super(camera.screenWidth, camera.screenHeight);

		nextFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		this.env = env;
		this.camera = camera;

		this.pathtrace = false;
		this.activeThreads = 0;
		this.modCount = 0;
		this.timeSamples = Collections.synchronizedList(new ArrayList<>());

		pixelBuffer = new Pixel[height][width];
		for (Pixel[] row : pixelBuffer) {
			for (int x = 0; x < row.length; x++) {
				row[x] = new Pixel();
			}
		}
	}

	public void cli(java.io.InputStream input){
		java.util.Scanner scan = new java.util.Scanner(input);
		System.out.println("Pathtracer CLI:");
		outer:
		while (true){
			String line = scan.nextLine();
			String[] args = line.split(" ");
			if (args.length == 0) continue;
			switch (args[0]){
				case "exit" -> {
					break outer;
				}
				case "view" -> {
					start();
				}
				case "pano" -> {
					int steps = 12;
					int samples = 32;
					if (args.length > 1) steps = Integer.parseInt(args[1]);
					if (args.length > 2) samples = Integer.parseInt(args[2]);
					pano(steps, samples);
				}
				case "reset" -> {
					this.camera = new Viewport(camera.fov, camera.focusDistance, camera.focus, camera.screenWidth, camera.screenHeight);
				}
				case "env" -> {

				}
				case "help" -> {
					System.out.print(
						"""
						exit \t exits the program
						view \t opens the interactive pathtracer
						pano \t renders a specified amount of frames rotating in yaw
						reset \t resets the camera position to the origin and its angle to default
						env \t modify the environment of the scene in some way
						"""
					);
				}
				default -> {
					System.out.println("Unrecognized command\n \"help\" for help");
				}
			}
		}
		scan.close();
	}

	@Override
	public String name(){
		return "Pathtraced 3d";
	}

	@Override
	public void tick(double dt){
		float relativeLateralSpeed = (float) (LATERAL_SPEED * dt);
		float relativeRotationalSpeed = (float) (ROTATIONAL_SPEED * dt);

		for (char a = '0'; a <= '9'; a++){
			if (input.keys[a]) selectedObjectIndex = a-'0';
		}
		Transform transform;
		if (selectedObjectIndex <= 0 || selectedObjectIndex > env.physicalObjects.size()){
			transform = camera;
		} else {
			transform = env.physicalObjects.get(selectedObjectIndex-1).transform;
		}

		if (input.keys['W']) 				{resetPixelBuffer(); transform.move(0, 0, relativeLateralSpeed);}
		if (input.keys['A']) 				{resetPixelBuffer(); transform.move(-relativeLateralSpeed, 0, 0);}
		if (input.keys['S']) 				{resetPixelBuffer(); transform.move(0, 0, -relativeLateralSpeed);}
		if (input.keys['D']) 				{resetPixelBuffer(); transform.move(relativeLateralSpeed, 0, 0);}
		if (input.keys[' ']) 				{resetPixelBuffer(); transform.move(0, relativeLateralSpeed, 0);}
		if (input.keys[Input.SHIFT]) 		{resetPixelBuffer(); transform.move(0, -relativeLateralSpeed, 0);}

		if (input.keys[Input.UP_ARROW]) 	{resetPixelBuffer(); transform.turnX( relativeRotationalSpeed);}
		if (input.keys[Input.DOWN_ARROW]) 	{resetPixelBuffer(); transform.turnX(-relativeRotationalSpeed);}
		if (input.keys[Input.LEFT_ARROW]) 	{resetPixelBuffer(); transform.turnY( relativeRotationalSpeed);}
		if (input.keys[Input.RIGHT_ARROW]) 	{resetPixelBuffer(); transform.turnY(-relativeRotationalSpeed);}
		if (input.keys['Q']) 				{resetPixelBuffer(); transform.turnZ(-relativeRotationalSpeed);}
		if (input.keys['E']) 				{resetPixelBuffer(); transform.turnZ( relativeRotationalSpeed);}


		if (input.keys['['] && !input.keys[']']) {
			if (pathtrace){
				resetPixelBuffer();
			} else {
				beginPathtracing(MAX_THREADS, 0);
			}
		}
		if (input.keys[']']) {
			stopPathtracing();
		}
	}

	public void pano(int steps, int sampleCount){
		stopPathtracing();
		Viewport backupCamera = camera;
		long start = System.nanoTime();
		for (int i = 0; i < steps; i++){
			float angle = FloatMath.TAU * i / steps;
			camera = new Viewport(.25f, 1f, 0, 512, 512);
			camera.rotateY(angle).move(0, 1, 2).rotateY(FloatMath.PI).turnX(-.52f);
			System.out.printf("rendering frame: %d (%d/%d)\n", i, i+1, steps);
			long duration = System.nanoTime() - start;
			if (i > 0) System.out.printf("%.1fs left\n", (duration * (steps-i)/(i))/1_000_000_000.0);
			renderPathtracedToFile("angle"+i, sampleCount);
		}
		camera = backupCamera;
		beginPathtracing(MAX_THREADS, 0);
	}

	/**
	 * Clears the pixel buffer and fills with blanks
	 */
	private void resetPixelBuffer(){
		if (!pathtrace) return;
		timeSamples.clear();
		modCount++;
		for (Pixel[] row : pixelBuffer) {
			for (Pixel element : row) {
				element.clear();
			}
		}
	}


	@Override
	public void generateFrame(){
		if (input.keys['K']){
			try {
				File outputfile = new File("saved.png");
				ImageIO.write(nextFrame, "png", outputfile);
			} catch (IOException e) {
				System.out.println("Failed to save screenshot");
			}
		}
		if (pathtrace){
			renderPathtraced();
		} else {
			renderRasterized();
		}

		if (timeSamples.isEmpty()){
			this.debug = "";
			return;
		}
		long time = 0;
		for (int i = 0; i < timeSamples.size(); i++){
			time += timeSamples.get(i);
		}
		time /= timeSamples.size();
		super.debug = String.format("frameTime: %4.1f, samples ~= %d", (time/1_000_000.0), pixelBuffer[0][0].getSamples());
	}

	/**
	 * Renders the scene using rasterization.
	 */
	private void renderRasterized(){
		WritableRaster raster = new BufferedImage(nextFrame.getWidth(), nextFrame.getHeight(), BufferedImage.TYPE_INT_RGB).getRaster();

		float[][] zBuffer = new float[width][height];
		for (int x = 0; x < width; x++) {
			Arrays.fill(zBuffer[x], Float.POSITIVE_INFINITY);
		}
		for (PhysicalObject object : env.physicalObjects){
			if (object == null) continue;
			if (object instanceof Mesh mesh){
				mesh.bvh.renderWireframe(raster, zBuffer, camera, object.transform, (int) (input.mouseWheel));
			}
			object.renderRasterized(raster, zBuffer, camera);
		}


		// do a fast arraycopy between the two rasters with system calls
		int[] myData = ((java.awt.image.DataBufferInt) raster.getDataBuffer()).getData();
		int[] screen = ((java.awt.image.DataBufferInt) nextFrame.getRaster().getDataBuffer()).getData();

		System.arraycopy(myData, 0, screen, 0, myData.length);
	}

	/**
	 * Renders the scene using pathtracing
	 */
	private void renderPathtraced(){
		renderPixelBufferToFrame(nextFrame);
	}

	/**
	 * Renders the scene with a specified amount of samples to a file and exports it to the named file
	 * @param filename
	 * @param sampleCount
	 */
	private void renderPathtracedToFile(String filename, int sampleCount){
		beginPathtracing(16, sampleCount);
		// wait until all threads start
		while (activeThreads == 0){
			Thread.onSpinWait();
		}
		while (activeThreads != 0){
			Thread.onSpinWait();
		}

		BufferedImage frame = new BufferedImage(camera.screenWidth, camera.screenHeight, BufferedImage.TYPE_INT_RGB);
		renderPixelBufferToFrame(frame);
		try {
			File outputfile = new File("Renders/"+filename+".png");
			ImageIO.write(frame, "png", outputfile);
			System.out.println("wrote "+"Renders/"+filename+".png");
		} catch (IOException exception){}
	}

	/**
	 * Renders the pixel buffer to a frame
	 * @param frame
	 * @return
	 */
	private BufferedImage renderPixelBufferToFrame(BufferedImage frame){
		WritableRaster raster = frame.getRaster();
		final int[] black = {0, 0, 0, 255};
		for (int y = 0; y < pixelBuffer.length; y++){
			for (int x = 0; x < pixelBuffer[y].length; x++){
				if (width <= 256 && pixelBuffer[y][x].getSamples() == 0){
					long start = System.nanoTime();
					// wait a few ms to get the data and give up if it takes too long
					while (pixelBuffer[y][x].getSamples() == 0 && System.nanoTime()-start < 1_000){
						Thread.onSpinWait();
					}
				}
				if (pixelBuffer[y][x].getSamples() == 0){
					raster.setPixel(x, y, black);
					continue;
				}
				raster.setPixel(x, y, pixelBuffer[y][x].getColor());
			}
		}
		return frame;
	}

	/**
	 * Creates and dispatches virtual threads to pathtrace the scene
	 * @param threadCount
	 */
	private void beginPathtracing(int threadCount, int sampleCount){
		stopPathtracing();
		Random globalRandom = new Random();

		// array of arraylists of pairs of integers, representing the random distribution of pixels among threads
		@SuppressWarnings("unchecked")
		List<Long>[] pointSets = (List<Long>[]) new List[threadCount];
		for (int i = 0; i < threadCount; i++){
			pointSets[i] = new ArrayList<>();
		}
		for (int x = 0; x < camera.screenWidth; x++){
			for (int y = 0; y < camera.screenHeight; y++){
				pointSets[globalRandom.nextInt(threadCount)].add(((long) y << 32) | x);
			}
		}

		List<Runnable> threads = new ArrayList<>();
		for (int i = 0; i < threadCount; i++){
			Collections.shuffle(pointSets[i]);
			final int iFinal = i;

			Runnable t = () -> pathtraceUntilInterrupt(iFinal, pointSets[iFinal], sampleCount);
			threads.add(t);
		}
		System.out.println("Starting "+ threads.size()+" virtual threads");
		pathtrace = true;
		threads.forEach(t -> Thread.startVirtualThread(t));
	}

	/**
	 * Collects and deletes threads from pathtracing
	 */
	private void stopPathtracing(){
		resetPixelBuffer();
		pathtrace = false;
		if (activeThreads == 0) return;
		System.out.printf("Disposing of %d threads\n", activeThreads);
		while (activeThreads != 0){
			Thread.onSpinWait();
		}
		System.out.println("Threads fully disposed");
	}

	/**
	 * Pathtraces the current scene until interrupted. Called by Runner Threads
	 * @param id
	 * @param pointSet
	 * @param sampleCount
	 */
	private void pathtraceUntilInterrupt(int id, List<Long> pointSet, int sampleCount){
		Random random = ThreadLocalRandom.current();
		inc();
		System.out.print("+"+id);
		int samples = 0;
		while (!Thread.currentThread().isInterrupted() && pathtrace && (sampleCount == 0 || samples < sampleCount)){
			int startModCount = modCount;
			long start = System.nanoTime();
			pathtrace(pointSet, random);
			long end = System.nanoTime();
			if (modCount == startModCount){
				timeSamples.add(end-start);
			}
			samples++;
		}
		dec();
		System.out.print("-"+id);
	}


	/**
	 * Called by runner threads only. Pathtraces a collection of points and adds that data to the pixelBuffer
	 * @param points
	 * @param random
	 */
	private void pathtrace(List<Long> points, Random random){
		Vec3 origin = camera.translation;

		// this sorts the objects by their distance to the camera. it allows speedups when a close collision can skip more checks.
		// its done by bit-packing the index and "distance" in a single int
		int[] objectOrder = new int[env.physicalObjects.size()];
		for (int i = 0; i < objectOrder.length; i++){
			float dist = env.physicalObjects.get(i).transform.translation.dist(origin);
			objectOrder[i] = (i & 0xff)|(((int) (dist * 64)) << 8);
		}
		Arrays.sort(objectOrder);

		for (long point : points){
			int x = (int) (point & 0xffffffffl);
			int y = (int) (point >> 32);
			Vec3 vector;
			if (camera.focus == 0){
				vector = camera.rot.mul((new Vec3(x-camera.cx, camera.cy-y, camera.focalLength)).normalize());
			} else {
				origin = camera.translation.add(new Vec3((FloatMath.random()-.5f)*camera.focus, (FloatMath.random()-.5f)*camera.focus, (FloatMath.random()-.5f)*camera.focus));
				Vec3 pixelPoint = camera.translation.add(camera.rot.mul(new Vec3(x-camera.cx, camera.cy-y, camera.focalLength).mul(camera.focusDistance/camera.focalLength)));
				vector = pixelPoint.sub(origin).normalize();
			}

			float[] col = Ray.trace(origin, vector, env, 10, random, objectOrder);

			pixelBuffer[y][x].addSample(col);
		}
	}


	private static class Pixel {
		private int rColor;
		private int gColor;
		private int bColor;
		private int samples;
		private boolean reset;

		public Pixel(){
			this(new float[3], 0);
		}
		public Pixel(float[] color, int weight){
			this.rColor = (int) (255 * color[0]);
			this.gColor = (int) (255 * color[1]);
			this.bColor = (int) (255 * color[2]);
			this.samples = weight;
		}
		public void addSample(float[] color){
			if (reset) clear();
			rColor += (int) (255.0 * color[0]);
			gColor += (int) (255.0 * color[1]);
			bColor += (int) (255.0 * color[2]);
			samples++;
		}
		public int[] getColor(){
			if (samples == 0) return new int[] {0, 0, 0, 255};
			return new int[] {rColor/samples, gColor/samples, bColor/samples, 255};
		}
		public void clear(){
			rColor = gColor = bColor = 0;
			samples = 0;
			reset = false;
		}
		public int getSamples(){
			return samples;
		}
	}
}