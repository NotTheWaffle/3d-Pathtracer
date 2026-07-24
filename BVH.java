
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * A binary BVH capable of holding general objects and utilizing AABBs.
 */
public class BVH {
	/**
	 * A base cost of a bvh in terms of objects. Used to account for: aabb intersection, method overheads, etc...
	 * not very well tuned. smaller values make deeper bvhs, and vice versa
	 */
	public static final int BASE_COST = 20;
	private BVH node0;
	private BVH node1;
	private DeficientPhysicalObject[] objects;
	private AABB bounds;
	public BVH(){
		bounds = new AABB();
		node0 = null;
		node1 = null;
		objects = null;
	}
	public static final float[] SPLITS;
	static {
		int count = 3;
		SPLITS = new float[count];
		for (int i = 0; i < count; i++) SPLITS[i] = (i+1f)/(count+1);
	}
	/**
	 * Constructs a BVH containing the specified objects utilizing a surface area heuristic.
	 * @param objects
	 */
	public BVH(List<? extends DeficientPhysicalObject> objects){
		bounds = new AABB();
		bounds.grow(objects);
		float best = bounds.getSurfaceArea() * (objects.size() + BASE_COST);
		float bestSplit = -1;
		int bestAxis = -1;

		float xRange = bounds.maxX - bounds.minX;
		float yRange = bounds.maxY - bounds.minY;
		float zRange = bounds.maxZ - bounds.minZ;
		for (int axis = 0; axis < 3; axis++){
			for (float split : SPLITS){
				List<DeficientPhysicalObject> side0 = new ArrayList<>();
				List<DeficientPhysicalObject> side1 = new ArrayList<>();
				switch (axis){
					case 0 -> splitX(objects, xRange * split + bounds.minX, side0, side1);
					case 1 -> splitY(objects, yRange * split + bounds.minY, side0, side1);
					case 2 -> splitZ(objects, zRange * split + bounds.minZ, side0, side1);
				}
				AABB aabb0 = new AABB();
				aabb0.grow(side0);

				AABB aabb1 = new AABB();
				aabb1.grow(side1);

				float heuristic = (aabb0.getSurfaceArea() * (side0.size() + BASE_COST)) + (aabb1.getSurfaceArea() * (side1.size() + BASE_COST));

				if (heuristic < best){
					best = heuristic;
					bestSplit = split;
					bestAxis = axis;
				}
			}
		}
		if (bestAxis == -1){
			// if all splits make the heuristic worse we dont split
			this.objects = objects.toArray(DeficientPhysicalObject[]::new);
			node0 = node1 = null;
			return;
		}
		List<DeficientPhysicalObject> side0 = new ArrayList<>();
		List<DeficientPhysicalObject> side1 = new ArrayList<>();
		switch (bestAxis) {
			case 0 -> splitX(objects, bestSplit * xRange + bounds.minX, side0, side1);
			case 1 -> splitY(objects, bestSplit * yRange + bounds.minY, side0, side1);
			case 2 -> splitZ(objects, bestSplit * zRange + bounds.minZ, side0, side1);
		}
		node0 = new BVH(side0);
		node1 = new BVH(side1);
	}


	/**
	 * Naive algorithm which generates a worse bvh, but faster. Simply splits along the longest axis until a specified amount of objects are in each bvh
	 * @param objects
	 * @return
	 */
	public static BVH generateBVHNaive(List<? extends DeficientPhysicalObject> objects){
		final int MAX_OBJECTS = 20;
		BVH bvh = new BVH();
		bvh.bounds = new AABB();
		bvh.bounds.grow(objects);
		if (objects.size() <= MAX_OBJECTS){
			bvh.objects = objects.toArray(DeficientPhysicalObject[]::new);
			bvh.node0 = bvh.node1 = null;
			return bvh;
		}
		List<DeficientPhysicalObject> side0 = new ArrayList<>();
		List<DeficientPhysicalObject> side1 = new ArrayList<>();

		float xRange = bvh.bounds.maxX - bvh.bounds.minX;
		float yRange = bvh.bounds.maxY - bvh.bounds.minY;
		float zRange = bvh.bounds.maxZ - bvh.bounds.minZ;
		if (xRange >= yRange && xRange >= zRange){
			splitX(objects, xRange/2+bvh.bounds.minX, side0, side1);
		} else if (yRange >= xRange && yRange >= zRange){
			splitY(objects, yRange/2+bvh.bounds.minY, side0, side1);
		} else {
			splitZ(objects, zRange/2+bvh.bounds.minZ, side0, side1);
		}

		if (side0.isEmpty()){
			bvh.objects = side1.toArray(DeficientPhysicalObject[]::new);
			bvh.node0 = bvh.node1 = null;
			return bvh;
		}
		if (side1.isEmpty()){
			bvh.objects = side0.toArray(DeficientPhysicalObject[]::new);
			bvh.node0 = bvh.node1 = null;
			return bvh;
		}
		bvh.node0 = generateBVHNaive(side0);
		bvh.node1 = generateBVHNaive(side1);
		return bvh;
	}

