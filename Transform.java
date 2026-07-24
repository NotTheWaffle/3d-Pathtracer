import Math.FloatMath;
import Math.Mat3;
import Math.Vec3;

public class Transform {
	public Vec3 translation;
	public Mat3 rot;
	public Mat3 inv;
	public Transform(){
		this(new Vec3(0, 0, 0), Mat3.IDENTITY);
	}
	public Transform(float x, float y, float z){
		this(new Vec3(x, y, z), Mat3.IDENTITY);
	}
	private Transform(Vec3 translation, Mat3 rot){
		this.translation = translation;
		this.rot = rot;
		this.inv = rot.transpose();
	}

	public Vec3 applyTo(Vec3 point){
		return inv.mul(point.sub(translation));
	}
	public Vec3 unapplyTo(Vec3 point){
		return translation.add(rot.mul(point));
	}

	public Vec3 applyTo2(Vec3 point){
		return inv.mul(point).sub(translation);
	}
	public Vec3 unapplyTo2(Vec3 point){
		return rot.mul(translation.add(point));
	}

	public Transform turnX(float pitch){
		rot = Mat3.multiply(rot, rotationX(-pitch));
		inv = rot.transpose();
		return this;
	}
	public Transform turnY(float yaw){
		rot = Mat3.multiply(rot, rotationY(-yaw));
		inv = rot.transpose();
		return this;
	}
	public Transform turnZ(float roll){
		rot = Mat3.multiply(rot, rotationZ(-roll));
		inv = rot.transpose();
		return this;
	}
	public Transform move(float right, float up, float forward){
		Vec3 fixed = rot.mul(new Vec3(right, up, forward));
		translation = translation.add(fixed);
		return this;
	}

	public Transform rotateX(float pitch){
		rot = Mat3.multiply(rotationX(pitch), rot);
		inv = rot.transpose();
		return this;
	}
	public Transform rotateY(float yaw){
		rot = Mat3.multiply(rotationY(yaw), rot);
		inv = rot.transpose();
		return this;
	}
	public Transform rotateZ(float roll){
		rot = Mat3.multiply(rotationZ(roll), rot);
		inv = rot.transpose();
		return this;
	}
	public Transform translate(float x, float y, float z){
		translation = translation.add(new Vec3(x, y, z));
		return this;
	}

	private static Mat3 rotationX(float pitch) {
		float c = FloatMath.cos(pitch);
		float s = FloatMath.sin(pitch);
		return new Mat3(
			1, 0, 0,
			0, c, -s,
			0, s, c
		);
	}

	private static Mat3 rotationY(float yaw) {
		float c = FloatMath.cos(yaw);
		float s = FloatMath.sin(yaw);
		return new Mat3(
			c, 0, s,
			0, 1, 0,
			-s, 0, c
		);
	}

	private static Mat3 rotationZ(float roll) {
		float c = FloatMath.cos(roll);
		float s = FloatMath.sin(roll);
		return new Mat3(
			c, -s, 0,
			s, c, 0,
			0, 0, 1
		);
	}
	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof Transform t){
			return t.rot.equals(rot) && t.translation.equals(translation);
		} else {
			return false;
		}
	}
	@Override
	public int hashCode(){
		return rot.hashCode() ^ translation.hashCode();
	}
	@Override
	public String toString(){
		return translation.toString()+"\n"+rot.toString();
	}
}
