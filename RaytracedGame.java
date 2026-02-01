import Game.Game;
import Game.Input;
import Math.*;
import java.awt.Color;
import java.awt.Graphics2D;
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

public class RaytracedGame extends Game{

	private final double focalLength;
	// this is only for rasterized rendering
	private final double[][] zBuffer;
	// this is distance per 16 ms
	public final double speed = .02;
	public final double rotSpeed = .03;
	// these represent the entire model
	private final Transform cam;
	private final Environment env;

	public boolean raytrace = false;
	public final Pixel[][] pixelBuffer;
	
	public static long logicTime;

	private final int cx;
	private final int cy;

	public RaytracedGame(int width, int height, double fov, double focusDistance, Environment env){
		super(width, height);

		cx = width / 2;
		cy = height / 2;

		this.env = env;
		cam = new Transform();
		cam.translateAbsolute(cam.getForwardVector().mul(-1));

		this.focalLength = (double) width / (2 * Math.tan(fov/2));
		zBuffer = new double[width][height];
		pixelBuffer = new Pixel[width][height];
		for (Pixel[] row : pixelBuffer) {
			for (int x = 0; x < row.length; x++) {
				row[x] = new Pixel();
			}
		}
		raytrace = true;
		resetPixelBuffer();
		raytrace = false;
	}

	@Override
	public String name(){
		return "Raytraced 3d";
	}
	
	@Override
	public void tick(double dt){
		long start = System.nanoTime();
		double speed = this.speed * dt/16.0;
		double rotSpeed = this.rotSpeed * dt/16.0;

		if (input.keys['W']) 			{resetPixelBuffer(); cam.translate(0, 0,  speed);}
		if (input.keys['A']) 			{resetPixelBuffer(); cam.translate(-speed, 0, 0);}
		if (input.keys['S']) 			{resetPixelBuffer(); cam.translate(0, 0, -speed);}
		if (input.keys['D']) 			{resetPixelBuffer(); cam.translate( speed, 0, 0);}
		if (input.keys[' ']) 			{resetPixelBuffer(); cam.translate(0,  speed, 0);}
		if (input.keys[Input.SHIFT]) 	{resetPixelBuffer(); cam.translate(0, -speed, 0);}

		if (input.keys[Input.UP_ARROW]) 	{resetPixelBuffer(); cam.rotateX( rotSpeed);}
		if (input.keys[Input.DOWN_ARROW]) 	{resetPixelBuffer(); cam.rotateX(-rotSpeed);}
		if (input.keys[Input.LEFT_ARROW]) 	{resetPixelBuffer(); cam.rotateY( rotSpeed);}
		if (input.keys[Input.RIGHT_ARROW]) 	{resetPixelBuffer(); cam.rotateY(-rotSpeed);}
		if (input.keys['Q']) 				{resetPixelBuffer(); cam.rotateZ(-rotSpeed);}
		if (input.keys['E']) 				{resetPixelBuffer(); cam.rotateZ( rotSpeed);}

		if (input.keys['[']) 	{raytrace = true; resetPixelBuffer();}
		if (input.keys[']']) 	raytrace = false;

		logicTime = System.nanoTime()-start;
	}

	private void clearZBuffer() {
		for (int x = 0; x < width; x++) {
			Arrays.fill(zBuffer[x], Double.POSITIVE_INFINITY);
		}
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
		Vec3 light = cam.getForwardVector();
		clearZBuffer();
		
		for (PhysicalObject p : env.physicalObjects){
			if (p instanceof Mesh mesh){
				for (Triangle triangle : mesh.triangles){
					triangle.recolor(light);
				}
			}
		}
		
		for (PhysicalObject physicalObjects : env.physicalObjects){
			physicalObjects.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
		for (Point point : env.points){
			point.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
		return image;
	}
	@Override
	public void generateFrame(){
		long renderStart = System.nanoTime();
		if (input.keys['K'] && nextFrame != null){
			try {
				File outputfile = new File("saved.png");
				ImageIO.write(nextFrame, "png", outputfile);
			} catch (IOException e) {}
		}
		if (raytrace){
			//nextFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			WritableRaster raster = nextFrame.getRaster();

			clearZBuffer();
			int ts = 6;
			List<Thread> threads = new ArrayList<>();
			for (int x = 0; x < ts; x++){
				for (int y = 0; y < ts; y++){
					final int x_f = x;
					final int y_f = y;
					Thread t = new Thread(() -> {
						raytraceRange(width*x_f/ts, height*y_f/ts, width*(x_f+1)/ts, height*(y_f+1)/ts, raster);
					});
					t.start();
					threads.add(t);
				}
			}
			try {
				for (Thread t : threads){
					t.join();
				}
			} catch (InterruptedException e){}
		} else {
			nextFrame = renderRasterized();
		}
		
		Graphics2D g2d = nextFrame.createGraphics();
		long renderTime = System.nanoTime()-renderStart;
		g2d.setColor(Color.RED);
		g2d.drawString("Render (ms):"+renderTime/1_000_000.0,0,20);
		g2d.drawString("Logic  (ms):"+logicTime/1_000_000.0,0,40);
		
		g2d.drawString(String.format("%2.2f",Math.random()), 0, 60);
		g2d.drawString(cam.translation.toString(), 0, 80);
	}
	
	private void raytraceRange(int x1, int y1, int x2, int y2, WritableRaster raster){
		Random random = ThreadLocalRandom.current();
		Vec3 origin = cam.translation;
		for (int x = x1; x < x2; x += 1){
			for (int y = y1; y < y2; y += 1){
				double px = (double)(x-cx);
				double py = (double)(cy-y);

				Vec3 vector = cam.rot.transform(new Vec3(px, py, focalLength).normalize());
				
				Pixel pixel = pixelBuffer[y][x];
				int[] color = new int[3];
				int samples = 4;
				for (int i = 0; i < samples; i++){
					double[] col = Ray.trace(origin, vector, env, 10, random);
					color[0] += (int) (255 * col[0]);
					color[1] += (int) (255 * col[1]);
					color[2] += (int) (255 * col[2]);
				}
				pixel.addSample(color, samples);
				color = pixel.getColor();
				int[] colori = {(int)(color[0]), (int)(color[1]), (int)(color[2]), 255};
				raster.setPixel(x, y, colori);
			}
		}
	}
}