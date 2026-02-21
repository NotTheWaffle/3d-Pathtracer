
import Math.Vec3;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Environment{
	
	public final PhysicalObject sun;
	public final Vec3 sunVec;
	public final List<PhysicalObject> physicalObjects;
	public Environment(){
		this(false);
	}
	public Environment(boolean sun){
		physicalObjects = new ArrayList<>();
		if (sun){
			this.sun = new Sphere(null, 0, Material.LIGHT);
			this.sunVec = new Vec3(0, 1, 1).normalize();
		} else {
			this.sun = new Sphere(null, 0, Material.solid(Color.BLACK));
			this.sunVec = new Vec3(0, 0, 0);
		}
	}
	public void addCornellBox(double innerWidth, double outerWidth){
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
		add(new Sphere(new Vec3(0, 0, 2.5), 1, Material.solid(Color.RED)));
		add(new Sphere(new Vec3(2.5, 0, 0), 1, Material.solid(Color.BLUE)));
		add(new Sphere(new Vec3(-2.5, 0, 0), 1, Material.solid(Color.GREEN)));
		add(new RectangularPrism(0, -1.5, 0, 20, 1, 20, Material.SOLID, 0));
	}
	public void addHueSpheres(int count, double radius){
		for (int i = 0; i < count; i++){
			Color color = new Color(Color.HSBtoRGB((float)i/count, 1, 1));
			add(new Sphere(Transform.rotationY(i*2*Math.PI/count).transform(new Vec3(3, 0, 0)), radius, Material.light(color)));
		}
	}
	public void add(PhysicalObject object){
		physicalObjects.add(object);
	}
}