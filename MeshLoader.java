
import Math.Pair;
import Math.Vec3;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeshLoader{
	private MeshLoader(){}
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
		compact(filename);
		System.out.println("Loading "+filename+"... ");
		
		Pair<List<Vec3>, List<IndexedTriangle>> pair = readFile(filename);
		List<Vec3> points = pair.t0;
		List<IndexedTriangle> indexedTriangles = pair.t1;
		AABB bounds = new AABB(points);


		
		System.out.println("  Loaded "+indexedTriangles.size()+" triangles");
		System.out.println("  Loaded "+points.size()+" points");
		System.out.println(filename+" successfully loaded");
		
		double xrange = bounds.maxX-bounds.minX;
		double yrange = bounds.maxY-bounds.minY;
		double zrange = bounds.maxZ-bounds.minZ;
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
					(p.x - bounds.minX)*xscale-xrange/2,
					(p.y - bounds.minY)*yscale-yrange/2,
					(p.z - bounds.minZ)*zscale-zrange/2
				)
			));
		}
		
		
		List<Triangle> triangles = new ArrayList<>(indexedTriangles.size());
		for (int i = 0; i < indexedTriangles.size(); i++){
			IndexedTriangle itri = indexedTriangles.get(i);
			triangles.add(new Triangle(itri.i1, itri.i2, itri.i3, points));
		}

		return new Mesh(triangles, material);
	}
	private static Pair<List<Vec3>, List<IndexedTriangle>> readFile(String filename){
		List<Vec3> points = new ArrayList<>();
		List<IndexedTriangle> indexedTriangles = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			List<Integer> pointBuffer = new ArrayList<>();
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				char type = line.charAt(0);
				if (type == 'v'){
					if (line.charAt(1) == 'n' || line.charAt(1) == 't') continue;
					
					String[] rawValues = line.split(" ");
					
					points.add(new Vec3(Double.parseDouble(rawValues[1]), Double.parseDouble(rawValues[2]), Double.parseDouble(rawValues[3])));
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
			System.out.println("Failed to load "+filename);
		}
		return new Pair<>(points, indexedTriangles);
	}
	public static void transform(String filename, Transform transform){

	}
	public static boolean compact(String filename){
		System.out.println("Compacting "+filename);
		List<Vec3> points = new ArrayList<>();
		List<int[]> faces = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			List<String> lines = reader.lines().toList();
			if (lines.get(0).equals("# compacted")){
				System.out.println(filename+" is already compact");
				return true;
			}
			for (String line : lines){
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;

				char type = line.charAt(0);
				if (type == 'v'){
					if (line.charAt(1) == 'n' || line.charAt(1) == 't'){
						// skip the texture and normal data
						continue;
					}
					String[] rawValues = line.split(" ");

					points.add(new Vec3(Double.parseDouble(rawValues[1]), Double.parseDouble(rawValues[2]), Double.parseDouble(rawValues[3])));
				} else if (type == 'f'){
					String[] facePointsRaw = line.split(" ");
					int[] facePoints = new int[facePointsRaw.length-1];
					for (int i = 0; i < facePoints.length; i++){
						facePoints[i] = Integer.parseInt(facePointsRaw[i+1].split("/")[0]);
					}
					faces.add(facePoints);
				}
			}
		} catch (IOException e) {
			System.out.println("Failed to read "+filename);
			return false;
		}
		System.out.println("Compacting "+filename);
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
			writer.write("# compacted\n");
			for (Vec3 point : points){
				writer.write(String.format("v %.6f %.6f %.6f%n", point.x, point.y, point.z));
			}
			StringBuilder str = new StringBuilder();
			for (int[] face : faces){
				str.append('f').append(' ');
				for (int point : face){
					str.append(point);
					str.append(' ');
				}
				str.deleteCharAt(str.length()-1);
				str.append('\n');
			}
			writer.write(str.toString());
		} catch (IOException e){
			System.out.println("Failed to save "+filename);
		}
		return false;
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