	/**
	 * Splits the objects in {@code objects} between {@code side0} and {@code side1} based on their {@code center().x} relative to {@code split}
	 * @param objects
	 * @param split
	 * @param side0
	 * @param side1
	 */
	public static void splitX(List<? extends DeficientPhysicalObject> objects, float split, List<? super DeficientPhysicalObject> side0, List<? super DeficientPhysicalObject> side1){
		for (DeficientPhysicalObject object : objects){
			if (object.center().x > split){
				side1.add(object);
			} else {
				side0.add(object);
			}
		}
	}
	/**
	 * Splits the objects in {@code objects} between {@code side0} and {@code side1} based on their {@code center().x} relative to {@code split}
	 * @param objects
	 * @param split
	 * @param side0
	 * @param side1
	 */
	public static void splitY(List<? extends DeficientPhysicalObject> objects, float split, List<? super DeficientPhysicalObject> side0, List<? super DeficientPhysicalObject> side1){
		for (DeficientPhysicalObject object : objects){
			if (object.center().y > split){
				side1.add(object);
			} else {
				side0.add(object);
			}
		}
	}
	/**
	 * Splits the objects in {@code objects} between {@code side0} and {@code side1} based on their {@code center().x} relative to {@code split}
	 * @param objects
	 * @param split
	 * @param side0
	 * @param side1
	 */
	public static void splitZ(List<? extends DeficientPhysicalObject> objects, float split, List<? super DeficientPhysicalObject> side0, List<? super DeficientPhysicalObject> side1){
		for (DeficientPhysicalObject object : objects){
			if (object.center().z > split){
				side1.add(object);
			} else {
				side0.add(object);
			}
		}
	}

	/**
	 * A public version which requries bounds checks.
	 * @param origin
	 * @param direction
	 * @param minDist
	 * @return an Instance of {@code Intersection} containing the hit location, and normal, or {@code null} if no intersection was found
	 */
	public Intersection getDeficientIntersection(Vec3 origin, Vec3 direction, float minDist){
		return getDeficientIntersection(origin, direction, minDist, true);
	}
	private Intersection getDeficientIntersection(Vec3 origin, Vec3 direction, float minDist, boolean doBoundsCheck){
		if (doBoundsCheck && bounds.testIntersection(origin, direction) > minDist) {
			// don't bother checking subnodes or triangles if our intersection is after an already found one
			return null;
		}
		Intersection intersection = null;
		if (objects == null){
			// not a leaf node
			if (node0 == null || node1 == null){
				System.out.println("Strange Node");
				return null;
			}
			BVH close = node0;
			BVH far = node1;
			float closeTime = close.bounds.testIntersection(origin, direction);
			float farTime = far.bounds.testIntersection(origin, direction);
			// swap the close and far nodes if they are reversed
			if (farTime < closeTime){
				BVH temp = close;
				close = far;
				far = temp;
				float tempTime = closeTime;
				closeTime = farTime;
				farTime = tempTime;
			}

			// closetime < 0 represents not colliding
			intersection = closeTime < 0 ? null : close.getDeficientIntersection(origin, direction, minDist, false);
			if (intersection == null){
				// same here
				intersection = farTime < 0 ? null : far.getDeficientIntersection(origin, direction, minDist, false);
			} else if (intersection.pos.dist(origin) > farTime){
				// if the closest point of the far box is earlier than the collision found in the first box we must check the far box
				// this only matters when two bvhs overlap in volume
				Intersection localIntersection = farTime < 0 ? null : far.getDeficientIntersection(origin, direction, minDist, false);
				if (localIntersection != null && origin.dist(intersection.pos) > origin.dist(localIntersection.pos)){
					intersection = localIntersection;
				}
			}
		} else {
			// leaf nodes just check their children
			for (DeficientPhysicalObject tri : objects){
				Intersection localIntersection = tri.getDeficientIntersection(origin, direction);
				if (localIntersection == null || (intersection != null && origin.dist(intersection.pos) < origin.dist(localIntersection.pos))) continue;
				intersection = localIntersection;
			}
		}
		return intersection;
	}
	/**
	 * Recursively renders a wireframe of the BVH structure.
	 * @param raster
	 * @param zBuffer
	 * @param camera
	 * @param transform
	 * @param depth
	 */
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
		if (depth >= 0 || (node0 == null && node1 == null)){
			int[] color;
			if (depth == 0){
				color = new int[] {255, 255, 255, 255};
			} else if (depth < 0){
				color = new int[] {128, 128, 128, 128};
			} else {
				color = new int[] {32, 32, 32, 32};
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
	/**
	 * Gets the max depth of this BVH node
	 * @return the depth of this bvh node, which is the max of the depth of its children + 1
	 */
	public int getMaxDepth(){
		if (node0 == null && node1 == null) return 0;
		int side0 = node0.getMaxDepth() + 1;
		int side1 = node1.getMaxDepth() + 1;
		return Math.max(side0, side1);
	}
	/**
	 * Returns the max amount of objects in any node of this bvh.
	 * @return the max amount of objects in any node of this bvh, which is the max of its children.
	 */
	public int getMaxObjectCount(){
		if (node0 == null && node1 == null) return objects.length;
		int side0 = node0.getMaxObjectCount();
		int side1 = node1.getMaxObjectCount();
		return Math.max(side0, side1);
	}
	/**
	 * Returns the total amount of objects in this node and the children of this bvh.
	 * @return
	 */
	public int getTotalObjects(){
		if (node0 == null || node1 == null) return objects.length;
		return node0.getTotalObjects() + node1.getTotalObjects();
	}

	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof BVH bvh){
			if (node0 == null || node1 == null){
				return bvh.bounds.equals(bounds) && Arrays.equals(bvh.objects, objects);
			} else {
				return bvh.node0.equals(node0) && bvh.node1.equals(node1) && bvh.bounds.equals(bounds);
			}
		} else {
			return false;
		}
	}

	@Override
	public int hashCode(){
		if (node0 == null || node1 == null){
			return bounds.hashCode() ^ Arrays.hashCode(objects);
		} else {
			return node0.hashCode() ^ node1.hashCode() ^ bounds.hashCode();
		}
	}

	@Override
	public String toString(){
		return "BVH node of depth: "+getMaxDepth()+" and "+getTotalObjects()+" objects";
	}
}
