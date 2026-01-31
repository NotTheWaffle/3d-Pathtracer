
import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Mesh extends PhysicalObject{
	public final AABB bounds;
	public final Triangle[] triangles;
	public Mesh(Triangle[] triangles, AABB bounds){
		super(null, null);
		this.triangles = triangles;
		this.bounds = bounds;
	}
	public static Mesh loadObj(String filename, boolean rescale, Color color, Material material){
		filename = "Models/"+filename+".obj";
		System.out.println("Loading "+filename+"... ");
		
		List<Point> points = new ArrayList<>();
		List<Triangle> triangles = new ArrayList<>();
		double minX, minY, minZ, maxX, maxY, maxZ;
		minX = minY = minZ = Double.POSITIVE_INFINITY;
		maxX = maxY = maxZ = Double.NEGATIVE_INFINITY;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			List<Point> pointBuffer = new ArrayList<>();
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				char type = line.charAt(0);
				if (type == 'v'){
					if (line.charAt(1) == 'n' || line.charAt(1) == 't'){
						continue;
					}
					String[] rawValues = line.split(" ");
					Point point = new Point(new Vec3(Double.parseDouble(rawValues[1]),Double.parseDouble(rawValues[2]),Double.parseDouble(rawValues[3])),0);
					if (point.pos.x > maxX) maxX = point.pos.x;
					if (point.pos.x < minX) minX = point.pos.x;
					
					if (point.pos.y > maxY) maxY = point.pos.y;
					if (point.pos.y < minY) minY = point.pos.y;
					
					if (point.pos.z > maxZ) maxZ = point.pos.z;
					if (point.pos.z < minZ) minZ = point.pos.z;
					
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
						triangles.add(new Triangle(0, 1, 2, pointBuffer.toArray(Point[]::new), material, color));
						pointBuffer.remove(1);
					}
				}
			}
		} catch (IOException e){
			System.out.println("Failed to load");
			return new Mesh(new Triangle[0], new AABB(0, 0, 0));
		}
		System.out.println("  Loaded "+triangles.size()+" triangles");
		System.out.println("  Loaded "+points.size()+" points");
		System.out.println(filename+" successfully loaded");
		if (rescale){
			double xrange = maxX-minX;
			double yrange = maxY-minY;
			double zrange = maxZ-minZ;
			double maxRange = Math.max(Math.max(xrange,yrange),zrange);

			double scale = 1/maxRange;

			xrange *= scale;
			yrange *= scale;
			zrange *= scale;
			for (Point point : points){
				point.pos = new Vec3(
					(point.pos.x - minX)*scale-xrange/2,
					(point.pos.y - minY)*scale-yrange/2,
					(point.pos.z - minZ)*scale-zrange/2
				);
			}
		}
		List<Vec3> vecs = points.stream().map((p)->p.pos).toList();
		AABB bounds = new AABB(vecs);
		Triangle[] rTriangles = triangles.toArray(Triangle[]::new);
		return new Mesh(rTriangles, bounds);
	}
	public static Mesh loadObj(String filename, Material material){
		return loadObj(filename, true, Color.white, material);
	}
	@Override
	public void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam){
		for (Triangle tri : triangles){
			tri.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
	}
	@Override
	public Intersection getIntersection(Vec3 origin, Vec3 direction){
		if (!bounds.testIntersection(origin, direction)) return null;
		Intersection intersection = null;
		for (Triangle tri : triangles){
			Intersection localIntersection = tri.getIntersection(origin, direction);
			if (localIntersection == null) continue;
			if (intersection == null || origin.dist(intersection.pos) > origin.dist(localIntersection.pos)){
				intersection = localIntersection;
			}
		}
		return intersection;
	}
}