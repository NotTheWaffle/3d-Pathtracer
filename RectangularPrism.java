
import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;

public final class RectangularPrism extends PhysicalObject{
	public final double maxX, maxY, maxZ, minX, minY, minZ;
	public RectangularPrism(double x0, double x1, double y0, double y1, double z0, double z1, Color color, Material material){
		super(color, material);
		this.maxX = Math.max(x0, x1);
		this.minX = Math.min(x0, x1);
		
		this.maxY = Math.max(y0, y1);
		this.minY = Math.min(y0, y1);
		
		this.maxZ = Math.max(z0, z1);
		this.minZ = Math.min(z0, z1);
	}
	public RectangularPrism(double x, double y, double z, double width, double height, double length, Color color, Material material, int flag){
		this(x-width/2, x+width/2, y-height/2, y+height/2, z-length/2, z+length/2, color, material);
	}
	@Override
	public Intersection getIntersection(Vec3 origin, Vec3 direction){
		final double EPSILON = 1e-8;
		double temp;
		double txenter = (minX-origin.x)/direction.x;
		double txexit = (maxX-origin.x)/direction.x;

		if (txenter > txexit){
			temp = txenter;
			txenter = txexit;
			txexit = temp;
		}

		
		double ty0 = (minY-origin.y)/direction.y;
		double ty1 = (maxY-origin.y)/direction.y;
		
		double tz0 = (minZ-origin.z)/direction.z;
		double tz1 = (maxZ-origin.z)/direction.z;


		double tyenter = Math.min(ty0, ty1);
		double tyexit = Math.max(ty0, ty1);

		double tzenter = Math.min(tz0, tz1);
		double tzexit = Math.max(tz0, tz1);


		Vec3 normal = null;
		double tenter = Double.NEGATIVE_INFINITY;
		if (txenter > tenter){
			tenter = txenter;
			normal = new Vec3(direction.x > 0 ? -1 : 1, 0, 0);
		}
		if (tyenter > tenter){
			tenter = tyenter;
			normal = new Vec3(0, direction.y > 0 ? -1 : 1, 0);
		}
		if (tzenter > tenter){
			tenter = tzenter;
			normal = new Vec3(0, 0, direction.z > 0 ? -1 : 1);
		}
		double texit = Math.min(txexit, Math.min(tyexit, tzexit));

		if (tenter - texit > EPSILON || tenter < 0){
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
