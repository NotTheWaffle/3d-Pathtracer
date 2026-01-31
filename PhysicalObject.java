
import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;

public abstract class PhysicalObject {
	public Color color;
	public Color emittedColor;
	public double luminosity;
	public double specularity;
	public double transparency;
	public PhysicalObject(Material material, Color color){
		switch (material){
			case SOLID -> {
				this.luminosity = 0;
				this.emittedColor = Color.black;
				this.color = color;
				this.specularity = 0.1;
				this.transparency = 0;
			}
			case LIGHT -> {
				this.luminosity = 1;
				this.emittedColor = color;
				this.color = Color.black;
				this.specularity = 0;
				this.transparency = 0;
			}
			case MIRROR -> {
				this.luminosity = 0;
				this.emittedColor = Color.BLACK;
				this.color = color;
				this.specularity = .99;
				this.transparency = 0;
			}
			case GLASS -> {
				this.luminosity = 0;
				this.emittedColor = Color.BLACK;
				this.color = color;
				this.specularity = 0;
				this.transparency = 1;
			}
			case null -> {

			}
		}
	}
	public static PhysicalObject rectangle(double x, double y, double z, double width, Color color, Material material){
		double radius = width/2;
		return new Mesh(
			new Triangle[] {
				new Triangle(
					new Point(new Vec3(x+radius, y, z-radius), 0),
					new Point(new Vec3(x-radius, y, z-radius), 0),
					new Point(new Vec3(x-radius, y, z+radius), 0),
					material, color
				),
				new Triangle(
					new Point(new Vec3(x+radius, y, z-radius), 0),
					new Point(new Vec3(x+radius, y, z+radius), 0),
					new Point(new Vec3(x-radius, y, z+radius), 0),
					material, color
				)
			},
			new AABB(x+radius, y, y+radius).addPoint(x-radius, y, z-radius)
		);
	}
	public abstract Intersection getIntersection(Vec3 origin, Vec3 direction);
	public abstract void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam);
}