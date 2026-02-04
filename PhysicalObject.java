
import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;

/**
 * An abstract object which represents the Physical parts of an object
 */
public abstract class PhysicalObject {
	public final Color reflectionColor;
	public final Color emissionColor;
	public final double emissionStrength;
	public final double specularity;
	public final double transparency;
	public final double specularityChance;

	public PhysicalObject(Color color, Material material){
		switch (material){
			case SOLID -> {
				this.emissionStrength = 0;
				this.emissionColor = Color.BLACK;

				this.reflectionColor = color;
				this.specularity = 0;
				this.specularityChance = 0;
				
				this.transparency = 0;
			}
			case SHINY_SOLID -> {
				this.emissionStrength = 0;
				this.emissionColor = Color.BLACK;
				this.reflectionColor = color;
				this.specularity = 1;
				this.transparency = 0;
				this.specularityChance = 1;
			}
			case LIGHT -> {
				this.emissionStrength = 1;
				this.emissionColor = color;
				this.reflectionColor = Color.BLACK;
				this.specularity = 0;
				this.transparency = 0;
				this.specularityChance = 0;
			}
			case MIRROR -> {
				this.emissionStrength = 0;
				this.emissionColor = Color.BLACK;
				this.reflectionColor = Color.WHITE;
				this.specularity = 1;
				this.transparency = 0;
				this.specularityChance = 1;
			}
			case GLASS -> {
				this.emissionStrength = 0;
				this.emissionColor = Color.BLACK;
				this.reflectionColor = color;
				this.specularity = 0;
				this.transparency = 1;
				this.specularityChance = 0;
			}
			case null -> {
				this.emissionStrength = 0;
				this.emissionColor = Color.BLACK;
				this.reflectionColor = Color.WHITE;
				this.specularity = 0;
				this.transparency = 0;
				this.specularityChance = 0;
			}
		}
	}
	public abstract Intersection getIntersection(Vec3 origin, Vec3 direction);
	public abstract void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam);
}