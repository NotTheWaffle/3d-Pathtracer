
import Math.Vec3;
import java.util.List;

public final class AABB{
	public double maxX, maxY, maxZ, minX, minY, minZ;
	public AABB(){
		maxX = minX = Double.NaN;
		maxY = minY = Double.NaN;
		maxZ = minZ = Double.NaN;
	}
	public AABB(double x, double y, double z){
		minX = maxX = x;
		minY = maxY = y;
		minZ = maxZ = z;
	}
	public AABB(List<Vec3> points){
		this();
		for (Vec3 p : points){
			addPoint(p);
		}
	}
	public AABB addTriangles(Iterable<Triangle> tris){
		for (Triangle tri : tris){
			addPoint(tri.p1);
			addPoint(tri.p2);
			addPoint(tri.p3);
		}
		return this;

	}
	public AABB addTriangles(Triangle[] tris){
		for (Triangle tri : tris){
			addPoint(tri.p1);
			addPoint(tri.p2);
			addPoint(tri.p3);
		}
		return this;
	}
	public double testIntersection(Vec3 origin, Vec3 direction){
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

		double tenter = Math.max(txenter, Math.max(tyenter, tzenter));
		double texit = Math.min(txexit, Math.min(tyexit, tzexit));

		if (tenter <= texit && texit > 0){
			return Math.max(tenter, 0);
		}
		return -1;
	}
	/**
	 * @param x
	 * @param y
	 * @param z
	 * @return the AABB itself, allowing chained calls
	 */
	public AABB addPoint(double x, double y, double z){
		if (!(x < maxX)) maxX = x;
		if (!(x > minX)) minX = x;

		if (!(y < maxY)) maxY = y;
		if (!(y > minY)) minY = y;

		if (!(z < maxZ)) maxZ = z;
		if (!(z > minZ)) minZ = z;
		return this;
	}
	public AABB addPoint(Vec3 vec){
		return addPoint(vec.x, vec.y, vec.z);
	}
	public AABB addPoint(Point point){
		return addPoint(point.pos.x, point.pos.y, point.pos.z);
	}
}
