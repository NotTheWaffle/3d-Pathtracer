
import Math.Pair;
import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;

public class Portal extends PhysicalObject{
	public static Pair<Portal, Portal> makePortalPair(float rx, float ry){
		Pair<Portal, Portal> pair = new Pair<>(
			new Portal(new Transform(0, 0, .1f), Material.lambertian(Color.ORANGE), rx, ry),
			new Portal(new Transform(0, 0, -.1f), Material.lambertian(Color.BLUE), rx, ry)
		);
		pair.t0.sibling = pair.t1;
		pair.t1.sibling = pair.t0;
		return pair;
	}
	Portal sibling;
	final float rx;
	final float ry;
	// its a oval at z = 0
	private Portal(Transform transform, Material material, float rx, float ry){
		super(material, transform);
		this.rx = rx;
		this.ry = ry;
	}

	@Override
	public void renderRasterized(WritableRaster raster, float[][] zBuffer, Viewport camera) {
		int[] color = {255, 255, 255, 255};
		Ray.render(transform.unapplyTo(new Vec3(-rx, 0, 0)), transform.unapplyTo(new Vec3(rx, 0, 0)), raster, zBuffer, camera, color);
		Ray.render(transform.unapplyTo(new Vec3(0, -ry, 0)), transform.unapplyTo(new Vec3(0, ry, 0)), raster, zBuffer, camera, color);
	}


	@Override
	protected Intersection getLocalIntersection(Vec3 rayOrigin, Vec3 rayDirection, float minDist) {

		float t = -rayOrigin.z/rayDirection.z;
		if (t < 0) return null;
		float x = rayOrigin.x + rayDirection.x * t;
		float y = rayOrigin.y + rayDirection.y * t;
		float R = (x/rx) * (x/rx) + (y/ry) * (y/ry);

		if (R < .9){
			// so something is wrong here but only about the angle part
			return new Intersection(transform.applyTo(sibling.transform.unapplyTo(new Vec3(x, y, 0))), Material.MAGIC, transform.inv.mul(sibling.transform.rot.mul(rayDirection)), new Vec3(x, y, 0).dist(rayOrigin));
		} else if (R < 1){
			return new Intersection(new Vec3(x, y, 0), material, new Vec3(0, 0, 1), false, rayOrigin);
		}
		return null;
	}
}
