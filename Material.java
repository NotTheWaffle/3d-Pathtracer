
import java.awt.Color;
import java.util.Arrays;
/**
 * A mildly accurate representation of a real material. Contains specular and diffuse reflections, emission colors, and refraction.
 * Material
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
	public static final Material LAMBERTIAN = lambertian(Color.WHITE);
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
	/** A white material that emits no light. Refracts most light with an index of 1.5 (glass), reflects small amounts to appear "frosted" */
	public static final Material FROSTED_GLASS = frostedGlass(Color.WHITE, 1.5f, .95f);
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
	public static Material lambertian(Color color){
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
		return new Material(0, Color.BLACK, color, frostiness, 0, true, refractiveIndex, 0);
	}
	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof Material mat){
			return
				mat.transparent == transparent
				&& mat.emissionStrength == emissionStrength
				&& (!transparent || mat.absorption == absorption)
				&& (!transparent || mat.refractiveIndex == refractiveIndex)
				&& (specularityChance == 0 || mat.specularity == specularity)
				&& (specularity == 0 || mat.specularityChance == specularityChance)
				&& (emissionStrength == 0 || Arrays.equals(mat.emissionColor, emissionColor))
				&& (emissionStrength == 1 || specularityChance == 1 || Arrays.equals(reflectionColor, reflectionColor));
		} else {
			return false;
		}
	}

	@Override
	public int hashCode(){
		int hashCode = 0;
		hashCode ^= Float.hashCode(specularity);
		hashCode ^= Float.hashCode(emissionStrength);
		if (!transparent) hashCode ^= Float.hashCode(absorption);
		if (!transparent) hashCode ^= Float.hashCode(refractiveIndex);
		if (specularity > 0) hashCode ^= Float.hashCode(specularityChance);
		if (specularityChance > 0) hashCode ^= Float.hashCode(specularity);
		if (emissionStrength > 0) hashCode ^= Arrays.hashCode(emissionColor);
		if (emissionStrength < 1 && specularityChance < 1) hashCode ^= Arrays.hashCode(reflectionColor);
		return hashCode;
	}

	@Override
	public String toString(){
		if (transparent){
			return "Transparent Material with IOR: "+refractiveIndex+" and absorption: "+absorption;
		} else if (emissionStrength > 0){
			return "Emissive Material with color "+Arrays.toString(emissionColor);
		} else if (specularity == 1 && specularityChance == 1) {
			return "Mirror Material";
		} else {
			return "Diffuse material with color"+Arrays.toString(emissionColor);
		}
	}
}