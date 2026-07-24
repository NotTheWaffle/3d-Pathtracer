
import Math.Vec3;
import java.awt.image.WritableRaster;

/**
 * Basically just an AABB with a material
 */
public final class RectangularPrism extends PhysicalObject{
	public final float maxX, maxY, maxZ, minX, minY, minZ;
	public RectangularPrism(float x0, float x1, float y0, float y1, float z0, float z1, Material material){
		this((x0+x1)/2, (y0+y1)/2, (z0+z1)/2, Math.abs(x0-x1), Math.abs(y0-y1), Math.abs(z0-z1), material, 0);
	}
	public RectangularPrism(float x, float y, float z, float width, float height, float length, Material material, int flag){
		super(material, new Transform(x, y, z));
		this.maxX = width/2;
		this.minX = -width/2;
		this.maxY = height/2;
		this.minY = -height/2;
		this.maxZ = length/2;
		this.minZ = -length/2;
	}
	/**
	 * Uses the slab intersection algorithm
	 */
	@Override
	public Intersection getLocalIntersection(Vec3 rayOrigin, Vec3 rayDirection, float minDist){
		float temp;
		float txenter = (minX-rayOrigin.x)/rayDirection.x;
		float txexit = (maxX-rayOrigin.x)/rayDirection.x;

		if (txenter > txexit){
			temp = txenter;
			txenter = txexit;
			txexit = temp;
		}

		float tyenter = (minY-rayOrigin.y)/rayDirection.y;
		float tyexit = (maxY-rayOrigin.y)/rayDirection.y;

		if (tyenter > tyexit){
			temp = tyenter;
			tyenter = tyexit;
			tyexit = temp;
		}

		float tzenter = (minZ-rayOrigin.z)/rayDirection.z;
		float tzexit = (maxZ-rayOrigin.z)/rayDirection.z;

		if (tzenter > tzexit){
			temp = tzenter;
			tzenter = tzexit;
			tzexit = temp;
		}

		Vec3 normal = null;
		float tenter = Math.max(txenter, Math.max(tyenter, tzenter));
		float texit = Math.min(txexit, Math.min(tyexit, tzexit));
		if (tenter > texit || texit < 0) return null;

		float thit;
		if (tenter > 0){
			thit = tenter;
			if (txenter == tenter){
				normal = new Vec3(rayDirection.x > 0 ? -1 : 1, 0, 0);
			} else if (tyenter == tenter){
				normal = new Vec3(0, rayDirection.y > 0 ? -1 : 1, 0);
			} else if (tzenter == tenter){
				normal = new Vec3(0, 0, rayDirection.z > 0 ? -1 : 1);
			}
		} else {
			thit = texit;
			if (txexit == texit){
				normal = new Vec3(rayDirection.x > 0 ? 1 : -1, 0, 0);
			} else if (tyexit == texit){
				normal = new Vec3(0, rayDirection.y > 0 ? 1 : -1, 0);
			} else if (tzexit == texit){
				normal = new Vec3(0, 0, rayDirection.z > 0 ? 1 : -1);
			}
		}
		if (normal == null) normal = new Vec3(1, 1, 1);
		return new Intersection(rayOrigin.add(rayDirection.mul(thit)), this.material, normal, normal.dot(rayDirection) > 0);
	}

	/**
	 * Renders a wireframe box around the rectangular prism
	 */
	@Override
	public void renderRasterized(WritableRaster raster, float[][] zBuffer, Viewport camera) {
		int[] color = {255, 255, 255, 255};
		Vec3 p0 = new Vec3(maxX, maxY, maxZ);
		Vec3 p1 = new Vec3(maxX, maxY, minZ);
		Vec3 p2 = new Vec3(maxX, minY, maxZ);
		Vec3 p3 = new Vec3(maxX, minY, minZ);
		Vec3 p4 = new Vec3(minX, maxY, maxZ);
		Vec3 p5 = new Vec3(minX, maxY, minZ);
		Vec3 p6 = new Vec3(minX, minY, maxZ);
		Vec3 p7 = new Vec3(minX, minY, minZ);

		p0 = transform.unapplyTo(p0);
		p1 = transform.unapplyTo(p1);
		p2 = transform.unapplyTo(p2);
		p3 = transform.unapplyTo(p3);
		p4 = transform.unapplyTo(p4);
		p5 = transform.unapplyTo(p5);
		p6 = transform.unapplyTo(p6);
		p7 = transform.unapplyTo(p7);

		// top face
		Ray.render(p0, p1, raster, zBuffer, camera, color);
		Ray.render(p1, p3, raster, zBuffer, camera, color);
		Ray.render(p3, p2, raster, zBuffer, camera, color);
		Ray.render(p2, p0, raster, zBuffer, camera, color);
		// bottom face
		Ray.render(p4, p5, raster, zBuffer, camera, color);
		Ray.render(p5, p7, raster, zBuffer, camera, color);
		Ray.render(p7, p6, raster, zBuffer, camera, color);
		Ray.render(p6, p4, raster, zBuffer, camera, color);
		// sides
		Ray.render(p0, p4, raster, zBuffer, camera, color);
		Ray.render(p1, p5, raster, zBuffer, camera, color);
		Ray.render(p2, p6, raster, zBuffer, camera, color);
		Ray.render(p3, p7, raster, zBuffer, camera, color);
	}

	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof RectangularPrism r){
			return
				r.minX == minX && r.maxX == maxX && r.minY == minY && r.maxY == maxY && r.minZ == minZ && r.maxZ == maxZ
				&& r.material.equals(material) && r.transform.equals(transform);
		} else {
			return false;
		}
	}
	@Override
	public int hashCode(){
		return Float.hashCode(minX) ^ Float.hashCode(minY) ^ Float.hashCode(minZ) ^ Float.hashCode(maxX) ^ Float.hashCode(maxY) ^ Float.hashCode(maxZ) ^ material.hashCode() ^ transform.hashCode();
	}
	@Override
	public String toString(){
		return "Rectangle of size "+new Vec3(minX, minY, minZ)+" to "+new Vec3(maxX, maxY, maxZ)+" with material: "+material+" at "+transform;
	}
}
