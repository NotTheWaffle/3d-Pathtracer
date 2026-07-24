import Math.Vec3;

/**
 * A representation of a ray intersecting with a physical object. Has a position, Material, and Normal
 */
public class Intersection {
	Vec3 pos;
	Material material;
	Vec3 normal;
	boolean backface;
	float distance;
	public Intersection(Vec3 pos, Material material, Vec3 normal, boolean backface, Vec3 origin){
		this.pos = pos;
		this.material = material;
		this.normal = normal;
		this.backface = backface;
		this.distance = origin.dist(pos);
	}
	public Intersection(Vec3 pos, Material material, Vec3 direction, float dist){
		this.pos = pos;
		this.material = material;
		this.normal = direction;
		this.distance = dist;
		this.backface = false;
	}
}