

import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;
import java.util.Random;

public final class Ray {
	private Ray(){}
	public static double[] trace(Vec3 origin, Vec3 direction, Environment env, int maxDepth, Random random){
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
			Vec3 specularDirection = direction.sub(intersection.normal.mul(2 * direction.dot(intersection.normal))).normalize();
			Vec3 diffuseDirection = new Vec3(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5);
			if (diffuseDirection.dot(intersection.normal) < 0) diffuseDirection = diffuseDirection.mul(-1);
			Vec3 nextDirection;
			if (random.nextDouble() < collisionObject.specularityChance){
				nextDirection = lerp(diffuseDirection, specularDirection, collisionObject.specularity).normalize();
			} else {
				nextDirection = diffuseDirection.normalize();
			}
			origin = intersection.pos;
			direction = nextDirection;
			
			
			//calculate colors
			Color emittedColor = collisionObject.emissionColor;
			Color color = collisionObject.reflectionColor;

			
			double[] emittedLight = {
				collisionObject.emissionStrength*emittedColor.getRed()/255.0,
				collisionObject.emissionStrength*emittedColor.getGreen()/255.0,
				collisionObject.emissionStrength*emittedColor.getBlue()/255.0,
			};
			incomingLight[0] += emittedLight[0] * rayColor[0];
			incomingLight[1] += emittedLight[1] * rayColor[1];
			incomingLight[2] += emittedLight[2] * rayColor[2];

			double[] c = {
				color.getRed() / 255.0,
				color.getGreen() / 255.0,
				color.getBlue() / 255.0
			};
			rayColor[0] *= c[0];
			rayColor[1] *= c[1];
			rayColor[2] *= c[2];
		}
		return incomingLight;
	}

	public static Vec3 lerp(final Vec3 start, final Vec3 end, final double a){
		final double ia = 1-a;
		return new Vec3(
			start.x * ia + end.x * a,
			start.y * ia + end.y * a,
			start.z * ia + end.z * a
		);
	}


	public static void render(Vec3 start, Vec3 end, WritableRaster raster, double focalLength, int cx, int cy, double[][] zBuffer, Transform cam) {
		start = Point.project(start, cam, focalLength);
		end = Point.project(end, cam, focalLength);

		if (start.z < 0 || end.z < 0) return;
		
		double x1 = (start.x+cx);
		double y1 = (cy-start.y);

		double x2 = (end.x+cx);
		double y2 = (cy-end.y);

		if (x1 > x2){
			double temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		double c = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		double dx = (x2-x1)/c;
		double dy = (y2-y1)/c;

		

		int[] color = {
			255,
			255,
			255,
			255
		};
		for (double x = x1, y = y1; x < x2; x+=dx, y+=dy){
			if (x < 0 || x > 512 || y < 0 || y > 512){
				break;
			}
			raster.setPixel((int)x, (int)y, color);
		}
		for (double x = x2, y = y2; x > x1; x-=dx, y-=dy){
			if (x < 0 || x > 512 || y < 0 || y > 512){
				break;
			}
			raster.setPixel((int)x, (int)y, color);
		}
	}
}
