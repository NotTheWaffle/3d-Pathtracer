import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Game3d extends Game{

	private final double focalLength;

	private final double[][] zBuffer;

	public final double speed = .02;
	public final double rotSpeed = .03;

	private final Vec3 light;

	private Transform cam;

	Triangle[] triangles;
	
	int cx = width / 2;
	int cy = height / 2;

	public Game3d(int width, int height, double fov, String model){
		super(width, height);
		String fileName = "Models/"+model+".obj";
		this.light = new Vec3(0, 1, -1).normalize();
		
		try {
			Mesh mesh = loadMesh(fileName);
			triangles = mesh.triangles();
			System.out.println("Estimating "+(mesh.triangles().length * 60 + mesh.points().length * 40)/(1024*1024.0)+" megabytes of memory");
		} catch (IOException _) {
			System.out.println("Unable to load");
		}
		
		
		
		if (triangles == null){
			System.exit(1);
		}
		
		// 60 bytes per tri, 36 per point
		Arrays.stream(triangles).forEach(tri -> tri.recolor(light));
		cam = new Transform();
		cam.rotateZ(Math.PI);
		cam.translate(0, 0, 1);
		
		this.focalLength = (double) height / (2 * Math.tan(fov/2));
		zBuffer = new double[width][height];
	}

	@Override
	public String name(){
		return "Game 3d";
	}
	public static long logicTime;
	@Override
	public void tick(){
		long start = System.nanoTime();
		if (input.keys['W']) 				cam.translate(0, 0, -speed);
		if (input.keys['A']) 				cam.translate(speed, 0, 0);
		if (input.keys['S']) 				cam.translate(0, 0, speed);
		if (input.keys['D']) 				cam.translate(-speed, 0, 0);
		if (input.keys[' ']) 				cam.translate(0, -speed, 0);
		if (input.keys[Input.SHIFT]) 		cam.translate(0, speed, 0);
	
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
	
	@Override
	public void updateFrame(Graphics2D g2d){
		long renderStart = System.nanoTime();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();

		Vec3 l = cam.getForwardVector();
		for (Triangle t : triangles){
			t.recolor(l);
		}

		clearZBuffer();
		for (Triangle triangle : triangles){
			triangle.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
		
		Vec3 origin = cam.translation;
		Vec3 vector = cam.getForwardVector().normalize();
		
		Vec3 intersect = null;
		for (Triangle tri : triangles){
			Vec3 inter = tri.getIntersection(vector, origin);
			if (inter == null) continue;
			if (intersect == null || origin.dist(intersect) > origin.dist(inter)){
				intersect = inter;
			}
		}

		
		final int incr = 16;
		for (int x = 0; x < width; x+=incr){
			for (int y = 0; y < height; y+=incr){
				double px = (double)(x-cx)/width;
				double py = (double)(cy-y)/height;

				Point p = new Point(cam.rot.transform(new Vec3(px, py, focalLength).sub(cam.translation).normalize()),.01);
				p.render(raster, focalLength, cx, cy, zBuffer, cam);
			}
		}
		new Point(new Vec3(0,0,0),.1).render(raster, focalLength, cx, cy, zBuffer, cam);

		g2d.drawImage(image, 0, 0, null);
		
		long renderTime = System.nanoTime()-renderStart;
		g2d.drawString("Render (ms):"+renderTime/1_000_000.0,0,20);
		g2d.drawString("Logic  (ms):"+logicTime/1_000_000.0,0,40);
	}
	public void updateDisplay(Graphics2D g2d){
		long renderStart = System.nanoTime();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		WritableRaster raster = image.getRaster();

		clearZBuffer();
	
		Vec3 origin = cam.translation;
		
		List<Point> points = new ArrayList<>();
		final int incr = 16 ;
		for (int x = 0; x < width; x+=incr){
			for (int y = 0; y < height; y+=incr){
				int px = x-cx;
				int py = cy-y;

				
				Vec3 vector = cam.inv.transform(new Vec3(px, py, focalLength)).normalize();
				Vec3 intersect = null;
				for (Triangle tri : triangles){
					Vec3 inter = tri.getIntersection(vector, origin);
					if (inter == null) continue;
					if (intersect == null || origin.dist(intersect) > origin.dist(inter)){
						intersect = inter;
					}
				}
				if (intersect == null) continue;
				int magnitude = (int) (255 * cam.translation.dist(intersect));
				int[] color = {
					magnitude,
					0,
					0,
					255,
				};
				for (int dx = 0; dx < incr; dx++){
					for (int dy = 0; dy < incr; dy++){
						raster.setPixel(x+dx, y+dy, color);
					}
				}
			}
		}


		for (Point point : points){
			point.render(raster, focalLength, cx, cy, zBuffer, cam);
		}

		g2d.drawImage(image, 0, 0, null);
		
		long renderTime = System.nanoTime()-renderStart;
		g2d.drawString("Render (ms):"+renderTime/1_000_000.0,0,20);
		g2d.drawString("Logic  (ms):"+logicTime/1_000_000.0,0,40);
	}



	public static Mesh loadMesh(String filename) throws IOException{
		System.out.println("Loading "+filename+"... ");
		List<Vec3> points = new ArrayList<>();
		List<Triangle> triangles = new ArrayList<>();
		double minX, minY, minZ, maxX, maxY, maxZ;
		minX = minY = minZ = Double.POSITIVE_INFINITY;
		maxX = maxY = maxZ = Double.NEGATIVE_INFINITY;
		
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		List<Vec3> pointBuffer = new ArrayList<>();
		for (String line = reader.readLine(); line != null; line = reader.readLine()){
			if (line.length() == 0) continue;
			if (line.charAt(0) == '#') continue;
			char type = line.charAt(0);
			if (type == 'v'){
				if (line.charAt(1) == 'n' || line.charAt(1) == 't'){
					continue;
				}
				String[] rawValues = line.split(" ");
				Vec3 point = new Vec3(Double.parseDouble(rawValues[1]),Double.parseDouble(rawValues[2]),Double.parseDouble(rawValues[3]));
				if (point.x > maxX) maxX = point.x;
				if (point.x < minX) minX = point.x;
				
				if (point.y > maxY) maxY = point.y;
				if (point.y < minY) minY = point.y;
				
				if (point.z > maxZ) maxZ = point.z;
				if (point.z < minZ) minZ = point.z;

				points.add(point);
			} else if (type == 'f'){
				String[] thesePoints = line.split(" ");
				pointBuffer.clear();
				for (int i = 1; i < thesePoints.length; i++){
					String rawPoint = thesePoints[i];
					int index = Integer.parseInt(rawPoint.split("/")[0]);
					pointBuffer.add(points.get(index-1));
				}
				while (pointBuffer.size()>2){
					triangles.add(new Triangle(0, 1, 2, pointBuffer.toArray(Vec3[]::new)));
					pointBuffer.remove(1);
				}
			}
		}
		reader.close();
		System.out.println("  Loaded "+triangles.size()+" triangles");
		System.out.println("  Loaded "+points.size()+" points");
		System.out.println(filename+" successfully loaded");

		double xrange = maxX-minX;
		double yrange = maxY-minY;
		double zrange = maxZ-minZ;
		double maxRange = Math.max(Math.max(xrange,yrange),zrange);

		double scale = 1/maxRange;

		xrange *= scale;
		yrange *= scale;
		zrange *= scale;

		for (Vec3 point : points){
			point.x = (point.x - minX)*scale-xrange/2;
			point.y = (point.y - minY)*scale-yrange/2;
			point.z = (point.z - minZ)*scale-zrange/2;
		}
		Triangle[] rTriangles = triangles.toArray(Triangle[]::new);
		Vec3[] rPoints = points.toArray(Vec3[]::new);
		return new Mesh(rPoints, rTriangles);
	}
}