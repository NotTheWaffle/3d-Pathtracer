
import Math.Pair;
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;

public class BVH {
	public static final int MAX_TRIANGLES = 20;
	public static final int MAX_DEPTH = 10;
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
		double split = (bounds.maxY+bounds.minY)/2;
		List<Triangle> side0 = new ArrayList<>();
		List<Triangle> side1 = new ArrayList<>();
		for (Triangle tri : triangles){
			if (tri.center().y > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
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
	
	public BVH(Triangle[] triangles){
		bounds = new AABB();
		bounds.addTriangles(triangles);
		if (triangles.length <= MAX_TRIANGLES){
			this.triangles = triangles;
			return;
		}
		double split = (bounds.maxY+bounds.minY)/2;
		List<Triangle> side0 = new ArrayList<>();
		List<Triangle> side1 = new ArrayList<>();
		for (Triangle tri : triangles){
			if (tri.center().y > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
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
	public Pair<Vec3, Vec3> getIntersection(Vec3 origin, Vec3 direction){
		if (!bounds.testIntersection(origin, direction)) return null;
		Pair<Vec3, Vec3> intersection = null;
		if (node0 != null) {
			intersection = node0.getIntersection(origin, direction);
		}
		if (node1 != null){
			Pair<Vec3, Vec3> localIntersection = node1.getIntersection(origin, direction);
			if (localIntersection != null && (intersection == null || origin.dist(intersection.t0) > origin.dist(localIntersection.t0))){
				intersection = localIntersection;
			}
		}
		if (triangles != null){
			for (Triangle tri : triangles){
				Pair<Vec3, Vec3> localIntersection = tri.getIntersection(origin, direction);
				if (localIntersection == null) continue;
				if (intersection == null || origin.dist(intersection.t0) > origin.dist(localIntersection.t0)){
					intersection = localIntersection;
				}
			}
		}
		return intersection;
	}
	public void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam) {
		if (node0 != null) {
			node0.render(raster, focalLength, cx, cy, zBuffer, cam);
		}
		if (node1 != null) {
			node1.render(raster, focalLength, cx, cy, zBuffer, cam);
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
			Ray.render(p0, p1, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p1, p3, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p3, p2, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p2, p0, raster, focalLength, cx, cy, zBuffer, cam);
			// bottom face
			Ray.render(p4, p5, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p5, p7, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p7, p6, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p6, p4, raster, focalLength, cx, cy, zBuffer, cam);
			// sides
			Ray.render(p0, p4, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p1, p5, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p2, p6, raster, focalLength, cx, cy, zBuffer, cam);
			Ray.render(p3, p7, raster, focalLength, cx, cy, zBuffer, cam);
		}
	}
}
