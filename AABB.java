
import Math.Vec3;
import java.util.List;
/**
 * An Axis Aligned Bounding Box. Provides methods for growing to encompass certain shapes. Unable to shrink
 */
public final class AABB{
	public float maxX, maxY, maxZ, minX, minY, minZ;
	public AABB(){
		maxX = maxY = maxZ = Float.NEGATIVE_INFINITY;
		minX = minY = minZ = Float.POSITIVE_INFINITY;
	}
	public AABB(float x, float y, float z){
		minX = maxX = x;
		minY = maxY = y;
		minZ = maxZ = z;
	}
	public AABB(float minX, float minY, float minZ, float maxX, float maxY, float maxZ){
		this.minX = Math.min(minX, maxX);
		this.maxX = Math.max(minX, maxX);
		this.minY = Math.min(minY, maxY);
		this.maxY = Math.max(minY, maxY);
		this.minZ = Math.min(minZ, maxZ);
		this.maxZ = Math.max(minZ, maxZ);
	}
	public AABB(Vec3 a, Vec3 b, Vec3 c){
		this();
		grow(a);
		grow(b);
		grow(c);
	}

	public AABB(List<Vec3> points){
		this();
		for (Vec3 p : points){
			grow(p);
		}
	}

	/**
	 * Grows this AABB by a small amount in all directions
	 * @return a reference to this object
	 */
	public AABB grow(){
		return grow(PhysicalObject.EPSILON);
	}

	/**
	 * Grows this AABB by {@code epsilon} in all directions
	 * @param epsilon
	 * @return a reference to this object
	 */
	public AABB grow(float epsilon){
		this.minX -= epsilon;
		this.minY -= epsilon;
		this.minZ -= epsilon;

		this.maxX += epsilon;
		this.maxY += epsilon;
		this.maxZ += epsilon;
		return this;
	}
	/**
	 * Grows this AABB to encompass the specified AABB
	 * @param that
	 * @return a reference to this object
	 */
	public AABB grow(AABB that){
		this.minX = Math.min(this.minX, that.minX);
		this.maxX = Math.max(this.maxX, that.maxX);
		this.minY = Math.min(this.minY, that.minY);
		this.maxY = Math.max(this.maxY, that.maxY);
		this.minZ = Math.min(this.minZ, that.minZ);
		this.maxZ = Math.max(this.maxZ, that.maxZ);
		return this;
	}

	/**
	 * Grows this AABB to encompass all objects specified
	 * @param objects
	 * @return a reference to this object
	 */
	public AABB grow(Iterable<? extends DeficientPhysicalObject> objects){
		for (DeficientPhysicalObject object : objects){
			if (object != null) grow(object);
		}
		return this;
	}

	/**
	 * Grows this AABB to encompass all objects specified
	 * @param objects
	 * @return a reference to this object
	 */
	public AABB grow(DeficientPhysicalObject[] objects){
		for (DeficientPhysicalObject object : objects){
			grow(object);
		}
		return this;
	}

	/**
	 * Grows this {@code AABB} to contain the specified point
	 * @param x
	 * @param y
	 * @param z
	 * @return a reference to this object
	 */
	public AABB grow(float x, float y, float z){
		maxX = Math.max(maxX, x);
		minX = Math.min(minX, x);

		maxY = Math.max(maxY, y);
		minY = Math.min(minY, y);

		maxZ = Math.max(maxZ, z);
		minZ = Math.min(minZ, z);
		return this;
	}
	/**
	 * Grows this {@code AABB} to contain the specified point
	 * @param vec
	 * @return a reference to this object
	 */
	public AABB grow(Vec3 vec){
		return grow(vec.x, vec.y, vec.z);
	}
	/**
	 * Grows this {@code AABB} to contain the specified object. Calls {@code object.grow()} on this
	 * @param object
	 * @return a reference to this object
	 */
	public AABB grow(DeficientPhysicalObject object){
		object.grow(this);
		return this;
	}


	/**
	 * Gets the surface area of this AABB when treated as a rectangular prism.
	 * @return an unsigned surface area of this AABB
	 */
	public float getSurfaceArea(){
		float xRange = maxX-minX;
		float yRange = maxY-minY;
		float zRange = maxZ-minZ;
		return 2 * (xRange * yRange + xRange * zRange + yRange * zRange);
	}
	/**
	 * Checks whether a specified point is contained in this AABB.
	 * @param point
	 * @return true if the specified point is contained in this AABB, false otherwise
	 */
	public boolean isContained(Vec3 point){
		// half open checks here to function with octrees
		return minX <= point.x && point.x < maxX && minY <= point.y && point.y < maxY && minZ <= point.z && point.z < maxZ;
	}
	/**
	 * @param origin
	 * @param direction
	 * @return the distance to this {@code AABB} starting at {@code origin} moving in {@code direction}, or -1 if this AABB doesn't collide with the Ray
	 */
	public float testIntersection(final Vec3 origin, final Vec3 direction){
		float txenter = (minX-origin.x)/direction.x;
		float txexit = (maxX-origin.x)/direction.x;

		if (txenter > txexit){
			final float temp = txenter;
			txenter = txexit;
			txexit = temp;
		}

		float tyenter = (minY-origin.y)/direction.y;
		float tyexit = (maxY-origin.y)/direction.y;

		if (tyenter > tyexit){
			final float temp = tyenter;
			tyenter = tyexit;
			tyexit = temp;
		}

		float tzenter = (minZ-origin.z)/direction.z;
		float tzexit = (maxZ-origin.z)/direction.z;

		if (tzenter > tzexit){
			final float temp = tzenter;
			tzenter = tzexit;
			tzexit = temp;
		}

		final float tenter = Math.max(txenter, Math.max(tyenter, tzenter));
		final float texit = Math.min(txexit, Math.min(tyexit, tzexit));

		if (tenter <= texit && texit > 0){
			// avoid 0 exactly just cuz
			return Math.max(tenter, PhysicalObject.EPSILON);
		}
		return -1;
	}

	@Override
	public int hashCode(){
		return Float.hashCode(minX) ^ Float.hashCode(maxX) ^ Float.hashCode(minY) ^ Float.hashCode(maxY) ^ Float.hashCode(minZ) ^ Float.hashCode(maxZ);
	}

	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof AABB aabb){
			return aabb.minX == this.minX && aabb.maxX == this.maxX && aabb.minY == this.minY && aabb.maxY == this.maxY && aabb.minZ == this.minZ && aabb.maxZ == this.maxZ;
		} else {
			return false;
		}
	}

	@Override
	public String toString(){
		return new Vec3(minX,minY,minZ)+" to "+new Vec3(maxX,maxY,maxZ);
	}
}
