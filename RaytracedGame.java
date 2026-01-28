import Game.Game;
import Game.Input;
import Math.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import javax.imageio.ImageIO;

public class RaytracedGame extends Game{

	private final double focalLength;

	private final double[][] zBuffer;

	public final double speed = .02;
	public final double rotSpeed = .03;

	private final Transform cam;
	private final Environment env;

	private final int pixelSize;
	private final int screenWidth;
	private final int screenHeight;
	
	public static long logicTime;

	int cx = width / 2;
	int cy = height / 2;

	public RaytracedGame(int width, int height, int pixelSize, double fov, Environment env){
		super(width, height);
		this.pixelSize = pixelSize;
		screenHeight = height/pixelSize;
		screenWidth = width/pixelSize;

		this.env = env;
		
		cam = new Transform();
		cam.translateAbsolute(cam.getForwardVector().mul(-1));

		
		this.focalLength = (double) height / (2 * Math.tan(fov/2));
		zBuffer = new double[width][height];
		pixelBuffer = new Pixel[screenWidth][screenHeight];
		raytrace = true;
		resetPixelBuffer();
		raytrace = false;
		System.out.println(pixelBuffer[0][0]);
	}

	@Override
	public String name(){
		return "Raytraced 3d";
	}
	
	@Override
	public void tick(){
		long start = System.nanoTime();

		if (input.keys['W']) 				{resetPixelBuffer(); cam.translate(0, 0, speed);}
		if (input.keys['A']) 				{resetPixelBuffer(); cam.translate(-speed, 0, 0);}
		if (input.keys['S']) 				{resetPixelBuffer(); cam.translate(0, 0, -speed);}
		if (input.keys['D']) 				{resetPixelBuffer(); cam.translate(speed, 0, 0);}
		if (input.keys[' ']) 				{resetPixelBuffer(); cam.translate(0, speed, 0);}
		if (input.keys[Input.SHIFT]) 		{resetPixelBuffer(); cam.translate(0, -speed, 0);}

		if (input.keys[Input.UP_ARROW]) 	{resetPixelBuffer(); cam.rotateX( rotSpeed);}
		if (input.keys[Input.DOWN_ARROW]) 	{resetPixelBuffer(); cam.rotateX(-rotSpeed);}
		if (input.keys[Input.LEFT_ARROW]) 	{resetPixelBuffer(); cam.rotateY( rotSpeed);}
		if (input.keys[Input.RIGHT_ARROW]) 	{resetPixelBuffer(); cam.rotateY(-rotSpeed);}
		if (input.keys['Q']) 				{resetPixelBuffer(); cam.rotateZ(-rotSpeed);}
		if (input.keys['E']) 				{resetPixelBuffer(); cam.rotateZ( rotSpeed);}

		if (input.keys['['])	{raytrace = true; resetPixelBuffer();}
		if (input.keys[']'])	raytrace = false;

		logicTime = System.nanoTime()-start;
	}

	public double getX(double x, double y, double z) {
		return focalLength * (x / z);
	}
	public double getY(double x, double y, double z) {
		return focalLength * (y / z);
	}

	private void clearZBuffer() {
		for (int x = 0; x < width; x++) {
			Arrays.fill(zBuffer[x], Double.POSITIVE_INFINITY);
		}
	}

	private void resetPixelBuffer(){
		if (!raytrace) return;
		for (Pixel[] row : pixelBuffer) {
			for (int x = 0; x < row.length; x++) {
				row[x] = new Pixel();
			}
		}
	}

	private void renderRasterized(Graphics2D g2d){
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
		g2d.drawImage(image, 0, 0, null);
	
		g2d.drawString(Math.random()+"", 0, 20);
		g2d.drawString(cam.translation.toString(), 0, 40);
	}
	public static BufferedImage render = null;
	public static boolean raytrace = false;
	public static Pixel[][] pixelBuffer;
	@Override
	public void updateFrame(Graphics2D g2d){
		if (input.keys['K'] && render != null){
			try {
				File outputfile = new File("saved.png");
				ImageIO.write(render, "png", outputfile);
			} catch (IOException e) {
			}
		}
		if (raytrace || input.keys['G']){
			
			long renderStart = System.nanoTime();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			WritableRaster raster = image.getRaster();

			clearZBuffer();
		
			
			Random random = new Random();
			Vec3 origin = cam.translation;
			for (int x = 0; x < width; x+=pixelSize){
				for (int y = 0; y < height; y+=pixelSize){
					double px = (double)(x-cx);
					double py = (double)(cy-y);

					Vec3 vector = cam.rot.transform(new Vec3(px, py, focalLength).normalize());
					
					
					double[] color = new double[3];
					int resolution = 1;
					for (int i = 0; i < resolution; i++){
						double[] col = Ray.getColor(origin, vector, env, 4, random);
						color[0] += col[0];
						color[1] += col[1];
						color[2] += col[2];
					}
					color[0] /= resolution;
					color[1] /= resolution;
					color[2] /= resolution;
					pixelBuffer[y/pixelSize][x/pixelSize].addSample(color, 1);
					color = pixelBuffer[y/pixelSize][x/pixelSize].color;

					int[] colori = {(int)(color[0]*255), (int)(color[1]*255), (int)(color[2]*255), 255};
					for (int dx = 0; dx < pixelSize; dx++){
						for (int dy = 0; dy < pixelSize; dy++){
							raster.setPixel(x+dx, y+dy, colori);
						}
					}
				}
			}

			g2d.drawImage(image, 0, 0, null);
			render = image;
			long renderTime = System.nanoTime()-renderStart;
			g2d.drawString("Render (ms):"+renderTime/1_000_000.0,0,20);
			g2d.drawString("Logic  (ms):"+logicTime/1_000_000.0,0,40);
			g2d.drawString(cam.toString(), 0, 60);
			return;
		}
		renderRasterized(g2d);
	}
}