
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public class BVH {
	public static final int MAX_TRIANGLES = 10;
	public static final int MAX_DEPTH = 30;
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
	
	public Intersection getIntersection(Vec3 origin, Vec3 direction){
		if (bounds.testIntersection(origin, direction) < 0) return null;
		Intersection intersection = null;
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
			} else if (intersection.pos.dist(origin) > farTime){
				Intersection localIntersection = farTime < 0 ? null : far.getIntersection(origin, direction);
				if (localIntersection != null && origin.dist(intersection.pos) > origin.dist(localIntersection.pos)){
					intersection = localIntersection;
				}
			}
		} else {
			for (Triangle tri : triangles){
				Intersection localIntersection = tri.getIntersection(origin, direction);
				if (localIntersection == null || (intersection != null && origin.dist(intersection.pos) < origin.dist(localIntersection.pos))) continue;
				intersection = localIntersection;
			}
		}
		return intersection;
	}
	public double testIntersection(Vec3 origin, Vec3 direction){
		return bounds.testIntersection(origin, direction.normalize());
	}
	public void render(WritableRaster raster, double[][] zBuffer, Viewport camera) {
		if (null == null) return;
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
