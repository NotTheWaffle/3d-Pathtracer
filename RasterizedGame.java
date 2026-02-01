import Game.Game;
import Game.Input;
import Math.Vec3;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class RasterizedGame extends Game{

	private final double focalLength;

	private final double[][] zBuffer;

	public final double speed = .02;
	public final double rotSpeed = .03;

	private final Transform cam;

	private final Environment env;
	
	int cx = width / 2;
	int cy = height / 2;

	public RasterizedGame(int width, int height, double fov, Environment env){
		super(width, height);
		
		this.env = env;
		
		cam = new Transform();
		cam.translateAbsolute(cam.getForwardVector().mul(-1));

		
		this.focalLength = (double) height / (2 * Math.tan(fov/2));
		zBuffer = new double[width][height];
	}

	@Override
	public String name(){
		return "Rasterized 3d";
	}
	public static long logicTime;
	@Override
	public void tick(double dt){
		long start = System.nanoTime();

		if (input.keys['W']) 				cam.translate(0, 0, speed);
		if (input.keys['A']) 				cam.translate(-speed, 0, 0);
		if (input.keys['S']) 				cam.translate(0, 0, -speed);
		if (input.keys['D']) 				cam.translate(speed, 0, 0);
		if (input.keys[' ']) 				cam.translate(0, speed, 0);
		if (input.keys[Input.SHIFT]) 		cam.translate(0, -speed, 0);
	
		if (input.keys[Input.UP_ARROW]) 	cam.rotateX( rotSpeed);
		if (input.keys[Input.DOWN_ARROW]) 	cam.rotateX(-rotSpeed);
		if (input.keys[Input.LEFT_ARROW]) 	cam.rotateY( rotSpeed);
		if (input.keys[Input.RIGHT_ARROW]) 	cam.rotateY(-rotSpeed);
		if (input.keys['Q']) 				cam.rotateZ(-rotSpeed);
		if (input.keys['E']) 				cam.rotateZ( rotSpeed);

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
	
	public void generateFrame(){
		long renderStart = System.nanoTime();
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
		new Point(new Vec3(0, 0, 0), 1).render(raster, focalLength, cx, cy, zBuffer, cam);
		Graphics2D g2d = image.createGraphics();
		long renderTime = System.nanoTime()-renderStart;
		g2d.drawString("Render (ms):"+renderTime/1_000_000.0,0,20);
		g2d.drawString("Logic  (ms):"+logicTime/1_000_000.0,0,40);
	
		g2d.drawString(Math.random()+"", 0, 100);
		g2d.drawString("Cam Pos:"+cam.translation.toString(), 0, 60);
		g2d.drawString("Cam Rot:"+cam.rot.toString(), 0, 80);
	}
}