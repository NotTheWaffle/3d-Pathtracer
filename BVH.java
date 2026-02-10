
import Math.Pair;
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public class BVH {
	public static final int MAX_TRIANGLES = 20;
	public static final int MAX_DEPTH = 20;
	private BVH node0;
	private BVH node1;
	private Triangle[] triangles;
	private final AABB bounds;
	public BVH(){
		bounds = new AABB();
		node0 = null;
		node1 = null;
		triangles = null;
	}
	public BVH(List<Triangle> triangles){
		bounds = new AABB();
		bounds.addTriangles(triangles);
		if (triangles.size() <= MAX_TRIANGLES){
			this.triangles = triangles.toArray(Triangle[]::new);
			return;
		}
		List<Triangle> side0 = new ArrayList<>();
		List<Triangle> side1 = new ArrayList<>();

		double xRange = bounds.maxX-bounds.minX;
		double yRange = bounds.maxY-bounds.minY;
		double zRange = bounds.maxZ-bounds.minZ;
		if (xRange > yRange && xRange > zRange){
			splitX(triangles, xRange/2+bounds.minX, side0, side1);
		} else if (yRange > xRange && yRange > zRange){
			splitY(triangles, yRange/2+bounds.minY, side0, side1);
		} else {
			splitZ(triangles, zRange/2+bounds.minZ, side0, side1);
		}

		if (side0.isEmpty()){
			this.triangles = side1.toArray(Triangle[]::new);
			return;
		}
		if (side1.isEmpty()){
			this.triangles = side0.toArray(Triangle[]::new);
			return;
		}
		node0 = new BVH(side0);
		node1 = new BVH(side1);
	}
	
	public static void splitX(List<Triangle> triangles, double split, List<Triangle> side0, List<Triangle> side1){
		for (Triangle tri : triangles){
			if (tri.center().x > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
		}
	}
	public static void splitY(List<Triangle> triangles, double split, List<Triangle> side0, List<Triangle> side1){
		for (Triangle tri : triangles){
			if (tri.center().y > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
		}
	}
	public static void splitZ(List<Triangle> triangles, double split, List<Triangle> side0, List<Triangle> side1){
		for (Triangle tri : triangles){
			if (tri.center().z > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
		}
	}
	
	public Pair<Vec3, Vec3> getIntersection(Vec3 origin, Vec3 direction){
		if (bounds.testIntersection(origin, direction) < 0) return null;
		Pair<Vec3, Vec3> intersection = null;
		if (triangles == null){
			if (node0 == null || node1 == null){
				System.out.println("wtf");
				return null;
			}
			BVH close = node0;
			BVH far = node1;
			double closeTime = close.testIntersection(origin, direction);
			double farTime = far.testIntersection(origin, direction);
			if (farTime < closeTime){
				BVH temp = close;
				close = far;
				far = temp;
				double tempTime = closeTime;
				closeTime = farTime;
				farTime = tempTime;
			}
			
			intersection = closeTime < 0 ? null : close.getIntersection(origin, direction);
			if (intersection == null){
				intersection = farTime < 0 ? null : far.getIntersection(origin, direction);
			} else if (intersection.t0.dist(origin) > farTime){
				Pair<Vec3, Vec3> localIntersection = farTime < 0 ? null : far.getIntersection(origin, direction);
				if (localIntersection != null && origin.dist(intersection.t0) > origin.dist(localIntersection.t0)){
					intersection = localIntersection;
				}
			}
		} else {
			for (Triangle tri : triangles){
				Pair<Vec3, Vec3> localIntersection = tri.getIntersection(origin, direction);
				if (localIntersection == null || (intersection != null && origin.dist(intersection.t0) < origin.dist(localIntersection.t0)) || localIntersection.t1.dot(direction) > 0) continue;
				intersection = localIntersection;
			}
		}
		return intersection;
	}
	public double testIntersection(Vec3 origin, Vec3 direction){
		return bounds.testIntersection(origin, direction.normalize());
	}
	public void render(WritableRaster raster, double[][] zBuffer, Viewport camera) {
		if (node0 != null) {
			node0.render(raster, zBuffer, camera);
		}
		if (node1 != null) {
			node1.render(raster, zBuffer, camera);
		}
		if (true){
			Vec3 p0 = new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ);
			Vec3 p1 = new Vec3(bounds.maxX, bounds.maxY, bounds.minZ);
			Vec3 p2 = new Vec3(bounds.maxX, bounds.minY, bounds.maxZ);
			Vec3 p3 = new Vec3(bounds.maxX, bounds.minY, bounds.minZ);
			
			Vec3 p4 = new Vec3(bounds.minX, bounds.maxY, bounds.maxZ);
			Vec3 p5 = new Vec3(bounds.minX, bounds.maxY, bounds.minZ);
			Vec3 p6 = new Vec3(bounds.minX, bounds.minY, bounds.maxZ);
			Vec3 p7 = new Vec3(bounds.minX, bounds.minY, bounds.minZ);
			// top face
			Ray.render(p0, p1, raster, zBuffer, camera);
			Ray.render(p1, p3, raster, zBuffer, camera);
			Ray.render(p3, p2, raster, zBuffer, camera);
			Ray.render(p2, p0, raster, zBuffer, camera);
			// bottom face
			Ray.render(p4, p5, raster, zBuffer, camera);
			Ray.render(p5, p7, raster, zBuffer, camera);
			Ray.render(p7, p6, raster, zBuffer, camera);
			Ray.render(p6, p4, raster, zBuffer, camera);
			// sides
			Ray.render(p0, p4, raster, zBuffer, camera);
			Ray.render(p1, p5, raster, zBuffer, camera);
			Ray.render(p2, p6, raster, zBuffer, camera);
			Ray.render(p3, p7, raster, zBuffer, camera);
		}
	}
}
