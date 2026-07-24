
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.List;

public class Triangle extends DeficientPhysicalObject{
	public final static float EPSILON = 1e-4f;
	public final Vec3 p1;
	public final Vec3 p2;
	public final Vec3 p3;
	public Triangle(Vec3 p1, Vec3 p2, Vec3 p3){
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}
	public Triangle(int i1, int i2, int i3, List<Vec3> points){
		this(points.get(i1), points.get(i2), points.get(i3));
	}
	@Override
	public Vec3 center(){
		return (p1.add(p2).add(p3)).mul(1.0f/3.0f);
	}
	public Vec3 normal() {
		Vec3 edge1 = p2.sub(p1);
		Vec3 edge2 = p3.sub(p1);
		return edge1.cross(edge2).normalize();
	}
	@Override
	public void grow(AABB aabb){
		aabb.grow(p1).grow(p2).grow(p3);
	}
	@Override
	public void render(WritableRaster raster, float[][] zBuffer, Viewport camera, Transform transform, int[] rgb) {
		Vec3 projectedP1 = camera.applyTo(transform.unapplyTo(this.p1));
		Vec3 projectedP2 = camera.applyTo(transform.unapplyTo(this.p2));
		Vec3 projectedP3 = camera.applyTo(transform.unapplyTo(this.p3));

		if (projectedP1.z < 0 || projectedP2.z < 0 || projectedP3.z < 0) return;

		float iz1 = 1.0f / projectedP1.z;
		float iz2 = 1.0f / projectedP2.z;
		float iz3 = 1.0f / projectedP3.z;


		int x1 = (int) camera.getX(projectedP1);
		int y1 = (int) camera.getY(projectedP1);

		int x2 = (int) camera.getX(projectedP2);
		int y2 = (int) camera.getY(projectedP2);

		int x3 = (int) camera.getX(projectedP3);
		int y3 = (int) camera.getY(projectedP3);

		int minX = Math.max(0, Math.min(x1, Math.min(x2, x3)));
		int maxX = Math.min(zBuffer.length - 1, Math.max(x1, Math.max(x2, x3)));
		int minY = Math.max(0, Math.min(y1, Math.min(y2, y3)));
		int maxY = Math.min(zBuffer[0].length - 1, Math.max(y1, Math.max(y2, y3)));

		float area = edge(x1, y1, x2, y2, x3, y3);
		if (area == 0) return;

		float ia = 1/area;
		float w1Incr = (y3-y2) * ia;
		float w2Incr = (y1-y3) * ia;
		float w3Incr = (y2-y1) * ia;

		for (int y = minY; y <= maxY; y++) {
			float w1 = edge(x2, y2, x3, y3, minX-1, y) * ia;
			float w2 = edge(x3, y3, x1, y1, minX-1, y) * ia;
			float w3 = edge(x1, y1, x2, y2, minX-1, y) * ia;
			for (int x = minX; x <= maxX; x++) {

				w1 += w1Incr;
				w2 += w2Incr;
				w3 += w3Incr;

				if (w1 >= 0 && w2 >= 0 && w3 >= 0) {
					float iz = w1 * iz1 + w2 * iz2 + w3 * iz3;
					float z = 1/iz;
					if (z < zBuffer[x][y]) {

						zBuffer[x][y] = z;
						raster.setPixel(x, y, rgb);
					}
				}
			}
		}
		//Vec3 center = p1.add(p2).add(p3).mul(1/3f);
		//rgb[0] = 255-rgb[0];
		//rgb[1] = 255-rgb[1];
		//rgb[2] = 255-rgb[2];
		//Ray.render(transform.unapplyTo(center), transform.unapplyTo(center.add(normal.mul(.02f))), raster, zBuffer, camera, rgb);
	}
	private static float edge(int x1, int y1, int x2, int y2, int x, int y) {
		return (x - x1) * (y2 - y1) - (y - y1) * (x2 - x1);
	}
	@Override
	public Intersection getDeficientIntersection(Vec3 rayOrigin, Vec3 rayDirection){
		Vec3 edge1 = p2.sub(p1);
		Vec3 edge2 = p3.sub(p1);
		Vec3 h = rayDirection.cross(edge2);

		float a = edge1.dot(h);

		// ideally used to cull triangles but floats don't have enough precision
		//if (a > -0 && a < 0) return null;

		float f = 1.0f / a;
		Vec3 s = rayOrigin.sub(p1);
		float u = f * s.dot(h);

		if (u < 0 || u > 1) return null;

		Vec3 q = s.cross(edge1);
		float v = f * rayDirection.dot(q);

		if (v < 0 || u + v > 1.0) return null;

		float t = f * edge2.dot(q);
		if (t < EPSILON) return null;
		Vec3 N = this.normal();
		return new Intersection(rayDirection.mul(t).add(rayOrigin), null, N, rayDirection.dot(N) > 0, rayOrigin);
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