
import Math.Vec3;
import java.awt.image.WritableRaster;

public final class RectangularPrism extends PhysicalObject{
	public final double maxX, maxY, maxZ, minX, minY, minZ;
	public RectangularPrism(double x0, double x1, double y0, double y1, double z0, double z1, Material material){
		super(material);
		this.maxX = Math.max(x0, x1);
		this.minX = Math.min(x0, x1);
		
		this.maxY = Math.max(y0, y1);
		this.minY = Math.min(y0, y1);
		
		this.maxZ = Math.max(z0, z1);
		this.minZ = Math.min(z0, z1);
	}
	public RectangularPrism(double x, double y, double z, double width, double height, double length, Material material, int flag){
		this(x-width/2, x+width/2, y-height/2, y+height/2, z-length/2, z+length/2, material);
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
		
		double tyenter = (minY-origin.y)/direction.y;
		double tyexit = (maxY-origin.y)/direction.y;

		if (tyenter > tyexit){
			temp = tyenter;
			tyenter = tyexit;
			tyexit = temp;
		}
		
		double tzenter = (minZ-origin.z)/direction.z;
		double tzexit = (maxZ-origin.z)/direction.z;

		if (tzenter > tzexit){
			temp = tzenter;
			tzenter = tzexit;
			tzexit = temp;
		}

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
		return new Intersection(origin.add(direction.mul(tenter)), this, normal, false);
	}
	
	@Override
	public void render(WritableRaster raster, double[][] zBuffer, Viewport camera) {
		Vec3 p0 = new Vec3(maxX, maxY, maxZ);
		Vec3 p1 = new Vec3(maxX, maxY, minZ);
		Vec3 p2 = new Vec3(maxX, minY, maxZ);
		Vec3 p3 = new Vec3(maxX, minY, minZ);
		Vec3 p4 = new Vec3(minX, maxY, maxZ);
		Vec3 p5 = new Vec3(minX, maxY, minZ);
		Vec3 p6 = new Vec3(minX, minY, maxZ);
		Vec3 p7 = new Vec3(minX, minY, minZ);
		// top face
		Ray.render(p0, p1, raster, zBuffer, camera);
		Ray.render(p1, p3, raster, zBuffer, camera);
		Ray.render(p3, p2, raster, zBuffer, camera);
		Ray.render(p2, p0, raster, zBuffer, camera);
		// bottom face
		Ray.render(p4, p5, raster, zBuffer, camera);
		Ray.render(p5, p7, raster, zBuffer, camera);
		Ray.render(p7, p6, raster, zBuffer, camera);
		Ray.render(p6, p4, raster, zBuffer, camera);
		// sides
		Ray.render(p0, p4, raster, zBuffer, camera);
		Ray.render(p1, p5, raster, zBuffer, camera);
		Ray.render(p2, p6, raster, zBuffer, camera);
		Ray.render(p3, p7, raster, zBuffer, camera);
	}
}
