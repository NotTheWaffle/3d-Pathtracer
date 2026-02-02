
import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;

public final class RectangularPrism extends PhysicalObject{
	public double maxX, maxY, maxZ, minX, minY, minZ;
	public RectangularPrism(double x0, double x1, double y0, double y1, double z0, double z1, Material material, Color color){
		super(material, color);
		this.maxX = Math.max(x0, x1);
		this.minX = Math.min(x0, x1);
		
		this.maxY = Math.max(y0, y1);
		this.minY = Math.min(y0, y1);
		
		this.maxZ = Math.max(z0, z1);
		this.minZ = Math.min(z0, z1);
	}
	public RectangularPrism(double x, double y, double z, double width, double height, double length, Material material, Color color, int flag){
		this(x-width/2, x+width/2, y-height/2, y+height/2, z-height/2, z+height/2, material, color);
	}
	@Override
	public Intersection getIntersection(Vec3 origin, Vec3 direction) {
		double tx0 = (minX - origin.x) / direction.x;
		double tx1 = (maxX - origin.x) / direction.x;
		double ty0 = (minY - origin.y) / direction.y;
		double ty1 = (maxY - origin.y) / direction.y;
		double tz0 = (minZ - origin.z) / direction.z;
		double tz1 = (maxZ - origin.z) / direction.z;

		double txMin = Math.min(tx0, tx1);
		double txMax = Math.max(tx0, tx1);
		double tyMin = Math.min(ty0, ty1);
		double tyMax = Math.max(ty0, ty1);
		double tzMin = Math.min(tz0, tz1);
		double tzMax = Math.max(tz0, tz1);

		double tenter = Math.max(txMin, Math.max(tyMin, tzMin));
		double texit = Math.min(txMax, Math.min(tyMax, tzMax));

		if (tenter > texit || texit < 0) {
			return null;
		}

		Vec3 normal = null;
		if (tenter == txMin) {
			normal = new Vec3(direction.x > 0 ? -1 : 1, 0, 0);
		} else if (tenter == tyMin) {
			normal = new Vec3(0, direction.y > 0 ? -1 : 1, 0);
		} else if (tenter == tzMin) {
			normal = new Vec3(0, 0, direction.z > 0 ? -1 : 1);
		}

		return new Intersection(origin.add(direction.mul(tenter)), this, normal);
	}
	//@Override
	public Intersection getIntersection(int a, Vec3 origin, Vec3 direction){
		double tx0 = (minX-origin.x)/direction.x;
		double tx1 = (maxX-origin.x)/direction.x;
		
		double ty0 = (minY-origin.y)/direction.y;
		double ty1 = (maxY-origin.y)/direction.y;
		
		double tz0 = (minZ-origin.z)/direction.z;
		double tz1 = (maxZ-origin.z)/direction.z;

		double txenter = Math.min(tx0, tx1);
		double txexit = Math.max(tx0, tx1);

		double tyenter = Math.min(ty0, ty1);
		double tyexit = Math.max(ty0, ty1);

		double tzenter = Math.min(tz0, tz1);
		double tzexit = Math.max(tz0, tz1);


		Vec3 normal = null;
		double tenter = Double.NEGATIVE_INFINITY;
		if (txenter > tenter){
			tenter = txenter;
			normal = new Vec3(1, 0, 0);
		}
		if (tyenter > tenter){
			tenter = tyenter;
			normal = new Vec3(0, 1, 0);
		}
		if (tzenter > tenter){
			tenter = tzenter;
			normal = new Vec3(0, 0, 1);
		}
		double texit = Math.min(txexit, Math.min(tyexit, tzexit));

		if (texit < tenter || texit < 0){
			return null;
		}
		return new Intersection(origin.add(direction.mul(tenter)), this, normal);
	}
	
	@Override
	public void render(WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam) {
		Vec3 p0 = new Vec3(maxX, maxY, maxZ);
		Vec3 p1 = new Vec3(maxX, maxY, minZ);
		Vec3 p2 = new Vec3(maxX, minY, maxZ);
		Vec3 p3 = new Vec3(maxX, minY, minZ);
		Vec3 p4 = new Vec3(minX, maxY, maxZ);
		Vec3 p5 = new Vec3(minX, maxY, minZ);
		Vec3 p6 = new Vec3(minX, minY, maxZ);
		Vec3 p7 = new Vec3(minX, minY, minZ);
		// top face
		Ray.render(p0, p1, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p1, p3, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p3, p2, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p2, p0, raster, focalLength, cx, cy, zBuffer, cam);
		// bottom face
		Ray.render(p4, p5, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p5, p7, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p7, p6, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p6, p4, raster, focalLength, cx, cy, zBuffer, cam);
		// sides
		Ray.render(p0, p4, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p1, p5, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p2, p6, raster, focalLength, cx, cy, zBuffer, cam);
		Ray.render(p3, p7, raster, focalLength, cx, cy, zBuffer, cam);
	}
}
