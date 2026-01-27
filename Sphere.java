
import java.awt.Color;

import Math.Vec3;

public class Sphere {
	public final Vec3 point;
	public final Color color;
	public final double radius;
	public Sphere(Vec3 p, double radius){
		this.point = p;
		this.color = new Color((int)(Math.random()*16777216));
		this.radius = radius;
	}
	public Sphere(Vec3 p, double radius, Color c){
		this.point = p;
		this.color = c;
		this.radius = radius;
	}
	public void render(java.awt.image.WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam) {
		Vec3 p = cam.applyTo(this.point);
		if (p.z < 0) return;

		
		int screenX = (int)( focalLength * p.x / p.z) + cx;
		int screenY = (int)(-focalLength * p.y / p.z) + cy;


		int radius = Math.max(1,(int) (focalLength * (this.radius / p.z)));

		int minX = Math.max(0, screenX-radius);
		int maxX = Math.min(zBuffer.length - 1, screenX+radius);
		int minY = Math.max(0, screenY-radius);
		int maxY = Math.min(zBuffer[0].length - 1, screenY+radius);
		int[] rgb = {
			color.getRed(),
			color.getGreen(),
			color.getBlue(),
			255
		};
		double iz = 1/p.z;
		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				int dx = x-screenX;
				int dy = y-screenY;
				if (dx * dx + dy * dy < radius * radius) {

					if (p.z < zBuffer[x][y]) {
						zBuffer[x][y] = p.z;
						raster.setPixel(x, y, rgb);
					}
				}
			}
		}
	}
	public Vec3 getIntersection(Vec3 rayOrigin, Vec3 rayVector){
		Vec3 l = rayOrigin.sub(point);
		
		double a = rayVector.dot(rayVector);
		double b = 2 * rayVector.dot(l);
		double c = l.dot(l) - radius*radius;


		double discriminant = b*b - 4*a*c;

		if (discriminant < 0) return null;
		double sqrtD = Math.sqrt(discriminant);

		double t0 = (-b - sqrtD) / (2 * a);
		double t1 = (-b + sqrtD) / (2 * a);

		double t = Double.POSITIVE_INFINITY;
		
		if (t0 > 0 && t0 < t) t = t0;
		if (t1 > 0 && t1 < t) t = t1;

		if (t == Double.POSITIVE_INFINITY) return null;

		return rayOrigin.add(rayVector.mul(t));
	}
}
