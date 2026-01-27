import Math.Vec3;

public class Intersection {
	public final Material mat;
	public final Vec3 pos;
	public final Triangle collisionEntity;
	public Intersection(Material mat, Vec3 pos){
		this.mat = mat;
		this.pos = pos;
		this.collisionEntity = null;
	}
	public Intersection(Vec3 pos, Triangle tri){
		this.pos = pos;
		this.mat = Material.SOLID;
		this.collisionEntity = tri;
	}
	public enum Material{
		LIGHT,
		SOLID
	}
}
