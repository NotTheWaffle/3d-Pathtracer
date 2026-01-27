

import java.util.Random;

import Math.Vec3;

public final class Ray {
	private Ray(){}
	public static int[] getColor(Vec3 origin, Vec3 direction, Environment env, int depth){
		int[] color = {0, 0, 0, 255};
		if (depth == 0) {color[0] = color[1] = color[2] = 25; return color;}
		Intersection intersection = null;
		for (Sphere p : env.lights){
			Vec3 localIntersection = p.getIntersection(origin, direction);
			if (localIntersection == null) continue;
			if (intersection == null || origin.dist(intersection.pos) > origin.dist(localIntersection)){
				intersection = new Intersection(Intersection.Material.LIGHT, localIntersection);
			}
		}
		
		for (Triangle tri : env.mesh.triangles()){
			Vec3 localIntersection = tri.getIntersection(origin, direction);
			if (localIntersection == null) continue;
			if (intersection == null || origin.dist(intersection.pos) > origin.dist(localIntersection)){
				intersection = new Intersection(localIntersection, tri);
			}
		}
		if (intersection == null) {
			color[0] = color[1] = color[2] = 0;
		} else if (intersection.mat == Intersection.Material.LIGHT){
			color[0] = color[1] = color[2] = 255;
		} else if (intersection.mat == Intersection.Material.SOLID){
			int detail = 25;
			Random random = new Random();
			for (int i = 0; i < detail; i++){
				Vec3 nextVec = new Vec3(1-2*random.nextDouble(), 1-2*random.nextDouble(), 1-2*random.nextDouble());
				if (nextVec.dot(intersection.collisionEntity.normal()) < 0) nextVec = nextVec.mul(-1);
				int[] col = getColor(intersection.pos, nextVec, env, depth-1);
				color[0] += col[0];
				color[1] += col[1];
				color[2] += col[2];
			}
			color[0]/=detail;
			color[1]/=detail;
			color[2]/=detail;
		}
		return color;
	}

}
