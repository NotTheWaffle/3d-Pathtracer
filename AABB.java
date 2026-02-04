
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
	public boolean testIntersection(Vec3 origin, Vec3 direction){
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

		double tenter = Math.max(txenter, Math.max(tyenter, tzenter));
		double texit = Math.min(txexit, Math.min(tyexit, tzexit));

		if (texit < tenter){
			return false;
		}
		return texit >= 0;
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
