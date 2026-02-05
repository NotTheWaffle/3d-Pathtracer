
import Math.Pair;
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.List;

public final class Triangle{
	public final Vec3 p1;
	public final Vec3 p2;
	public final Vec3 p3;
	public final Vec3 normal;
	public Triangle(Vec3 p1, Vec3 p2, Vec3 p3){
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.normal = normal();
	}
	public Triangle(int i1, int i2, int i3, List<Vec3> points){
		if (i1 < 0) i1 += points.size();
		if (i2 < 0) i2 += points.size();
		if (i3 < 0) i3 += points.size();
		if (i1 < 0 || i2 < 0 || i3 < 0 || i1 >= points.size() || i2 >= points.size() || i3 >= points.size()){

			System.out.println("error");
			i1 = i2 = i3 = 0;
		}
		this(points.get(i1), points.get(i2), points.get(i3));
	}
	public Vec3 center(){
		return (p1.add(p2).add(p3)).mul(1.0/3);
	}
	private Vec3 normal() {
		Vec3 edge1 = p2.sub(p1);
		Vec3 edge2 = p3.sub(p1);
		return edge1.cross(edge2).normalize();
	}
	public void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam) {
		Vec3 projectedP1 = cam.applyTo(this.p1);
		Vec3 projectedP2 = cam.applyTo(this.p2);
		Vec3 projectedP3 = cam.applyTo(this.p3);
		
		if (projectedP1.z < 0 || projectedP2.z < 0 || projectedP3.z < 0) return;

		double iz1 = 1.0 / projectedP1.z;
		double iz2 = 1.0 / projectedP2.z;
		double iz3 = 1.0 / projectedP3.z;


		int x1 = (int)( focalLength * projectedP1.x / projectedP1.z) + cx;
		int y1 = (int)(-focalLength * projectedP1.y / projectedP1.z) + cy;

		int x2 = (int)( focalLength * projectedP2.x / projectedP2.z) + cx;
		int y2 = (int)(-focalLength * projectedP2.y / projectedP2.z) + cy;

		int x3 = (int)( focalLength * projectedP3.x / projectedP3.z) + cx;
		int y3 = (int)(-focalLength * projectedP3.y / projectedP3.z) + cy;

		int minX = Math.max(0, Math.min(x1, Math.min(x2, x3)));
		int maxX = Math.min(zBuffer.length - 1, Math.max(x1, Math.max(x2, x3)));
		int minY = Math.max(0, Math.min(y1, Math.min(y2, y3)));
		int maxY = Math.min(zBuffer[0].length - 1, Math.max(y1, Math.max(y2, y3)));

		double area = edge(x1, y1, x2, y2, x3, y3);
		if (area == 0) return;

		int[] rgb = {
			255,
			255,
			255,
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
					double z = 1/iz;
					if (z < zBuffer[x][y]) {
						
						zBuffer[x][y] = z;
						raster.setPixel(x, y, rgb);
					}
				}
			}
		}
	}
	private static double edge(int x1, int y1, int x2, int y2, int x, int y) {
		return (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);
	}
	public Pair<Vec3, Vec3> getIntersection(Vec3 rayOrigin, Vec3 rayVector){
		final double EPSILON = 1e-8;
		// the ray should be pointing in the opposite direction as the normal, else they shouldn't've interescted
		if (rayVector.dot(normal) > 0) return null;

		Vec3 edge1 = p2.sub(p1);
		Vec3 edge2 = p3.sub(p1);
		Vec3 h = rayVector.cross(edge2);

		double a = edge1.dot(h);

		if (a > -EPSILON && a < EPSILON) return null;

		double f = 1.0 / a;
		Vec3 s = rayOrigin.sub(p1);
		double u = f * s.dot(h);

		if (u < 0.0 || u > 1.0) return null;

		Vec3 q = s.cross(edge1);
		double v = f * rayVector.dot(q);

		if (v < 0.0 || u + v > 1.0) return null;

		double t = f * edge2.dot(q);
		if (t < EPSILON) return null;

		return new Pair<>(rayVector.mul(t).add(rayOrigin), this.normal);
	}

	@Override
	public int hashCode(){
		return p1.hashCode() ^ p2.hashCode() ^ p3.hashCode();
	}

	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof Triangle t){
			return t.p1.equals(p1) && t.p2.equals(p2) && t.p3.equals(p3);
		} else {
			return false;
		}
	}

	@Override
	public String toString(){
		return "Tri:("+p1+", "+p2+", "+p3+")";
	}
}