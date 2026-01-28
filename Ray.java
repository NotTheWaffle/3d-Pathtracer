

import Math.Vec3;
import java.awt.Color;
import java.util.Random;

public final class Ray {
	private Ray(){}
	public static double[] getColor(Vec3 origin, Vec3 direction, Environment env, int depth, Random random){
		double[] rayColor = {1.0, 1.0, 1.0};
		double[] incomingLight = {0.0, 0.0, 0.0};

		for (int i = 0; i < depth; i++){
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
			if (random.nextDouble() < collisionObject.specularity) {
				nextDirection = direction.sub(intersection.normal.mul(2 * direction.dot(intersection.normal))).normalize();
			} else {
				nextDirection = new Vec3(1-2*random.nextDouble(), 1-2*random.nextDouble(), 1-2*random.nextDouble());
				if (nextDirection.dot(intersection.normal) < 0) nextDirection = nextDirection.mul(-1);
			}
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
		return incomingLight;
	}

}
