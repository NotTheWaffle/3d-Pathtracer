
import Math.FloatMath;
import Math.Vec3;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

//TODO add writeout to a json
/**
 * A collection of objects which makeup a scene. Contains a list of objects and a background.
 * Environment
 */
public class Environment{
	public static final int MAX_OBJECT_COUNT = 256;
	public static final int VOID = 0;
	public static final int SUN = 1;
	public static final int SKY = 2;

	private final int backgroundType;

	public final List<PhysicalObject> physicalObjects;


	public Environment(){
		this(VOID);
	}
	public Environment(int backGroundType){
		physicalObjects = new ArrayList<>();
		this.backgroundType = backGroundType;
	}

	public void addCornellBox(float innerWidth, float outerWidth){
		//bottom
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, -outerWidth/2, -innerWidth/2, -outerWidth/2, outerWidth/2, Material.lambertian(Color.white)));
		//left
		add(new RectangularPrism(-outerWidth/2, -innerWidth/2, -innerWidth/2, innerWidth/2, -outerWidth/2, innerWidth/2, Material.lambertian(Color.RED)));
		//right
		add(new RectangularPrism(innerWidth/2, outerWidth/2, -innerWidth/2, innerWidth/2, -outerWidth/2, innerWidth/2, Material.lambertian(Color.GREEN)));
		//back
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, -innerWidth/2, innerWidth/2, outerWidth/2, innerWidth/2, Material.lambertian(Color.WHITE)));
		//ceiling
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, outerWidth/2, innerWidth/2, -outerWidth/2, outerWidth/2, Material.lambertian(Color.WHITE)));
		//light
		add(new RectangularPrism(-innerWidth/4, innerWidth/4, (innerWidth/2-innerWidth/16), innerWidth/2, -innerWidth/4, innerWidth/4, Material.LIGHT));
	}
	public void addSphereTest(){
		add(new Sphere(new Vec3(0, 0, 2.5f), 1, Material.lambertian(Color.RED)));
		add(new Sphere(new Vec3(2.5f, 0, 0), 1, Material.lambertian(Color.BLUE)));
		add(new Sphere(new Vec3(-2.5f, 0, 0), 1, Material.lambertian(Color.GREEN)));
		addFloor();
	}
	public void addFloor(){
		add(new RectangularPrism(0, -1.5f, 0, 20, 1, 20, Material.LAMBERTIAN, 0));
	}
	public void addHueSpheres(int count, float radius){
		for (int i = 0; i < count; i++){
			Color color = new Color(Color.HSBtoRGB((float)i/count, 1, 1));
			add(new Sphere(new Transform().rotateY(i*2*FloatMath.PI/count).applyTo(new Vec3(3, 0, 0)), radius, Material.light(color)));
		}
	}

	/**
	 * Adds an object to this environment
	 * @param object
	 */
	public void add(PhysicalObject object){
		if (physicalObjects.size() < MAX_OBJECT_COUNT) physicalObjects.add(object);
	}

	/**
	 * Returns a intersection containing a material representing the light collected from the background.
	 * @param rayDirection
	 * @return
	 */
	public Intersection getBackgroundEnvironment(Vec3 rayDirection){
		return switch (backgroundType){
			case VOID -> {
				yield null;
			}
			case SUN -> {
				if (rayDirection.dot(SUN_VEC) > .9){
					yield new Intersection(Vec3.ZERO_VEC, Material.LIGHT, Vec3.ZERO_VEC, false, Vec3.ZERO_VEC);
				}
				yield null;
			}
			case SKY -> {
				yield new Intersection(Vec3.ZERO_VEC, new Material(computeSkyColor(rayDirection), 1), Vec3.ZERO_VEC, false, Vec3.ZERO_VEC);
			}
			default -> {
				yield null;
			}
		};
	}


	public static final Vec3 SUN_VEC = new Vec3(0, 1, 1).normalize();
	private static final float[] SUN_COLOR = {.9f, 0.9f, 0.9f}; //{.8f, 0.8f, 0.6f}; // {1, 0.8187f, 0.5333f};
	private static final float[] GROUND_COLOR = {0.1f, 0.1f, 0.1f}; // {0.2f, 0.2f, 0.2f}; // {0.35f, 0.30f, 0.35f};
	private static final float[] SKY_COLOR_HORIZON = {0.2f, 0.25f, 0.4f}; // {0.2f, 0.2f, 0.2f}; // {1.0f, 1.0f, 1.0f};
	private static final float[] SKY_COLOR = {0.3f, 0.3f, 0.4f}; // {0.01f, 0.02f, 0.1f}; // {0.08f, 0.37f, 0.73f};
	private static final float SUN_INVERSE_RADIUS = 100f; // bigger = smaller
	private static final float SUN_INTENSITY = .9f; // bigger = brighter

	private float[] computeSkyColor(Vec3 dir) {
		// smoothstep(0, 0.4, dir.y)
		float t0 = (dir.y) / 0.4f;
		t0 = Math.max(0.0f, Math.min(1.0f, t0));
		t0 = t0 * t0 * (3.0f - 2.0f * t0);
		float skyGradientT = FloatMath.pow(t0, 0.35f);

		// lerp(skyColourHorizon, skyColourZenith, skyGradientT)
		float[] skyGradient = new float[3];
		skyGradient[0] = SKY_COLOR_HORIZON[0] + (SKY_COLOR[0] - SKY_COLOR_HORIZON[0]) * skyGradientT;
		skyGradient[1] = SKY_COLOR_HORIZON[1] + (SKY_COLOR[1] - SKY_COLOR_HORIZON[1]) * skyGradientT;
		skyGradient[2] = SKY_COLOR_HORIZON[2] + (SKY_COLOR[2] - SKY_COLOR_HORIZON[2]) * skyGradientT;

		// smoothstep(-0.01, 0, dir.y)
		float t1 = (dir.y + 0.01f) / 0.01f;
		t1 = Math.max(0.0f, Math.min(1.0f, t1));
		t1 = t1 * t1 * (3.0f - 2.0f * t1);
		float groundToSkyT = t1;
		float sunMask = (groundToSkyT >= 1.0f) ? 1.0f : 0.0f;

		float sunniness = FloatMath.pow(Math.max(0f, dir.dot(SUN_VEC)), 1000f / SUN_INVERSE_RADIUS) * SUN_INTENSITY;

		float[] composite = new float[3];
		composite[0] = GROUND_COLOR[0] + (skyGradient[0] - GROUND_COLOR[0]) * groundToSkyT + sunniness * SUN_COLOR[0] * sunMask;
		composite[1] = GROUND_COLOR[1] + (skyGradient[1] - GROUND_COLOR[1]) * groundToSkyT + sunniness * SUN_COLOR[1] * sunMask;
		composite[2] = GROUND_COLOR[2] + (skyGradient[2] - GROUND_COLOR[2]) * groundToSkyT + sunniness * SUN_COLOR[2] * sunMask;

		if (composite[0] > 1) composite[0] = 1;
		if (composite[1] > 1) composite[1] = 1;
		if (composite[2] > 1) composite[2] = 1;

		return composite;
	}

	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof Environment env){
			return env.backgroundType == backgroundType && env.physicalObjects.equals(physicalObjects);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode(){
		int hashCode = 0;
		hashCode ^= backgroundType;
		hashCode ^= physicalObjects.hashCode();
		return hashCode;
	}

	@Override
	public String toString(){
		return "Environment with "+physicalObjects.size()+" objects and a background type:"+backgroundType;
	}
}