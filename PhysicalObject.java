
import Math.Vec3;
import java.awt.image.WritableRaster;

/**
 * An object with a material and transform
 */
public abstract class PhysicalObject {
	public final static float EPSILON = 1e-4f;
	public final Material material;
	public final Transform transform;

	public PhysicalObject(Material material){
		this(material, new Transform());
	}
	public PhysicalObject(Material material, Transform transform){
		this.material = material;
		this.transform = transform;
	}
	/**
	 * Gets an absolute intersection
	 * @param rayOrigin
	 * @param rayDirection
	 * @param minDist
	 * @return
	 */
	public final Intersection getTransformedintersection(Vec3 rayOrigin, Vec3 rayDirection, float minDist){
		Intersection intersection = getLocalIntersection(transform.applyTo(rayOrigin), transform.inv.mul(rayDirection), minDist);
		if (intersection == null) return null;
		intersection.pos = transform.unapplyTo(intersection.pos);
		intersection.normal = transform.rot.mul(intersection.normal);
		return intersection;
	}
	/**
	 * gets local intersection data with provided ray assuming a position of (0, 0, 0)
	 * doesn't gaurentee its closer than minDist, but passing a correct value can speed up the method for certain implementations
	 * @return null if no hit occured, some Intersection otherwise
	 */
	protected abstract Intersection getLocalIntersection(Vec3 rayOrigin, Vec3 rayDirection, float minDist);
	public abstract void renderRasterized(WritableRaster raster, float[][] zBuffer, Viewport camera);
}