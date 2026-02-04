
import java.awt.Color;
/**
 * fake enums
 */
public class Material {
	public final Color reflectionColor;
	public final Color emissionColor;
	public final double emissionStrength;
	public final double specularity;
	public final double transparency;
	public final double specularityChance;
	public static final Material SOLID  = new Material(0, Color.BLACK, Color.WHITE, .9, .1, 0);
	public static final Material LIGHT  = new Material(1, Color.WHITE, Color.BLACK, 0, 0, 0);
	public static final Material MIRROR = new Material(0, Color.BLACK, Color.WHITE, 1, 1, 0);
	public static final Material GLASS  = new Material(0, Color.BLACK, Color.WHITE, 0, 0, 1);
	public Material(double emissionStrength, Color emissionColor, Color reflectionColor, double specularity, double specularityChance, double transparency){
		this.emissionStrength = emissionStrength;
		this.emissionColor = emissionColor;

		this.reflectionColor = reflectionColor;
		this.specularity = specularity;
		this.specularityChance = specularityChance;

		this.transparency = transparency;
	}
	public static Material solid(Color color){
		return new Material(0, Color.BLACK, color, .9, .1, 0);
	}
	public static Material light(Color color){
		return new Material(1, color, Color.BLACK, 0, 0, 0);
	}
	public static Material glass(Color color){
		return new Material(0, Color.BLACK, color, 0, 0, 1);
	}
}