import Math.Mat3;
import Math.Vec3;

public final class Transform {
	public Vec3 translation;
	public Mat3 rot;
	public Mat3 inv;
	public Transform(){
		final double EPSILON = 1e-8;
		this.translation = new Vec3(EPSILON, EPSILON, EPSILON);
		this.rot = Mat3.identity();
		this.inv = rot.transpose();
		rotateX(EPSILON);
		rotateY(EPSILON);
		rotateZ(EPSILON);
	}
	public Vec3 getForwardVector(){
		return new Vec3(rot.m02, rot.m12, rot.m22).normalize();
	}

	public Vec3 applyTo(Vec3 point){
		return inv.transform(point.sub(translation));
	}
	
	public Transform rotateX(double pitch){
		rot = Mat3.multiply(rot, rotationX(-pitch));
		inv = rot.transpose();
		return this;
	}
	public Transform rotateY(double yaw){
		rot = Mat3.multiply(rot, rotationY(-yaw));
		inv = rot.transpose();
		return this;
	}
	public Transform rotateZ(double roll){
		rot = Mat3.multiply(rot, rotationZ(-roll));
		inv = rot.transpose();
		return this;
	}
	
	public Transform translate(double x, double y, double z){
		Vec3 fixed = rot.transform(new Vec3(x, y, z));
		translation = translation.add(fixed);
		return this;
	}
	public Transform translateAbsolute(Vec3 vec){
		translation = translation.add(vec);
		return this;
	}
	
	public static Mat3 rotationX(double pitch) {
		double c = Math.cos(pitch);
		double s = Math.sin(pitch);
		return new Mat3(
			1, 0, 0,
			0, c, -s,
			0, s, c
		);
	}

	public static Mat3 rotationY(double yaw) {
		double c = Math.cos(yaw);
		double s = Math.sin(yaw);
		return new Mat3(
			c, 0, s,
			0, 1, 0,
			-s, 0, c
		);
	}

	public static Mat3 rotationZ(double roll) {
		double c = Math.cos(roll);
		double s = Math.sin(roll);
		return new Mat3(
			c, -s, 0,
			s, c, 0,
			0, 0, 1
		);
	}
	@Override
	public String toString(){
		return translation.toString()+"\n"+rot.toString();
	}
}
