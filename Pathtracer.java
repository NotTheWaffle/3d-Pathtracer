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
	/**
	 * Distance per second
	 */
	private final float speed = 1.25f;
	/**
	 * Radians per second
	 */
	private final float rotSpeed = 1.875f;

	// these represent the entire model
	private final Viewport camera;
	private final Environment env;

	// pathtracing control variables
	private boolean pathtrace;
	private final Pixel[][] pixelBuffer;
	private List<Runnable> threads;


	private volatile int modCount = 0;
	public List<Long> timeSamples = Collections.synchronizedList(new ArrayList<>());

	public Pathtracer(Viewport camera, Environment env){
		super(camera.screenWidth, camera.screenHeight);

		this.env = env;
		this.camera = camera;


		pixelBuffer = new Pixel[height][width];
		for (Pixel[] row : pixelBuffer) {
			for (int x = 0; x < row.length; x++) {
				row[x] = new Pixel();
			}
		}
	}

	@Override
	public String name(){
		return "Pathtraced 3d";
	}
	/**
	 * Selected and controlled object
	 */
	private int selected = 0;
	@Override
	public void tick(double dt){
		float relativeSpeed = (float) (this.speed * dt);
		float relativeRotSpeed = (float) (this.rotSpeed * dt);

		Transform transform = camera;
		for (char a = '0'; a <= '9'; a++){
			if (input.keys[a]){
				selected = a-'0';
			}
		}
		if (selected != 0){
			int a = selected;
			if (a > env.physicalObjects.size()){
				a = env.physicalObjects.size();
			}
			transform = env.physicalObjects.get(a-1).transform;
		}

		if (input.keys['W']) 			{resetPixelBuffer(); transform.move(0, 0, relativeSpeed);}
		if (input.keys['A']) 			{resetPixelBuffer(); transform.move(-relativeSpeed, 0, 0);}
		if (input.keys['S']) 			{resetPixelBuffer(); transform.move(0, 0, -relativeSpeed);}
		if (input.keys['D']) 			{resetPixelBuffer(); transform.move(relativeSpeed, 0, 0);}
		if (input.keys[' ']) 			{resetPixelBuffer(); transform.move(0, relativeSpeed, 0);}
		if (input.keys[Input.SHIFT]) 	{resetPixelBuffer(); transform.move(0, -relativeSpeed, 0);}

		if (input.keys[Input.UP_ARROW]) 	{resetPixelBuffer(); transform.turnX( relativeRotSpeed);}
		if (input.keys[Input.DOWN_ARROW]) 	{resetPixelBuffer(); transform.turnX(-relativeRotSpeed);}
		if (input.keys[Input.LEFT_ARROW]) 	{resetPixelBuffer(); transform.turnY( relativeRotSpeed);}
		if (input.keys[Input.RIGHT_ARROW]) 	{resetPixelBuffer(); transform.turnY(-relativeRotSpeed);}
		if (input.keys['Q']) 				{resetPixelBuffer(); transform.turnZ(-relativeRotSpeed);}
		if (input.keys['E']) 				{resetPixelBuffer(); transform.turnZ( relativeRotSpeed);}


		if (input.keys['[']) {
			if (pathtrace){
				resetPixelBuffer();
			} else {
				pathtrace = true;
				resetPixelBuffer();
				beginPathtracing(16);
			}
		}
		if (input.keys[']']) {
			pathtrace = false;
			stopPathtracing();
		}
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
		if (input.keys['K'] && nextFrame != null){
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
			nextFrame = renderRasterized();
		}

		if (timeSamples.isEmpty()){
			this.debug ="";
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
	 * @return
	 */
	private BufferedImage renderRasterized(){
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();
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
		return image;
	}
	/**
	 * Renders the scene using pathtracing
	 */
	private void renderPathtraced(){
		if (nextFrame == null) nextFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		WritableRaster raster = nextFrame.getRaster();
		int[] black = {0, 0, 0, 255};
		for (int y = 0; y < pixelBuffer.length; y++){
			for (int x = 0; x < pixelBuffer[y].length; x++){
				if (width <= 256 && pixelBuffer[y][x].getSamples() == 0){
					long start = System.nanoTime();
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
	}

	private int activeThreads = 0;
	/**
	 * Creates and dispatches threads to pathtrace the scene
	 * @param threadCount
	 */
	private void beginPathtracing(int threadCount){
		stopPathtracing();
		Random globalRandom = new Random();
		// array of arralists of pairs of integers
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
		for (int i = 0; i < threadCount; i++){
			Collections.shuffle(pointSets[i]);
			final int iFinal = i;

			Runnable t = () -> {
				int id = iFinal;
				List<Long> pointSet = pointSets[id];
				Random random = ThreadLocalRandom.current();
				while (activeThreads != id){
					Thread.onSpinWait();
				}
				System.out.println("Starting Thread "+id);
				activeThreads++;
				while (!Thread.currentThread().isInterrupted() && pathtrace){
					int startModCount = modCount;
					long start = System.nanoTime();
					pathtrace(pointSet, random);
					long end = System.nanoTime();
					if (modCount == startModCount){
						timeSamples.add(end-start);
					}
				}
				while (activeThreads != id+1){
					Thread.onSpinWait();
				}
				System.out.println("Ending Thread "+id);
				activeThreads--;
			};

			threads.add(t);
		}
		System.out.println("Starting "+ threads.size()+" virtual threads");
		threads.forEach(t -> Thread.startVirtualThread(t));
	}
	/**
	 * Collects and deletes threads from pathtracing
	 */
	private void stopPathtracing(){
		if (threads == null) threads = new ArrayList<>();
		if (threads.isEmpty()) return;

		System.out.println("Disposing "+threads.size()+" threads");
		threads.clear();
		while (activeThreads != 0){
			Thread.onSpinWait();
		}
		System.out.println("Threads fully disposed");
	}

	/**
	 * Called by runner threads only. Pathtraces a collection of points.
	 * @param points
	 * @param random
	 */
	private void pathtrace(List<Long> points, Random random){
		Vec3 origin = camera.translation;

		// bitpacking thing to sort the indicies by the dist to them. 1/64th precision is good enough for a speedup measure
		// this allows us to make us of skipping over objects when we have found a closer collision
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
			if (reset){
				clear();
			}
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