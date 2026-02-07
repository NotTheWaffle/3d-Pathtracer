
import Math.Pair;
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * A object which represents a group of triangles. Uses a BVH to speedup ray intersections
 * @extends PhysicalObject
 */
public class Mesh extends PhysicalObject{
	public final BVH bvh;
	public final Triangle[] triangles;
	public Mesh(Triangle[] triangles, BVH bounds, Material material){
		super(material);
		this.triangles = triangles;
		this.bvh = bounds;
	}
	public Mesh(List<Triangle> triangles, Material material){
		super(material);
		this.triangles = triangles.toArray(Triangle[]::new);
		this.bvh = new BVH(triangles);
	}
	public static Mesh rectangle(double x, double y, double z, double width, Material material){
		double radius = width/2;
		List<Triangle> tris = List.of(
			new Triangle(
				new Vec3(x+radius, y, z-radius),
				new Vec3(x-radius, y, z-radius),
				new Vec3(x-radius, y, z+radius)
			),
			new Triangle(
				new Vec3(x-radius, y, z+radius),
				new Vec3(x+radius, y, z+radius),
				new Vec3(x+radius, y, z-radius)
			)
		);
		return new Mesh(tris, material);
	}
	//prolly too many overloads
	public static Mesh loadObj(String filename){
		return loadObj(filename, Material.SOLID);
	}
	public static Mesh loadObj(String filename, Material material){
		return loadObj(filename, 0, material);
	}
	public static Mesh loadObj(String filename, double size, Material material){
		return loadObj(filename, new Transform(), size, material);
	}
	public static Mesh loadObj(String filename, double x, double y, double z, double size, Material material){
		return loadObj(filename, new Transform().translate(x, y, z), size, material);
	}
	public static Mesh loadObj(String filename, Transform transform, double size, Material material){
		return loadObj(filename, transform, size, material, true);
	}
	public static Mesh loadObj(String filename, Transform transform, double size, Material material, boolean maintainAspectratio){
		System.out.println("Loading "+filename+"... ");
		
		List<Vec3> points = new ArrayList<>();
		List<IndexedTriangle> indexedTriangles = new ArrayList<>();
		double minX, minY, minZ, maxX, maxY, maxZ;
		minX = minY = minZ = Double.POSITIVE_INFINITY;
		maxX = maxY = maxZ = Double.NEGATIVE_INFINITY;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			List<Integer> pointBuffer = new ArrayList<>();
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				char type = line.charAt(0);
				if (type == 'v'){
					if (line.charAt(1) == 'n' || line.charAt(1) == 't'){
						continue;
					}
					String[] rawValues = line.split(" ");
					Vec3 point = new Vec3(Double.parseDouble(rawValues[1]), Double.parseDouble(rawValues[2]), Double.parseDouble(rawValues[3]));
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
						pointBuffer.add(index-1);
					}
					while (pointBuffer.size()>2){
						indexedTriangles.add(new IndexedTriangle((int)pointBuffer.get(0), (int)pointBuffer.get(1), (int)pointBuffer.get(2)));
						pointBuffer.remove(1);
					}
				}
			}
		} catch (IOException e){
			System.out.println("Failed to load");
			return new Mesh(new ArrayList<>(), Material.SOLID);
		}
		System.out.println("  Loaded "+indexedTriangles.size()+" triangles");
		System.out.println("  Loaded "+points.size()+" points");
		System.out.println(filename+" successfully loaded");
		
		double xrange = maxX-minX;
		double yrange = maxY-minY;
		double zrange = maxZ-minZ;
		double maxRange = Math.max(Math.max(xrange,yrange),zrange);


		double xscale, yscale, zscale;
		if (maintainAspectratio){
			double scale = (size == 0 ? 1 : size/maxRange);
			xscale = yscale = zscale = scale;
		} else {
			xscale = (size == 0 ? 1 : size/xrange);
			yscale = (size == 0 ? 1 : size/yrange);
			zscale = (size == 0 ? 1 : size/zrange);
		}


		xrange *= xscale;
		yrange *= yscale;
		zrange *= zscale;
		for (int i = 0; i < points.size(); i++){
			Vec3 p = points.get(i);
			points.set(i, transform.applyTo(
					new Vec3(
					(p.x - minX)*xscale-xrange/2,
					(p.y - minY)*yscale-yrange/2,
					(p.z - minZ)*zscale-zrange/2
				)
			));
		}
		
		
		List<Triangle> triangles = new ArrayList<>(indexedTriangles.size());
		for (int i = 0; i < indexedTriangles.size(); i++){
			IndexedTriangle itri = indexedTriangles.get(i);
			triangles.add(new Triangle(itri.i1, itri.i2, itri.i3, points));
		}

		try (BufferedWriter writer = new BufferedWriter(new FileWriter("compact.obj"))){
			for (Vec3 point : points){
				writer.write(String.format("v %.6f %.6f %.6f%n", point.x, point.y, point.z));
			}
			for (IndexedTriangle itri : indexedTriangles){
				writer.write(String.format(
					"f %d %d %d%n",
					itri.i1 + 1,
					itri.i2 + 1,
					itri.i3 + 1
				));
			}
		} catch (IOException e){
			System.out.println("faile");
		}

		return new Mesh(triangles, material);
	}
	@Override
	public void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam){
		for (Triangle tri : triangles){
			tri.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
	}
	@Override
	public Intersection getIntersection(Vec3 origin, Vec3 direction){
		Pair<Vec3, Vec3> intersection = bvh.getIntersection(origin, direction);
		if (intersection == null) return null;
		return new Intersection(intersection.t0, this, intersection.t1);
	}
	private static class IndexedTriangle{
		public final int i1;
		public final int i2;
		public final int i3;
		public IndexedTriangle(int i1, int i2, int i3){
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
		}
	}
}