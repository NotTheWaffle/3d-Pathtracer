

import Math.Vec3;
import java.awt.Color;
import java.util.Random;

public final class Ray {
	private Ray(){}
	public static double[] trace(Vec3 origin, Vec3 direction, Environment env, int maxDepth, Random random){
		final double EPSILON = 1e-8;
		double[] rayColor = {1.0, 1.0, 1.0};
		double[] incomingLight = {0.0, 0.0, 0.0};
		
		for (int i = 0; i < maxDepth; i++){
			// find intersection
			Intersection intersection = null;
			for (PhysicalObject p : env.physicalObjects){
				Intersection localIntersection = p.getIntersection(origin, direction);
				if (localIntersection == null) continue;
				if (intersection == null || origin.dist(intersection.pos) > origin.dist(localIntersection.pos)){
					intersection = localIntersection;
				}
			}
			//break if no intersection
			if (intersection == null) {
				break;
			}
			
			PhysicalObject collisionObject = intersection.object;
			
			//find next ray
			Vec3 nextDirection;
			if (collisionObject.transparency > 0){
				
				
			} else {
				nextDirection = direction.sub(intersection.normal.mul(2 * direction.dot(intersection.normal))).normalize();
				double iv = collisionObject.specularity;
				double variance = 1-iv;
				nextDirection = new Vec3(
					nextDirection.x*iv+(random.nextDouble()-.5)*variance,
					nextDirection.y*iv+(random.nextDouble()-.5)*variance,
					nextDirection.z*iv+(random.nextDouble()-.5)*variance
				).normalize();
				origin = intersection.pos;
				direction = nextDirection;
			
			
			//calculate colors
			Color emittedColor = collisionObject.emittedColor;
			Color color = collisionObject.color;

			double[] c = {
				color.getRed() / 255.0,
				color.getGreen() / 255.0,
				color.getBlue() / 255.0
			};
			double[] emittedLight = {
				collisionObject.luminosity*emittedColor.getRed()/255.0,
				collisionObject.luminosity*emittedColor.getGreen()/255.0,
				collisionObject.luminosity*emittedColor.getBlue()/255.0,
			};
			incomingLight[0] += emittedLight[0] * rayColor[0];
			incomingLight[1] += emittedLight[1] * rayColor[1];
			incomingLight[2] += emittedLight[2] * rayColor[2];

			rayColor[0] *= c[0];
			rayColor[1] *= c[1];
			rayColor[2] *= c[2];
			}
		}
		return incomingLight;
	}

}
