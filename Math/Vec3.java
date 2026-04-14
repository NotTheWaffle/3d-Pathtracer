package Math;

import java.util.Random;

public class Vec3{
	public final float x, y, z;
	public static final Vec3 ZERO_VEC = new Vec3(0, 0, 0);

	public Vec3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec3 normalize(){
		float r = FloatMath.sqrt(x*x + y*y + z*z);
		if (r == 0) return new Vec3(0, 0, 0);
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
	public Vec3 mul(float m){
		return new Vec3(
			x * m,
			y * m,
			z * m
		);
	}
	public float dot(Vec3 v){
		return x * v.x + y * v.y + z * v.z;
	}
	public float dist(Vec3 v){
		float dx = x-v.x;
		float dy = y-v.y;
		float dz = z-v.z;
		return FloatMath.sqrt(dx*dx + dy*dy + dz*dz);
	}
	public Vec3 cross(Vec3 v){
		return new Vec3(
			y * v.z - z * v.y,
			z * v.x - x * v.z,
			x * v.y - y * v.x
		);
	}

	public static Vec3 random(Random random){
		return new Vec3(FloatMath.random() - .5f, FloatMath.random() - .5f, FloatMath.random() - .5f);
	}
	public static Vec3 randomUnit(Random random){
		return new Vec3(FloatMath.randomGaussian(random), FloatMath.randomGaussian(random), FloatMath.randomGaussian(random)).normalize();
	}

	@Override
	public int hashCode(){
		return
			Integer.hashCode(Float.floatToRawIntBits(x)) ^
			Integer.hashCode(Float.floatToRawIntBits(y)) ^
			Integer.hashCode(Float.floatToRawIntBits(z));
	}
	@Override
	public boolean equals(Object o){
		if (this == o) return true;
		if (o instanceof Vec3 v){
			return x == v.x && y == v.y && z == v.z;
		} else {
			return false;
		}
	}
	@Override
	public String toString(){
		return String.format("(%3.2f, %3.2f, %3.2f)", x, y, z);
	}
}