import Math.Vec3;
import java.awt.image.WritableRaster;
/**
 * Represents an Object which is able to interact with light.
 * DeficientPhysicalObject
 */
public abstract class DeficientPhysicalObject {
	/**
	 * Get the center of the object.
	 * @return
	 */
	public abstract Vec3 center();
	/**
	 * Get a deficient intersection between a ray and this object. Any passed material is expected tobe replaced
	 * @param rayOrigin
	 * @param rayDirection
	 * @return
	 */
	public abstract Intersection getDeficientIntersection(Vec3 rayOrigin, Vec3 rayDirection);
	/**
	 * Grow the AABB to fit this
	 * @param aabb
	 */
	public abstract void grow(AABB aabb);
	/**
	 * Render this object onto a raster given a certain camera position.
	 * @param raster
	 * @param zBuffer
	 * @param camera
	 * @param transform
	 * @param rgb
	 */
	public abstract void render(WritableRaster raster, float[][] zBuffer, Viewport camera, Transform transform, int[] rgb);
}