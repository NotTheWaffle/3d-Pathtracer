
import Math.Vec3;
import java.awt.image.WritableRaster;

/**
 * An abstract object which represents the Physical parts of an object
 */
public abstract class PhysicalObject {
	public final static double EPSILON = 1e-8;
	public final double[] reflectionColor;
	public final double[] emissionColor;
	public final double emissionStrength;
	public final double specularity;
	public final double transparency;
	public final double specularityChance;

	public PhysicalObject(Material material){
		this.reflectionColor = material.reflectionColor;
		this.emissionColor = material.emissionColor;
		this.emissionStrength = material.emissionStrength;
		this.specularity = material.specularity;
		this.transparency = material.transparency;
		this.specularityChance = material.specularityChance;
	}
	public abstract Intersection getIntersection(Vec3 origin, Vec3 direction);
	public abstract void render(WritableRaster raster, double[][] zBuffer, Viewport camera);
}