
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.List;
/**
 * A object which represents a group of triangles. Uses a BVH to speedup ray intersections
 * @extends PhysicalObject
 */
@Deprecated
public class TriangleMesh extends PhysicalObject{
	@Deprecated
	public final BVH bvh;
	@Deprecated
	public final Triangle[] triangles;
	@Deprecated
	public TriangleMesh(List<Triangle> triangles, Material material, Transform transform, boolean sahConstruction){
		super(material, transform);
		this.triangles = triangles.toArray(Triangle[]::new);
		if (sahConstruction){
			this.bvh = new BVH(triangles);
		} else {
			this.bvh = BVH.generateBVHNaive(triangles);
		}
	}
	@Deprecated
	@Override
	public void renderRasterized(WritableRaster raster, float[][] zBuffer, Viewport camera) {
		int[] color = new int[] {(int) (super.material.reflectionColor[0]*255),(int) (super.material.reflectionColor[1]*255),(int) (super.material.reflectionColor[2]*255)};
		for (Triangle tri : triangles){
			tri.render(raster, zBuffer, camera, transform, color);
		}
	}
	@Deprecated
	@Override
	public Intersection getLocalIntersection(Vec3 origin, Vec3 direction, float minDist){
		Intersection intersection = bvh.getDeficientIntersection(origin, direction, minDist);
		if (intersection == null) return null;
		intersection.material = this.material;
		return intersection;
	}
}