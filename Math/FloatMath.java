package Math;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class FloatMath {
	public static final float PI = (float) Math.PI;
	public static float sqrt(float f){
		return (float) Math.sqrt(f);
	}
	public static float exp(float f){
		return (float) Math.exp(f);
	}
	public static float pow(float a, float b){
		return (float) Math.pow(a, b);
	}
	public static float cos(float a){
		return (float) Math.cos(a);
	}
	public static float sin(float a){
		return (float) Math.sin(a);
	}
	public static float tan(float a){
		return (float) Math.tan(a);
	}
	public static float random(){
		return Float.intBitsToFloat(0x3f80_0000 | ThreadLocalRandom.current().nextInt(0x80_0000)) - 1;
	}
	public static float randomGaussian(Random random){
		return sqrt(-2*(float) Math.log(FloatMath.random())) * cos(2*PI*FloatMath.random());
	}
}
