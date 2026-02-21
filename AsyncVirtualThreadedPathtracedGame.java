import Game.Game;
import Game.Input;
import Math.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

public class AsyncVirtualThreadedPathtracedGame extends Game{
	
	// this is only for rasterized rendering
	private final double[][] zBuffer;
	// this is distance per 16 ms
	public final double speed = .02;
	public final double rotSpeed = .03;
	// these represent the entire model
	private final Viewport camera;
	private final Environment env;

	public boolean raytrace = false;
	public final Pixel[][] pixelBuffer;

	public List<Thread> threads;
	
	public static long logicTime;

	public AsyncVirtualThreadedPathtracedGame(Viewport camera, Environment env){
		super(camera.screenWidth, camera.screenHeight);
		
		this.env = env;
		this.camera = camera;
	
		zBuffer = new double[width][height];

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
	
	@Override
	public void tick(double dt){
		long start = System.nanoTime();
		double relativeSpeed = this.speed * dt/16.0;
		double relativeRotSpeed = this.rotSpeed * dt/16.0;

		if (input.keys['W']) 			{resetPixelBuffer(); camera.translateZ( relativeSpeed);}
		if (input.keys['A']) 			{resetPixelBuffer(); camera.translateX(-relativeSpeed);}
		if (input.keys['S']) 			{resetPixelBuffer(); camera.translateZ(-relativeSpeed);}
		if (input.keys['D']) 			{resetPixelBuffer(); camera.translateX( relativeSpeed);}
		if (input.keys[' ']) 			{resetPixelBuffer(); camera.translateY( relativeSpeed);}
		if (input.keys[Input.SHIFT]) 	{resetPixelBuffer(); camera.translateY(-relativeSpeed);}

		if (input.keys[Input.UP_ARROW]) 	{resetPixelBuffer(); camera.rotateX( relativeRotSpeed);}
		if (input.keys[Input.DOWN_ARROW]) 	{resetPixelBuffer(); camera.rotateX(-relativeRotSpeed);}
		if (input.keys[Input.LEFT_ARROW]) 	{resetPixelBuffer(); camera.rotateY( relativeRotSpeed);}
		if (input.keys[Input.RIGHT_ARROW]) 	{resetPixelBuffer(); camera.rotateY(-relativeRotSpeed);}
		if (input.keys['Q']) 				{resetPixelBuffer(); camera.rotateZ(-relativeRotSpeed);}
		if (input.keys['E']) 				{resetPixelBuffer(); camera.rotateZ( relativeRotSpeed);}

		if (input.keys['[']) {
			if (raytrace){
				resetPixelBuffer();
			} else {
				raytrace = true;
				resetPixelBuffer();
				beginPathtracing(16);
			}
		}
		if (input.keys[']']) {
			raytrace = false;
			stopPathtracing();
		}

		logicTime = System.nanoTime()-start;
	}

	private void resetPixelBuffer(){
		if (!raytrace) return;
		for (Pixel[] row : pixelBuffer) {
			for (Pixel element : row) {
				element.clear();
			}
		}
	}

	

	private BufferedImage renderRasterized(){
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();
		for (int x = 0; x < width; x++) {
			Arrays.fill(zBuffer[x], Double.POSITIVE_INFINITY);
		}
		
		for (PhysicalObject object : env.physicalObjects){
			object.render(raster, zBuffer, camera);
		}
		for (PhysicalObject object : env.physicalObjects){
			if (object instanceof Mesh mesh){
				mesh.bvh.render(raster, zBuffer, camera);
			}
		}
		return image;
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
	}

	private void renderPathtraced(){
		WritableRaster raster = nextFrame.getRaster();
		for (int y = 0; y < pixelBuffer.length; y++){
			for (int x = 0; x < pixelBuffer[y].length; x++){
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
					while (!Thread.currentThread().isInterrupted()){
						pathtrace(x_f, y_f, camera.screenWidth, camera.screenHeight, partitions, partitions, random);
					}
				});
				threads.add(t);
			}
		}
		threads.forEach(t -> Thread.startVirtualThread(t));
		System.out.println("Started "+ threads.size()+" virtual threads");
	}
	private void stopPathtracing(){
		if (threads == null) threads = new ArrayList<>();
		if (threads.isEmpty()) return;

		threads.forEach(t -> t.interrupt());
		System.out.println("Dispatched "+threads.size()+" threads");
		threads.clear();
	}

	private void pathtrace(int x1, int y1, int x2, int y2, int dx, int dy, Random random){
		Vec3 origin = camera.transform.translation;
		for (int x = x1; x < x2; x += dx){
			for (int y = y1; y < y2; y += dy){
				Vec3 vector;
				if (camera.focus == 0){
					vector = camera.transform.rot.transform((new Vec3(x-camera.cx, camera.cy-y, camera.focalLength)).normalize());
				} else {
					origin = camera.transform.translation.add(new Vec3((random.nextDouble()-.5)*camera.focus, (random.nextDouble()-.5)*camera.focus, (random.nextDouble()-.5)*camera.focus));
					Vec3 pixelPoint = camera.transform.translation.add(camera.transform.rot.transform(new Vec3(x-camera.cx, camera.cy-y, camera.focalLength).mul(camera.focusDistance/camera.focalLength)));
					vector = pixelPoint.sub(origin).normalize();
				}
				
				double[] col = Ray.trace(origin, vector, env, 10, random);
				
				pixelBuffer[y][x].addSample(
					new int[] {
						(int) (255.0 * col[0]),
						(int) (255.0 * col[1]),
						(int) (255.0 * col[2])
					},
					1
				);
			}
		}
	}
}