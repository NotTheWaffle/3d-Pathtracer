import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Random;

/*
 * so itd use less data to have a list of indicies and data (just as its stored in the .obj)
 * but it requries more lookups. though it could benefit from cache locality?
 * TODO maybe implement both and contrast
 * Mesh
 */

/**
 * A collection of objects united by a BVH and a shared material.
 * Mesh
 */
public class Mesh extends PhysicalObject{
	public final BVH bvh;
	public final DeficientPhysicalObject[] objects;
	public Mesh(List<? extends DeficientPhysicalObject> objects, Material material, Transform transform, boolean sahConstruction){
		super(material, transform);
		this.objects = objects.toArray(DeficientPhysicalObject[]::new);
		if (sahConstruction){
			this.bvh = new BVH(objects);
		} else {
			this.bvh = BVH.generateBVHNaive(objects);
		}
	}
	@Override
	public void renderRasterized(WritableRaster raster, float[][] zBuffer, Viewport camera) {
		int[] color = new int[] {(int) (super.material.reflectionColor[0]*255),(int) (super.material.reflectionColor[1]*255),(int) (super.material.reflectionColor[2]*255)};
		if (objects.length > 100_000){
			// with a lot of objects just treat each one as a point
			for (DeficientPhysicalObject object : objects){
				Vec3 point = camera.applyTo(transform.unapplyTo(object.center()));
				int x1 = (int) camera.getX(point);
				if (x1 < 0 || x1 >= camera.screenWidth) continue;
				int y1 = (int) camera.getY(point);
				if (y1 < 0 || y1 >= camera.screenHeight) continue;
				if (point.z < 0) continue;

				zBuffer[x1][y1] = point.z;
				raster.setPixel(x1, y1, color);
			}
		} else {
			for (DeficientPhysicalObject object : objects){
				Random random = new Random(object.hashCode());
				color[0] = random.nextInt(256);
				color[1] = random.nextInt(256);
				color[2] = random.nextInt(256);
				object.render(raster, zBuffer, camera, super.transform, color);
			}
		}
	}
	@Override
	public Intersection getLocalIntersection(Vec3 origin, Vec3 direction, float minDist){
		Intersection intersection = bvh.getDeficientIntersection(origin, direction, minDist);
		if (intersection == null) return null;
		intersection.material = this.material;
		return intersection;
	}
	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof Mesh m){
			return m.bvh.equals(bvh);
		} else {
			return false;
		}
	}
}
