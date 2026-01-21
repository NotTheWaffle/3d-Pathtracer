
import java.awt.Color;
import java.io.Serializable;

public final class Triangle implements Serializable{
	public final Vec3 p1;
	public final Vec3 p2;
	public final Vec3 p3;
	private transient final Vec3 normal;
	public transient final Color underlyingColor;
	public transient Color color;
	
	public Triangle(Vec3 p1, Vec3 p2, Vec3 p3){
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.normal = normal();
		this.underlyingColor = Color.white;
		recolor(new Vec3(0, 1, 0).normalize());
	}
	public Triangle(int i1, int i2, int i3, Vec3[] points){
		this(points[i1], points[i2], points[i3]);
	}
	public double avgZ(Transform cam){
		
		double avg = (cam.distanceTo(p1) + cam.distanceTo(p2) + cam.distanceTo(p3))/3;
		return avg;
	}
	public void recolor(Vec3 light){
		double coeff = light.dot(normal);
		if (coeff < 0.2) coeff = 0.2;
		this.color = new Color(
			(int) (underlyingColor.getRed() * coeff),
			(int) (underlyingColor.getGreen() * coeff),
			(int) (underlyingColor.getBlue() * coeff)
		);
	}
	public Vec3 normal() {
		Vec3 edge1 = p2.sub(p1);
		Vec3 edge2 = p3.sub(p1);
		return edge1.cross(edge2).normalize();
	}
	public void render(java.awt.image.WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam) {
		Vec3 p1 = cam.applyTo(this.p1);
		Vec3 p2 = cam.applyTo(this.p2);
		Vec3 p3 = cam.applyTo(this.p3);
		
		if (p1.z > 0 || p2.z > 0 || p3.z > 0) return;

		double iz1 = 1.0 / p1.z;
		double iz2 = 1.0 / p2.z;
		double iz3 = 1.0 / p3.z;


		int x1 = (int)( focalLength * p1.x / p1.z) + cx;
		int y1 = (int)(-focalLength * p1.y / p1.z) + cy;

		int x2 = (int)( focalLength * p2.x / p2.z) + cx;
		int y2 = (int)(-focalLength * p2.y / p2.z) + cy;

		int x3 = (int)( focalLength * p3.x / p3.z) + cx;
		int y3 = (int)(-focalLength * p3.y / p3.z) + cy;

		int minX = Math.max(0, Math.min(x1, Math.min(x2, x3)));
		int maxX = Math.min(zBuffer.length - 1, Math.max(x1, Math.max(x2, x3)));
		int minY = Math.max(0, Math.min(y1, Math.min(y2, y3)));
		int maxY = Math.min(zBuffer[0].length - 1, Math.max(y1, Math.max(y2, y3)));

		double area = edge(x1, y1, x2, y2, x3, y3);
		if (area == 0) return;

		int[] rgb = {
			color.getRed(),
			color.getGreen(),
			color.getBlue(),
			255
		};
		double ia = 1/area;
		double w1Incr = (y3-y2) * ia;
		double w2Incr = (y1-y3) * ia;
		double w3Incr = (y2-y1) * ia;

		for (int y = minY; y <= maxY; y++) {
			double w1 = edge(x2, y2, x3, y3, minX-1, y) * ia;
			double w2 = edge(x3, y3, x1, y1, minX-1, y) * ia;
			double w3 = edge(x1, y1, x2, y2, minX-1, y) * ia;
			for (int x = minX; x <= maxX; x++) {

				w1 += w1Incr;
				w2 += w2Incr;
				w3 += w3Incr;

				if (w1 >= 0 && w2 >= 0 && w3 >= 0) {
					double iz = w1 * iz1 + w2 * iz2 + w3 * iz3;

					if (iz < zBuffer[x][y]) {
						
						zBuffer[x][y] = iz;
						raster.setPixel(x, y, rgb);
					}
				}
			}
		}
	}
	private static double edge(int x1, int y1, int x2, int y2, int x, int y) {
		return (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);
	}
	public Vec3 getIntersection(Vec3 rayVector, Vec3 rayOrigin){

		final double EPSILON = 1e-4;

		Vec3 edge1 = p2.sub(p1);
		Vec3 edge2 = p3.sub(p1);
		Vec3 h = rayVector.cross(edge2);
		

		double a = edge1.dot(h);

		if (a > -EPSILON && a < EPSILON) {
			return null;
		}

		double f = 1.0 / a;
		Vec3 s = rayOrigin.sub(p1);
		double u = f * s.dot(h);

		if (u < 0.0 || u > 1.0) {
			return null;
		}

		Vec3 q = s.cross(edge1);
		double v = f * rayVector.dot(q);

		if (v < 0.0 || u + v > 1.0) {
			return null;
		}

		double t = f * edge2.dot(q);
		if (t < EPSILON) {
			return rayVector.mul(t).add(rayOrigin);
		} else {
			return null;
		}
	}
	@Override
	public int hashCode(){
		return p1.hashCode() ^ p2.hashCode() ^ p3.hashCode();
	}
	@Override
	public boolean equals(Object o){
		if (o == null || !(o instanceof Triangle)){
			return false;
		}
		Triangle t = (Triangle) o;
		return t.p1.equals(p1) && t.p2.equals(p2) && t.p3.equals(p3);
	}
}