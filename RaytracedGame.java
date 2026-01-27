import Game.Game;
import Game.Input;
import Math.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.List;

public class RaytracedGame extends Game{

	private final double focalLength;

	private final double[][] zBuffer;

	public final double speed = .02;
	public final double rotSpeed = .03;

	private Vec3 light;

	private final Transform cam;

	private final Environment env;
	
	public static long logicTime;

	int cx = width / 2;
	int cy = height / 2;

	public RaytracedGame(int width, int height, double fov, Environment env){
		super(width, height);
		this.light = new Vec3(0, 1, 1).normalize();
		
		this.env = env;
		
		cam = new Transform();
		cam.translateAbsolute(cam.getForwardVector().mul(-1));

		
		this.focalLength = (double) height / (2 * Math.tan(fov/2));
		zBuffer = new double[width][height];
	}

	@Override
	public String name(){
		return "Raytraced 3d";
	}
	
	@Override
	public void tick(){
		long start = System.nanoTime();

		if (input.keys['W']) 				{render = null; cam.translate(0, 0, speed);}
		if (input.keys['A']) 				{render = null; cam.translate(-speed, 0, 0);}
		if (input.keys['S']) 				{render = null; cam.translate(0, 0, -speed);}
		if (input.keys['D']) 				{render = null; cam.translate(speed, 0, 0);}
		if (input.keys[' ']) 				{render = null; cam.translate(0, speed, 0);}
		if (input.keys[Input.SHIFT]) 		{render = null; cam.translate(0, -speed, 0);}
		if (input.keys[Input.UP_ARROW]) 	{render = null; cam.rotateX( rotSpeed);}
		if (input.keys[Input.DOWN_ARROW]) 	{render = null; cam.rotateX(-rotSpeed);}
		if (input.keys[Input.LEFT_ARROW]) 	{render = null; cam.rotateY( rotSpeed);}
		if (input.keys[Input.RIGHT_ARROW]) 	{render = null; cam.rotateY(-rotSpeed);}
		if (input.keys['Q']) 				{render = null; cam.rotateZ(-rotSpeed);}
		if (input.keys['E']) 				{render = null; cam.rotateZ( rotSpeed);}

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
	private void renderRasterized(Graphics2D g2d){
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();
		Vec3 light = cam.getForwardVector();
		clearZBuffer();
		for (Triangle triangle : env.mesh.triangles()){
			triangle.recolor(light);
			triangle.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
		
		Vec3 origin = cam.translation;
		Vec3 vector = cam.getForwardVector().normalize();
		
		Vec3 intersection = null;
		for (Triangle tri : env.mesh.triangles()){
			Vec3 localIntersection = tri.getIntersection(vector, origin);
			if (localIntersection == null) continue;
			if (intersection == null || origin.dist(intersection) > origin.dist(localIntersection)){
				intersection = localIntersection;
			}
		}
		for (Sphere p : env.lights){
			p.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
		new Sphere(new Vec3(0, 0, 0), .01).render(raster, focalLength, cx, cy, zBuffer, cam);
		
		g2d.drawImage(image, 0, 0, null);
	
		g2d.drawString(Math.random()+"", 0, 100);
		g2d.drawString("Cam Pos:"+cam.translation.toString(), 0, 60);
		g2d.drawString("Cam Rot:"+cam.rot.toString(), 0, 80);
	}
	public static BufferedImage render = null;
	@Override
	public void updateFrame(Graphics2D g2d){
		
		if (input.keys[0x11]) {
			if (render != null) {
				g2d.drawImage(render, 0, 0, null);
				return;
			}
			long renderStart = System.nanoTime();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			WritableRaster raster = image.getRaster();

			clearZBuffer();
		
			env.points.clear();
			List<Sphere> points = env.points;
			final int incr = 4;
			
			points.add(new Sphere(cam.translation, .01, Color.white));

			Vec3 origin = cam.translation;
			for (int x = 0; x < width; x+=incr){
				for (int y = 0; y < height; y+=incr){
					double px = (double)(x-cx);
					double py = (double)(cy-y);

					Vec3 vector = cam.rot.transform(new Vec3(px, py, focalLength).normalize());

					Sphere p = new Sphere(vector.add(origin),.01,
						new Color((256*x)/width, (256*y)/height, 0)
					);
					points.add(p);
					
					int[] color = Ray.getColor(origin, vector, env, 3);
					for (int dx = 0; dx < incr; dx++){
						for (int dy = 0; dy < incr; dy++){
							raster.setPixel(x+dx, y+dy, color);
						}
					}
				}
				System.out.println(x);
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