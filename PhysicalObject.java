
import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;

public abstract class PhysicalObject {
	public Color color;
	public Color emittedColor;
	public double specularity;
	public double luminosity;
	public PhysicalObject(Material material, Color color){
		switch (material){
			case SOLID -> {
				luminosity = 0;
				specularity = 0;
				this.emittedColor = Color.black;
				this.color = color;
			}
			case LIGHT -> {
				luminosity = 1;
				specularity = 0;
				this.emittedColor = color;
				this.color = Color.black;
			}
			case null -> {

			}
		}
	}
	public abstract Intersection getIntersection(Vec3 origin, Vec3 direction);
	public abstract void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam);
}