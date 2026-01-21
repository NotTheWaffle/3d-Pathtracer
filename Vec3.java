
import java.io.Serializable;

public class Vec3 implements Serializable{
	public double x, y, z;

	public Vec3(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3 normalize(){
		double r = Math.sqrt(x*x + y*y + z*z);
		return new Vec3(
			x/r,
			y/r,
			z/r
		);
	}

	public Vec3 add(Vec3 v){
		return new Vec3(
			x + v.x,
			y + v.y,
			z + v.z
		);
	}
	public Vec3 sub(Vec3 v){
		return new Vec3(
			x - v.x,
			y - v.y,
			z - v.z
		);
	}
	public Vec3 mul(double m){
		return new Vec3(
			x * m,
			y * m,
			z * m
		);
	}
	public double dot(Vec3 v){
		return x * v.x + y * v.y + z * v.z;
	}
	public double dist(Vec3 v){
		double dx = x-v.x;
		double dy = y-v.y;
		double dz = z-v.z;
		return Math.sqrt(dx*dx + dy*dy + dz*dz);
	}
	public Vec3 cross(Vec3 v){
		return new Vec3(
			y * v.z - z * v.y,
			z * v.x - x * v.z,
			x * v.y - y * v.x
		);
	}
	@Override
	public String toString(){
		return String.format("(%3.2f, %3.2f, %3.2f)", x, y, z);
	}
}