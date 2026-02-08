
import Math.Vec3;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Environment{
	public final List<PhysicalObject> physicalObjects;
	public Environment(){
		physicalObjects = new ArrayList<>();
	}
	public Environment(PhysicalObject... mesh){
		this();
		physicalObjects.addAll(Arrays.asList(mesh));
	}
	public void addStanfordBox(double innerWidth, double outerWidth){
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, -outerWidth/2, -innerWidth/2, -outerWidth/2, outerWidth/2, Material.solid(Color.RED)));
		add(new RectangularPrism(-outerWidth/2, -innerWidth/2, -innerWidth/2, innerWidth/2, -outerWidth/2, innerWidth/2, Material.solid(Color.BLUE)));
		add(new RectangularPrism(innerWidth/2, outerWidth/2, -innerWidth/2, innerWidth/2, -outerWidth/2, innerWidth/2, Material.solid(Color.GREEN)));
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, -innerWidth/2, innerWidth/2, outerWidth/2, innerWidth/2, Material.solid(Color.WHITE)));
		add(new RectangularPrism(-outerWidth/2, outerWidth/2, outerWidth/2, innerWidth/2, -outerWidth/2, outerWidth/2, Material.solid(Color.WHITE)));
		add(new RectangularPrism(-innerWidth/4, innerWidth/4, (innerWidth/2-innerWidth/16), innerWidth/2, -innerWidth/4, innerWidth/4, Material.LIGHT));
	}
	public void addSphereTest(){
		add(new Sphere(new Vec3(0, 0, 2.5), 1, Material.solid(Color.RED)));
		add(new Sphere(new Vec3(2.5, 0, 0), 1, Material.solid(Color.BLUE)));
		add(new Sphere(new Vec3(-2.5, 0, 0), 1, Material.solid(Color.GREEN)));
		add(new RectangularPrism(0, -1.5, 0, 20, 1, 20, Material.SOLID, 0));
	}
	public void add(PhysicalObject object){
		physicalObjects.add(object);
	}
}