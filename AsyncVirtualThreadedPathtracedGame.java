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

public class AsyncVirtualThreadedPathtracedGame extends Game{
	// this is distance per 16 ms
	private final float speed = .02f;
	private final float rotSpeed = .03f;
	// these represent the entire model
	private final Viewport camera;
	private final Environment env;

	private boolean raytrace;
	private final Pixel[][] pixelBuffer;

	private List<Thread> threads;

	private volatile int modCount = 0;
	public List<Long> timeSamples = Collections.synchronizedList(new ArrayList<>());

	public AsyncVirtualThreadedPathtracedGame(Viewport camera, Environment env){
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
	
	private int selected = 0;
	@Override
	public void tick(double dt){
		float relativeSpeed = (float) (this.speed * dt/16.0f);
		float relativeRotSpeed = (float) (this.rotSpeed * dt/16.0f);

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
			if (raytrace){
				resetPixelBuffer();
			} else {
				raytrace = true;
				resetPixelBuffer();
				beginPathtracing2(16);
			}
		}
		if (input.keys[']']) {
			raytrace = false;
			stopPathtracing();
		}
	}

	private void resetPixelBuffer(){
		if (!raytrace) return;
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
		if (raytrace){
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
		super.debug = "frame: "+(time/1_000_000.0);
	}

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
				mesh.bvh.renderWireframe(raster, zBuffer, camera, (int) (input.mouseWheel));
			} else {
				object.renderRasterized(raster, zBuffer, camera);
			}
		}
		return image;
	}
	private void renderPathtraced(){
		if (nextFrame == null) nextFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = nextFrame.getRaster();
		int[] black = {0, 0, 0, 255};
		for (int y = 0; y < pixelBuffer.length; y++){
			for (int x = 0; x < pixelBuffer[y].length; x++){
				if (pixelBuffer[y][x].getSamples() == 0){
					
					//Thread.onSpinWait();
					raster.setPixel(x, y, black);
					continue;
				}
				raster.setPixel(x, y, pixelBuffer[y][x].getColor());
			}
		}
	}


	private void beginPathtracing(int threadCount){
		stopPathtracing();
		int partitions = (int) Math.sqrt(threadCount);
		for (int x = 0; x < partitions; x++){
			for (int y = 0; y < partitions; y++){
				final int x_f = x;
				final int y_f = y;
				Thread t = new Thread(() -> {
					Random random = ThreadLocalRandom.current();
					System.out.println("Thread starting");
					while (!Thread.currentThread().isInterrupted() && raytrace){
						int startModCount = modCount;
						long start = System.nanoTime();
						pathtrace(x_f, y_f, camera.screenWidth, camera.screenHeight, partitions, partitions, random);
						long end = System.nanoTime();
						if (modCount == startModCount){
							timeSamples.add(end-start);
						}
					}
					System.out.println("Thread ending");
				});
				threads.add(t);
			}
		}
		System.out.println("Starting "+ threads.size()+" virtual threads");
		threads.forEach(t -> Thread.startVirtualThread(t));
	}
	private void beginPathtracing2(int threadCount){
		stopPathtracing();
		Random globalRandom = new Random();
		int partitions = (int) Math.sqrt(threadCount);
		List<Pair<Integer, Integer>>[] pointSets = new List[partitions];
		for (int i = 0; i < partitions; i++){
			pointSets[i] = new ArrayList<>();
		}
		for (int x = 0; x < camera.screenWidth; x++){
			for (int y = 0; y < camera.screenHeight; y++){
				pointSets[globalRandom.nextInt(partitions)].add(new Pair<>(x, y));
			}
		}
		for (int i = 0; i < partitions; i++){
			Collections.shuffle(pointSets[i]);
			final int i_f = i;
			Thread t = new Thread(() -> {
				Random random = ThreadLocalRandom.current();
				System.out.println("Thread starting");
				while (!Thread.currentThread().isInterrupted() && raytrace){
					int startModCount = modCount;
					long start = System.nanoTime();
					pathtrace(pointSets[i_f], random);
					long end = System.nanoTime();
					if (modCount == startModCount){
						timeSamples.add(end-start);
					}
				}
				System.out.println("Thread ending");
			});
			threads.add(t);
		}
		System.out.println("Starting "+ threads.size()+" virtual threads");
		threads.forEach(t -> Thread.startVirtualThread(t));
	}
	private void stopPathtracing(){
		if (threads == null) threads = new ArrayList<>();
		if (threads.isEmpty()) return;

		System.out.println("Disposing "+threads.size()+" threads");
		threads.forEach(t -> t.interrupt());
		threads.clear();
	}

	private void pathtrace(int x1, int y1, int x2, int y2, int dx, int dy, Random random){
		Vec3 origin = camera.translation;
		for (int x = x1; x < x2; x += dx){
			for (int y = y1; y < y2; y += dy){
				Vec3 vector;
				if (camera.focus == 0){
					vector = camera.rot.mul((new Vec3(x-camera.cx, camera.cy-y, camera.focalLength)).normalize());
				} else {
					origin = camera.translation.add(new Vec3((random.nextFloat()-.5f)*camera.focus, (random.nextFloat()-.5f)*camera.focus, (random.nextFloat()-.5f)*camera.focus));
					Vec3 pixelPoint = camera.translation.add(camera.rot.mul(new Vec3(x-camera.cx, camera.cy-y, camera.focalLength).mul(camera.focusDistance/camera.focalLength)));
					vector = pixelPoint.sub(origin).normalize();
				}
				
				float[] col = Ray.trace(origin, vector, env, 10, random);
				
				pixelBuffer[y][x].addSample(col);
			}
		}
	}
	private void pathtrace(List<Pair<Integer, Integer>> points, Random random){
		Vec3 origin = camera.translation;
		for (Pair<Integer, Integer> point : points){
			int x = point.t0;
			int y = point.t1;
			Vec3 vector;
			if (camera.focus == 0){
				vector = camera.rot.mul((new Vec3(x-camera.cx, camera.cy-y, camera.focalLength)).normalize());
			} else {
				origin = camera.translation.add(new Vec3((random.nextFloat()-.5f)*camera.focus, (random.nextFloat()-.5f)*camera.focus, (random.nextFloat()-.5f)*camera.focus));
				Vec3 pixelPoint = camera.translation.add(camera.rot.mul(new Vec3(x-camera.cx, camera.cy-y, camera.focalLength).mul(camera.focusDistance/camera.focalLength)));
				vector = pixelPoint.sub(origin).normalize();
			}
			
			float[] col = Ray.trace(origin, vector, env, 10, random);
			
			pixelBuffer[y][x].addSample(col);
		}
	}
}