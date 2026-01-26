



public class Transform {
	public Vec3 translation;
	public Mat3 rot;
	public Mat3 inv;
	public Transform(){
		this.translation = new Vec3(0, 0, 0);
		this.rot = Mat3.identity();
		this.inv = rot.transpose();
	}
	public Vec3 getForwardVector(){
		return new Vec3(rot.m[2], rot.m[5], rot.m[8]).normalize();
	}
	public Vec3 applyTo(Vec3 point){
		return inv.transform(point.sub(translation));
	}
	public double distanceTo(Vec3 point){
		return translation.dist(point);
	}
	public void rotateX(double pitch){
		rot = Mat3.multiply(rot, rotationX(-pitch));
		inv = rot.transpose();
	}
	public void rotateY(double yaw){
		rot = Mat3.multiply(rot, rotationY(-yaw));
		inv = rot.transpose();
	}
	public void rotateZ(double roll){
		rot = Mat3.multiply(rot, rotationZ(-roll));
		inv = rot.transpose();
	}
	public void translate(double x, double y, double z){
		Vec3 fixed = rot.transform(new Vec3(x, y, z));
		translation = translation.add(fixed);
	}
	public void translate(Vec3 vec){
		translation = translation.add(vec);
	}
	
	public static Mat3 rotationX(double pitch) {
		Mat3 m = Mat3.identity();
		m.m[4] =  Math.cos(pitch);
		m.m[5] = -Math.sin(pitch);
		m.m[7] =  Math.sin(pitch);
		m.m[8] =  Math.cos(pitch);
		return m;
	}

	public static Mat3 rotationY(double yaw) {
		Mat3 m = Mat3.identity();
		m.m[0] =  Math.cos(yaw);
		m.m[2] =  Math.sin(yaw);
		m.m[6] = -Math.sin(yaw);
		m.m[8] =  Math.cos(yaw);
		return m;
	}

	public static Mat3 rotationZ(double roll) {
		Mat3 m = Mat3.identity();
		m.m[0] =  Math.cos(roll);
		m.m[1] = -Math.sin(roll);
		m.m[3] =  Math.sin(roll);
		m.m[4] =  Math.cos(roll);
		return m;
	}
	@Override
	public String toString(){
		return translation.toString()+"\n"+rot.toString();
	}
}
