
import java.util.Arrays;

public class Mat3 {
	public final double[] m;
	private Mat3(double d0, double d1, double d2,
				double d3, double d4, double d5,
				double d6, double d7, double d8 ){
		this.m = new double[] {
			d0, d1, d2,
			d3, d4, d5,
			d6, d7, d8
		};
	}
	private Mat3(){
		this.m = new double[9];
	}
	public static Mat3 identity() {
		Mat3 r = new Mat3();
		for (int i = 0; i < 9; i+=4) r.m[i] = 1;
		return r;
	}
	public Mat3 multiply(Mat3 mat3){
		Mat3 r = new Mat3();
		for (int row = 0; row < 9; row+=3) {
			for (int col = 0; col < 3; col++) {
				for (int k = 0; k < 3; k++) {
					r.m[row+col] += this.m[row+k] * mat3.m[k*3+col];
				}
			}
		}
		return r;
	}
	public static Mat3 multiply(Mat3 a, Mat3 b) {
		Mat3 r = new Mat3();
		for (int row = 0; row < 9; row+=3) {
			for (int col = 0; col < 3; col++) {
				for (int k = 0; k < 3; k++) {
					r.m[row+col] += a.m[row+k] * b.m[k*3+col];
				}
			}
		}
		return r;
	}
	public Vec3 transform(Vec3 v) {
		return new Vec3(
			v.x * m[0] + v.y * m[1] + v.z * m[2],
			v.x * m[3] + v.y * m[4] + v.z * m[5],
			v.x * m[6] + v.y * m[7] + v.z * m[8]
		);
	}
	public Mat3 transpose(){
		return new Mat3(
			this.m[0], this.m[3], this.m[6],
			this.m[1], this.m[4], this.m[7],
			this.m[2], this.m[5], this.m[8]
		);
	}
	
	@Override
	public int hashCode(){
		return Arrays.hashCode(m);
	}
	@Override
	public boolean equals(Object o){
		if (o == null || !(o instanceof Mat3)){
			return false;
		}
		Mat3 mat = (Mat3) o;
		return Arrays.equals(mat.m, m);
	}
}