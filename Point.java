
import java.awt.Color;

public class Point {
	public final Vec3 point;
	public final Color color;
	public final double radius;
	public Point(Vec3 p, double radius){
		this.point = p;
		this.color = new Color((int)(Math.random()*16777216));
		this.radius = radius;
	}
	public void render(java.awt.image.WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam) {
		Vec3 p = cam.applyTo(this.point);
		if (p.z >= 0) return;
		
		int screenX = (int)( focalLength * p.x / p.z) + cx;
		int screenY = (int)(-focalLength * p.y / p.z) + cy;


		int radius = (int) (focalLength * (this.radius / -p.z));

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

					if (iz < zBuffer[x][y]) {
						zBuffer[x][y] = iz;
						raster.setPixel(x, y, rgb);
					}
				}
			}
		}
	}
}
