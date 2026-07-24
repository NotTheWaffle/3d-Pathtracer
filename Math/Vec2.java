package Math;

public class Vec2{
	public final float x, y;
	public static final Vec2 ZERO_VEC = new Vec2(0, 0);

	public Vec2(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Vec2 normalize(){
		float r = FloatMath.sqrt(x*x + y*y);
		if (r == 0) return ZERO_VEC;
		return new Vec2(
			x/r,
			y/r
		);
	}

	public Vec2 add(Vec2 v){
		return new Vec2(
			x + v.x,
			y + v.y
		);
	}
	public Vec2 sub(Vec2 v){
		return new Vec2(
			x - v.x,
			y - v.y
		);
	}
	public Vec2 mul(float m){
		return new Vec2(
			x * m,
			y * m
		);
	}
	public float dot(Vec2 v){
		return x * v.x + y * v.y;
	}
	public float dist(Vec2 v){
		float dx = x-v.x;
		float dy = y-v.y;
		return FloatMath.sqrt(dx*dx + dy*dy);
	}


	@Override
	public int hashCode(){
		return
			Integer.hashCode(Float.floatToRawIntBits(x)) ^
			Integer.hashCode(Float.floatToRawIntBits(y));
	}
	@Override
	public boolean equals(Object o){
		if (this == o) return true;
		if (o instanceof Vec3 v){
			return x == v.x && y == v.y;
		} else {
			return false;
		}
	}
	@Override
	public String toString(){
		return String.format("(%3.2f, %3.2f)", x, y);
	}
}