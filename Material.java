
import java.awt.Color;
/**
 * fake enums
 */
public class Material {
	public final float[] emissionColor;
	public final float emissionStrength;

	public final float[] reflectionColor;
	public final float specularity;
	public final float specularityChance;

	public final boolean transparent;
	public final float refractiveIndex;
	public final float absorption;

	/** A white material that emits no light. Fully diffuse reflections.*/
	public static final Material SOLID = solid(Color.WHITE);
	/** A black material that emits white light. No Reflections.*/
	public static final Material LIGHT = light(Color.WHITE);
	/** A material that reflects all light perfectly specularly.*/
	public static final Material MIRROR = new Material(0, Color.BLACK, Color.WHITE, 1, 1, false, 0, 0);
	/** A white material that emits no light. Reflects perfectly specularly 10% of incoming light.*/
	public static final Material PLASTIC = plastic(Color.WHITE);
	/** A material that emits no light. Reflects all light somewhat specularly.*/
	public static final Material METAL = metal(Color.WHITE);
	/** A white material that emits no light. Refracts all light with an index of 1.5 (glass).*/
	public static final Material GLASS = glass(Color.WHITE, 1.5f);

	private Material(float emissionStrength, Color emissionColor, Color reflectionColor, float specularity, float specularityChance, boolean transparent, float refractiveIndex, float absorption){
		this.emissionStrength = emissionStrength;
		this.emissionColor = new float[] {emissionColor.getRed()/255.0f, emissionColor.getGreen()/255.0f, emissionColor.getBlue()/255.0f};
		
		this.reflectionColor = new float[] {reflectionColor.getRed()/255.0f, reflectionColor.getGreen()/255.0f, reflectionColor.getBlue()/255.0f};
		this.specularity = specularity;
		this.specularityChance = specularityChance;

		this.transparent = transparent;
		this.refractiveIndex = refractiveIndex;
		this.absorption = absorption;
	}
	public Material(float[] color, float strength){
		this.emissionStrength = strength;
		this.emissionColor = color;

		this.reflectionColor = new float[] {0, 0, 0};
		this.specularity = 0;
		this.specularityChance = 0;

		this.transparent = false;
		this.refractiveIndex = 1;
		this.absorption = 0;
	}
	public static Material solid(Color color){
		return new Material(0, Color.BLACK, color, 0, 0, false, 0, 0);
	}
	public static Material plastic(Color color){
		return new Material(0, Color.BLACK, color, 1, .1f, false, 0, 0);
	}
	public static Material metal(Color color){
		return new Material(0, Color.BLACK, color, .5f, 1, false, 0, 0);
	}
	public static Material reflective(Color color, float specularity, float specularityChance){
		return new Material(0, Color.BLACK, color, specularity, specularityChance, false, 1, 1);
	}
	public static Material light(Color color){
		return new Material(1, color, Color.BLACK, 0, 0, false, 0, 0);
	}
	public static Material glass(Color color, float refractiveIndex){
		return new Material(0, Color.BLACK, color, 1, 0, true, refractiveIndex, 0);
	}
	public static Material frostedGlass(Color color, float refractiveIndex, float frostiness){
		return new Material(0, Color.BLACK, color,frostiness, 0, true, refractiveIndex, 0);
	}
}