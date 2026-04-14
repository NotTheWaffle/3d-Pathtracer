
import Math.FloatMath;
import Math.Vec3;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Environment{

	public final Vec3 sunvec = new Vec3(0, 1, 1);;
	private final float[] sunColor = {.8f, 0.8f, 0.6f}; // {1, 0.8187f, 0.5333f};
	private final float[] groundColour = {0.2f, 0.2f, 0.2f}; // {0.35f, 0.30f, 0.35f};
	private final float[] skyColorHorizon = {0.2f, 0.2f, 0.2f}; // {1.0f, 1.0f, 1.0f};
	private final float[] skyColorZenith = {0.01f, 0.02f, 0.1f}; // {0.08f, 0.37f, 0.73f};
	private final float sunInverseRadius = 250f; // bigger = smaller
	private final float sunIntensity = .25f; // bigger = brighter

	private final boolean background;
	private final boolean sun;
	

	public final List<PhysicalObject> physicalObjects;
	public Environment(){
		this(false);
	}
	public Environment(boolean environment){
		physicalObjects = new ArrayList<>();
		this.background = environment;
		this.sun = !background;
	}
	public Environment(boolean sky, boolean sun){
		this.physicalObjects = new ArrayList<>();
		this.background = sky;
		this.sun = sun;
	}
	
	public void addCornellBox(float innerWidth, float outerWidth){
		//bottom
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, -outerWidth/2, -innerWidth/2, -outerWidth/2, outerWidth/2, Material.solid(Color.white)));
		//left
		add(new RectangularPrism(-outerWidth/2, -innerWidth/2, -innerWidth/2, innerWidth/2, -outerWidth/2, innerWidth/2, Material.solid(Color.RED)));
		//right
		add(new RectangularPrism(innerWidth/2, outerWidth/2, -innerWidth/2, innerWidth/2, -outerWidth/2, innerWidth/2, Material.solid(Color.GREEN)));
		//back
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, -innerWidth/2, innerWidth/2, outerWidth/2, innerWidth/2, Material.solid(Color.WHITE)));
		//ceiling
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, outerWidth/2, innerWidth/2, -outerWidth/2, outerWidth/2, Material.solid(Color.WHITE)));
		//light
		add(new RectangularPrism(-innerWidth/4, innerWidth/4, (innerWidth/2-innerWidth/16), innerWidth/2, -innerWidth/4, innerWidth/4, Material.LIGHT));
	}
	public void addSphereTest(){
		add(new Sphere(new Vec3(0, 0, 2.5f), 1, Material.solid(Color.RED)));
		add(new Sphere(new Vec3(2.5f, 0, 0), 1, Material.solid(Color.BLUE)));
		add(new Sphere(new Vec3(-2.5f, 0, 0), 1, Material.solid(Color.GREEN)));
		addFloor();
	}
	public void addFloor(){
		add(new RectangularPrism(0, -1.5f, 0, 20, 1, 20, Material.SOLID, 0));
	}
	public void addHueSpheres(int count, float radius){
		for (int i = 0; i < count; i++){
			Color color = new Color(Color.HSBtoRGB((float)i/count, 1, 1));
			add(new Sphere(new Transform().rotateY(i*2*FloatMath.PI/count).applyTo(new Vec3(3, 0, 0)), radius, Material.light(color)));
		}
	}
	
	public void add(PhysicalObject object){
		physicalObjects.add(object);
	}
	public Intersection getBackgroundEnvironment(Vec3 rayDirection){
		if (background){
			return new Intersection(Vec3.ZERO_VEC, new Material(computeSkyColor(rayDirection), 1), Vec3.ZERO_VEC, false);
		} else if (sun){
			if (rayDirection.dist(sunvec) < .7){
				return new Intersection(Vec3.ZERO_VEC, Material.LIGHT, Vec3.ZERO_VEC, false);
			}
		}
		return null;
	}
	public float[] computeSkyColor(Vec3 dir) {
		// smoothstep(0, 0.4, dir.y)
		float t0 = (dir.y) / 0.4f;
		t0 = Math.max(0.0f, Math.min(1.0f, t0));
		t0 = t0 * t0 * (3.0f - 2.0f * t0);
		float skyGradientT = FloatMath.pow(t0, 0.35f);

		// lerp(skyColourHorizon, skyColourZenith, skyGradientT)
		float[] skyGradient = new float[3];
		skyGradient[0] = skyColorHorizon[0] + (skyColorZenith[0] - skyColorHorizon[0]) * skyGradientT;
		skyGradient[1] = skyColorHorizon[1] + (skyColorZenith[1] - skyColorHorizon[1]) * skyGradientT;
		skyGradient[2] = skyColorHorizon[2] + (skyColorZenith[2] - skyColorHorizon[2]) * skyGradientT;

		// smoothstep(-0.01, 0, dir.y)
		float t1 = (dir.y + 0.01f) / 0.01f;
		t1 = Math.max(0.0f, Math.min(1.0f, t1));
		t1 = t1 * t1 * (3.0f - 2.0f * t1);
		float groundToSkyT = t1;
		float sunMask = (groundToSkyT >= 1.0f) ? 1.0f : 0.0f;

		float sunniness = FloatMath.pow(Math.max(0f, dir.dot(sunvec)), 1000f / sunInverseRadius) * sunIntensity;

		float[] composite = new float[3];
		composite[0] = groundColour[0] + (skyGradient[0] - groundColour[0]) * groundToSkyT + sunniness * sunColor[0] * sunMask;
		composite[1] = groundColour[1] + (skyGradient[1] - groundColour[1]) * groundToSkyT + sunniness * sunColor[1] * sunMask;
		composite[2] = groundColour[2] + (skyGradient[2] - groundColour[2]) * groundToSkyT + sunniness * sunColor[2] * sunMask;
		
		if (composite[0] > 1) composite[0] = 1;
		if (composite[1] > 1) composite[1] = 1;
		if (composite[2] > 1) composite[2] = 1;

		return composite;
	}
}