
import Math.Vec3;
import java.awt.image.WritableRaster;

/**
 * An abstract object which represents the Physical parts of an object
 */
public abstract class PhysicalObject {
	public final double[] reflectionColor;
	public final double[] emissionColor;
	public final double emissionStrength;
	public final double specularity;
	public final double transparency;
	public final double specularityChance;

	public PhysicalObject(Material material){
		this.reflectionColor = new double[] {material.reflectionColor.getRed()/255.0, material.reflectionColor.getGreen()/255.0, material.reflectionColor.getBlue()/255.0};
		this.emissionColor =new double[] {material.emissionColor.getRed()/255.0, material.emissionColor.getGreen()/255.0, material.emissionColor.getBlue()/255.0};
		this.emissionStrength = material.emissionStrength;
		this.specularity = material.specularity;
		this.transparency = material.transparency;
		this.specularityChance = material.specularityChance;
	}
	public abstract Intersection getIntersection(Vec3 origin, Vec3 direction);
	public abstract void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam);
}