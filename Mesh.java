
import Math.Vec3;
import java.awt.image.WritableRaster;
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
	@Override
	public void render(WritableRaster raster, double[][] zBuffer, Viewport camera) {
		for (Triangle tri : triangles){
			tri.render(raster, zBuffer, camera);
		}
	}
	@Override
	public Intersection getIntersection(Vec3 origin, Vec3 direction){
		Intersection intersection = bvh.getIntersection(origin, direction);
		if (intersection == null) return null;
		return new Intersection(intersection.pos, this, intersection.normal, intersection.backface);
	}
}