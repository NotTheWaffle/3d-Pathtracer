package Math;

public class ImplicitEquation{
	public static ImplicitEquation torus(float R, float r){
		return new ImplicitEquation((x, y, z) -> {
				float s = (x*x + y*y + z*z + R*R - r*r);
				return (s*s - 4 * R * R * (x * x + y * y));
		});
	}
	public static ImplicitEquation bean(){
		return new ImplicitEquation((x, y, z) -> {
			float R = 1.25f;
			float a = 0.45f;
			float b = 1.0f;
			float z0 = 0.15f;

			float radial = FloatMath.sqrt(x*x + y*y);

			float F =
				FloatMath.pow(radial - R, 2) / (a*a)
				+ FloatMath.pow(z - z0, 2) / (b*b)
				- 1.0f
				+ 0.12f * FloatMath.pow(z, 3)
				+ 0.03f * x*x * y*y;

			return F;
		});
	}

	TriFunction<Float, Float, Float, Float> func;
	public ImplicitEquation(TriFunction<Float, Float, Float, Float> func){
		this.func = func;
	}
	public float apply(float x, float y, float z){
		return func.apply(x, y, z);
	}
	@FunctionalInterface
	public interface TriFunction<A, B, C, R> {
		R apply(A a, B b, C c);
	}
}