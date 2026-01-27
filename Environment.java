
import Math.Vec3;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Environment{
	public final Mesh mesh;
	public final List<Sphere> points;
	public final List<Sphere> lights;
	public Environment(String filename){
		this.mesh = loadMesh("Models/"+filename+".obj").mesh;
		
		this.points = new ArrayList<>();
		this.lights = new ArrayList<>();
	}
	public Environment(){
		this.mesh = new Mesh(new Vec3[0], new Triangle[0]);
		this.points = new ArrayList<>();
		this.lights = new ArrayList<>();
	}
	public Environment(Mesh mesh){
		this.mesh = mesh;
		this.points = new ArrayList<>();
		this.lights = new ArrayList<>();
	}
	public static Environment loadMesh(String filename){
		System.out.println("Loading "+filename+"... ");
		List<Vec3> points = new ArrayList<>();
		List<Triangle> triangles = new ArrayList<>();
		double minX, minY, minZ, maxX, maxY, maxZ;
		minX = minY = minZ = Double.POSITIVE_INFINITY;
		maxX = maxY = maxZ = Double.NEGATIVE_INFINITY;
		
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
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
		} catch (IOException e){
			System.out.println("Failed to load");
			return new Environment(new Mesh(new Vec3[0], new Triangle[0]));
		}
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
		return new Environment(new Mesh(rPoints, rTriangles));
	}
}