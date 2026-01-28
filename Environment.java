
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Environment{
	public final List<Point> points;
	public final List<PhysicalObject> physicalObjects;
	public Environment(String filename){
		this(Mesh.loadObj("Models/"+filename+".obj", Material.SOLID));
	}
	public Environment(){
		physicalObjects = new ArrayList<>();

		this.points = new ArrayList<>();
	}
	public Environment(PhysicalObject... mesh){
		physicalObjects = new ArrayList<>();
		physicalObjects.addAll(Arrays.asList(mesh));
		this.points = new ArrayList<>();
	}
}