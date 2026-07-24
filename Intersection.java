import Math.Vec3;

/**
 * A representation of a ray intersecting with a physical object.
 * Intersection
 */
public class Intersection {
	Vec3 pos;
	Material material;
	Vec3 normal;
	boolean backface;
	public Intersection(Vec3 pos, Material material, Vec3 normal, boolean backface){
		this.pos = pos;
		this.material = material;
		this.normal = normal;
		this.backface = backface;
	}
}