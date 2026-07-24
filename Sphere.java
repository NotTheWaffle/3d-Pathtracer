
import Math.FloatMath;
import Math.Vec3;
import java.awt.image.WritableRaster;

public class Sphere extends PhysicalObject{
	public final float radius;

	public Sphere(float radius, Material material){
		this(new Transform(), radius, material);
	}
	public Sphere(Vec3 origin, float radius, Material material){
		this(new Transform().translate(origin.x, origin.y, origin.z), radius, material);
	}
	public Sphere(Transform transform, float radius, Material material){
		super(material, transform);
		this.radius = radius;
	}
	/**
	 * Very innaccurate rasterization
	 */
	@Override
	public void renderRasterized(WritableRaster raster, float[][] zBuffer, Viewport camera) {
		Vec3 p = camera.applyTo(transform.translation);
		if (p.z < 0) return;

		int screenX = (int) camera.getX(p);
		int screenY = (int) camera.getY(p);


		int projectedRadius = Math.max(1, (int) (camera.focalLength * (this.radius / p.z)));

		int minX = Math.max(0, screenX-projectedRadius);
		int maxX = Math.min(zBuffer.length - 1, screenX+projectedRadius);
		int minY = Math.max(0, screenY-projectedRadius);
		int maxY = Math.min(zBuffer[0].length - 1, screenY+projectedRadius);

		int[] rgb = {
			(int) (255 * (material.reflectionColor[0]+material.emissionColor[0])),
			(int) (255 * (material.reflectionColor[1]+material.emissionColor[1])),
			(int) (255 * (material.reflectionColor[2]+material.emissionColor[2])),
			255
		};

		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				int dx = x-screenX;
				int dy = y-screenY;
				if (dx * dx + dy * dy < projectedRadius * projectedRadius) {
					if (p.z < zBuffer[x][y]) {
						zBuffer[x][y] = p.z;
						raster.setPixel(x, y, rgb);
					}
				}
			}
		}
	}


	@Override
	public Intersection getLocalIntersection(Vec3 rayOrigin, Vec3 rayDirection, float minDist){
		Vec3 l = rayOrigin;

		float b = 2 * rayDirection.dot(l);
		float c = l.dot(l) - radius*radius;


		float discriminant = b*b - 4*c;

		if (discriminant < 0) return null;
		float sqrtD = FloatMath.sqrt(discriminant);

		float t0 = (-b - sqrtD) / 2;
		float t1 = (-b + sqrtD) / 2;

		float t = Float.POSITIVE_INFINITY;

		if (t0 > EPSILON && t0 < t) t = t0;
		if (t1 > EPSILON && t1 < t) t = t1;

		if (t == Float.POSITIVE_INFINITY) return null;
		Vec3 intersectionPoint = rayOrigin.add(rayDirection.mul(t));
		Vec3 normal = intersectionPoint.normalize();

		return new Intersection(intersectionPoint, this.material, normal, normal.dot(rayDirection) > 0);
	}

	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof Sphere s){
			return s.radius == radius && s.material.equals(material) && s.transform.equals(transform);
		} else {
			return false;
		}
	}
	@Override
	public int hashCode(){
		return Float.hashCode(radius) ^ material.hashCode() ^ transform.hashCode();
	}
	@Override
	public String toString(){
		return "Sphere of radius "+radius+" with material: "+material+" at "+transform;
	}
}