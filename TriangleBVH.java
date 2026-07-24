
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A bvh specifically for triangles (kinda useless)
 * TriangleBVH
 */
@Deprecated
public class TriangleBVH {
	@Deprecated
	public static final int MAX_TRIANGLES = 20;
	@Deprecated
	public static final int MAX_DEPTH = 30;
	/**
	 * The base cost of a bvh step in terms of triangles
	 */
	@Deprecated
	public static final int BASE_COST = 20;
	@Deprecated
	private TriangleBVH node0;
	@Deprecated
	private TriangleBVH node1;
	@Deprecated
	private Triangle[] triangles;
	@Deprecated
	private final AABB bounds;
	@Deprecated
	public TriangleBVH(){
		bounds = new AABB();
		node0 = null;
		node1 = null;
		triangles = null;
	}
	@Deprecated
	public TriangleBVH(List<Triangle> triangles){
		bounds = new AABB();
		bounds.grow(triangles);
		if (triangles.size() <= MAX_TRIANGLES){
			this.triangles = triangles.toArray(Triangle[]::new);
			return;
		}
		List<Triangle> side0 = new ArrayList<>();
		List<Triangle> side1 = new ArrayList<>();

		float xRange = bounds.maxX-bounds.minX;
		float yRange = bounds.maxY-bounds.minY;
		float zRange = bounds.maxZ-bounds.minZ;
		if (xRange >= yRange && xRange >= zRange){
			splitX(triangles, xRange/2+bounds.minX, side0, side1);
		} else if (yRange >= xRange && yRange >= zRange){
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
		node0 = new TriangleBVH(side0);
		node1 = new TriangleBVH(side1);
	}
	@Deprecated
	public static final float[] SPLITS = generateSplits(3);
	@Deprecated
	public static float[] generateSplits(int count){
		float[] ret = new float[count];
		for (int i = 0; i < count; i++){
			ret[i] = (i+1)/(count+1);
		}
		System.out.println(Arrays.toString(ret));
		return ret;
	}
	@Deprecated
	public TriangleBVH(List<Triangle> triangles, boolean flag){
		bounds = new AABB();
		bounds.grow(triangles);
		float best = bounds.getSurfaceArea() * (triangles.size() + BASE_COST);
		float bestSplit = -1;
		int bestAxis = -1;

		float xRange = bounds.maxX - bounds.minX;
		float yRange = bounds.maxY - bounds.minY;
		float zRange = bounds.maxZ - bounds.minZ;
		for (int axis = 0; axis < 3; axis++){
			for (float split : SPLITS){
				List<Triangle> side0 = new ArrayList<>();
				List<Triangle> side1 = new ArrayList<>();
				if (axis == 0){
					splitX(triangles, xRange * split + bounds.minX, side0, side1);
				} else if (axis == 1){
					splitY(triangles, yRange * split + bounds.minY, side0, side1);
				} else {
					splitZ(triangles, zRange * split + bounds.minZ, side0, side1);
				}
				AABB aabb0 = new AABB();
				aabb0.grow(side0);

				AABB aabb1 = new AABB();
				aabb1.grow(side1);

				float heuristic = aabb0.getSurfaceArea() * (side0.size() + BASE_COST) + (aabb1.getSurfaceArea() * (side1.size() + BASE_COST));
				if (heuristic < best){
					best = heuristic;
					bestSplit = split;
					bestAxis = axis;
				}
			}
		}
		if (bestAxis == -1){
			this.triangles = triangles.toArray(Triangle[]::new);
			return;
		}
		List<Triangle> side0 = new ArrayList<>();
		List<Triangle> side1 = new ArrayList<>();
		if (bestAxis == 0){
			splitX(triangles, bestSplit * xRange + bounds.minX, side0, side1);
		} else if (bestAxis == 1){
			splitY(triangles, bestSplit * yRange + bounds.minY, side0, side1);
		} else {
			splitZ(triangles, bestSplit * zRange + bounds.minZ, side0, side1);
		}
		node0 = new TriangleBVH(side0, flag);
		node1 = new TriangleBVH(side1, flag);
	}



	@Deprecated
	public static void splitX(List<Triangle> triangles, float split, List<Triangle> side0, List<Triangle> side1){
		for (Triangle tri : triangles){
			if (tri.center().x > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
		}
	}
	@Deprecated
	public static void splitY(List<Triangle> triangles, float split, List<Triangle> side0, List<Triangle> side1){
		for (Triangle tri : triangles){
			if (tri.center().y > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
		}
	}
	@Deprecated
	public static void splitZ(List<Triangle> triangles, float split, List<Triangle> side0, List<Triangle> side1){
		for (Triangle tri : triangles){
			if (tri.center().z > split){
				side1.add(tri);
			} else {
				side0.add(tri);
			}
		}
	}

	@Deprecated
	public Intersection getDeficientIntersection(Vec3 origin, Vec3 direction){
		if (bounds.testIntersection(origin, direction) < 0) return null;
		Intersection intersection = null;
		if (triangles == null){
			if (node0 == null || node1 == null){
				System.out.println("Strange node");
				return null;
			}
			TriangleBVH close = node0;
			TriangleBVH far = node1;
			float closeTime = close.testIntersection(origin, direction);
			float farTime = far.testIntersection(origin, direction);
			if (farTime < closeTime){
				TriangleBVH temp = close;
				close = far;
				far = temp;
				float tempTime = closeTime;
				closeTime = farTime;
				farTime = tempTime;
			}

			intersection = closeTime < 0 ? null : close.getDeficientIntersection(origin, direction);
			if (intersection == null){
				intersection = farTime < 0 ? null : far.getDeficientIntersection(origin, direction);
			} else if (intersection.pos.dist(origin) > farTime){
				Intersection localIntersection = farTime < 0 ? null : far.getDeficientIntersection(origin, direction);
				if (localIntersection != null && origin.dist(intersection.pos) > origin.dist(localIntersection.pos)){
					intersection = localIntersection;
				}
			}
		} else {
			for (Triangle tri : triangles){
				Intersection localIntersection = tri.getDeficientIntersection(origin, direction);
				if (localIntersection == null || (intersection != null && origin.dist(intersection.pos) < origin.dist(localIntersection.pos))) continue;
				intersection = localIntersection;
			}
		}
		return intersection;
	}

	@Deprecated
	public float testIntersection(Vec3 origin, Vec3 direction){
		return bounds.testIntersection(origin, direction);
	}
	@Deprecated
	public void renderWireframe(WritableRaster raster, float[][] zBuffer, Viewport camera, Transform transform, int depth) {

		if (node0 != null) {
			node0.renderWireframe(raster, zBuffer, camera, transform, depth-1);
		}
		if (node1 != null) {
			node1.renderWireframe(raster, zBuffer, camera, transform, depth-1);
		}
		if (depth < 0){
			return;
		}
		if (depth == 0 || (node0 == null && node1 == null)){
			int[] color;
			if (depth == 0){
				color = new int[] {255, 255, 255, 255};
			} else {
				color = new int[] {128, 128, 128, 128};
			}

			Vec3 p0 = new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ);
			Vec3 p1 = new Vec3(bounds.maxX, bounds.maxY, bounds.minZ);
			Vec3 p2 = new Vec3(bounds.maxX, bounds.minY, bounds.maxZ);
			Vec3 p3 = new Vec3(bounds.maxX, bounds.minY, bounds.minZ);

			Vec3 p4 = new Vec3(bounds.minX, bounds.maxY, bounds.maxZ);
			Vec3 p5 = new Vec3(bounds.minX, bounds.maxY, bounds.minZ);
			Vec3 p6 = new Vec3(bounds.minX, bounds.minY, bounds.maxZ);
			Vec3 p7 = new Vec3(bounds.minX, bounds.minY, bounds.minZ);

			p0 = transform.unapplyTo(p0);
			p1 = transform.unapplyTo(p1);
			p2 = transform.unapplyTo(p2);
			p3 = transform.unapplyTo(p3);
			p4 = transform.unapplyTo(p4);
			p5 = transform.unapplyTo(p5);
			p6 = transform.unapplyTo(p6);
			p7 = transform.unapplyTo(p7);

			// top face
			Ray.render(p0, p1, raster, zBuffer, camera, color);
			Ray.render(p1, p3, raster, zBuffer, camera, color);
			Ray.render(p3, p2, raster, zBuffer, camera, color);
			Ray.render(p2, p0, raster, zBuffer, camera, color);
			// bottom face
			Ray.render(p4, p5, raster, zBuffer, camera, color);
			Ray.render(p5, p7, raster, zBuffer, camera, color);
			Ray.render(p7, p6, raster, zBuffer, camera, color);
			Ray.render(p6, p4, raster, zBuffer, camera, color);
			// sides
			Ray.render(p0, p4, raster, zBuffer, camera, color);
			Ray.render(p1, p5, raster, zBuffer, camera, color);
			Ray.render(p2, p6, raster, zBuffer, camera, color);
			Ray.render(p3, p7, raster, zBuffer, camera, color);
		}
	}
	@Deprecated
	public int getMaxDepth(){
		if (node0 == null && node1 == null) return 0;
		int side0 = node0.getMaxDepth() + 1;
		int side1 = node1.getMaxDepth() + 1;
		return Math.max(side0, side1);
	}
	@Deprecated
	public int getMaxTriangleCount(){
		if (node0 == null && node1 == null) return triangles.length;
		int side0 = node0.getMaxTriangleCount();
		int side1 = node1.getMaxTriangleCount();
		return Math.max(side0, side1);
	}
}
