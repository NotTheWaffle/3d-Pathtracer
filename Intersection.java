import Math.Vec3;

public class Intersection {
	public final Vec3 pos;
	public final PhysicalObject object;
	public final Vec3 normal;
	public final boolean backface;
	public Intersection(Vec3 pos, PhysicalObject collisionEntity, Vec3 normal, boolean backface){
		this.pos = pos;
		this.object = collisionEntity;
		this.normal = normal;
		this.backface = backface;
	}
}