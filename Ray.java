

import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;
import java.util.Random;

public final class Ray {
	private Ray(){}
	public final static PhysicalObject sun = new Sphere(null, 0, Material.LIGHT);
	public final static Vec3 sunVec = new Vec3(0, 1, 0).normalize();
	private final static PhysicalObject err = new Sphere(null, 0, new Material(1, Color.GREEN, Color.BLACK, 0, 0, 0));
	public static double[] trace(Vec3 origin, Vec3 direction, Environment env, int maxDepth, Random random){
		final double EPSILON = 1e-8;
		double[] rayColor = {1.0, 1.0, 1.0};
		double[] incomingLight = {0.0, 0.0, 0.0};
		
		for (int i = 0; i < maxDepth; i++){
			// find intersection
			Intersection intersection = null;
			for (PhysicalObject p : env.physicalObjects){
				Intersection localIntersection = p.getIntersection(origin, direction);
				if (localIntersection == null || (intersection != null && origin.dist(intersection.pos) < origin.dist(localIntersection.pos))) continue;
				if (localIntersection.normal.dot(direction) > 0){
					localIntersection = new Intersection(localIntersection.pos, err, localIntersection.normal);
				}
				intersection = localIntersection;
			}

			//break if no intersection
			if (intersection == null) {
				if (direction.dist(sunVec) < .75){
					intersection = new Intersection(Vec3.ZERO_VEC, sun, Vec3.ZERO_VEC);
				} else {
					break;
				}
			}
			
			
			PhysicalObject object = intersection.object;

			
			
			// find next ray
			Vec3 nextDirection;
			boolean applyColor = true;
			if (object.transparency == 0){
				Vec3 diffuseDirection = new Vec3(random.nextDouble() - .5, random.nextDouble() - .5, random.nextDouble() - .5);
				if (diffuseDirection.dot(intersection.normal) < 0) diffuseDirection = diffuseDirection.mul(-1);
				
				if (random.nextDouble() < object.specularityChance){
					applyColor = false;
					Vec3 specularDirection = direction.sub(intersection.normal.mul(2 * direction.dot(intersection.normal))).normalize();
					nextDirection = lerp(diffuseDirection, specularDirection, object.specularity).normalize();
				} else {
					nextDirection = diffuseDirection.normalize();
				}
			} else {
				nextDirection = direction;
			}
			origin = intersection.pos;
			direction = nextDirection;

			double strength = intersection.normal.dot(direction);
			
			//calculate colors
			// emissionStrengh * emissionColor = emitted light, multiply with ray color to get the intersection of the colors
			if (applyColor){
				incomingLight[0] += (object.emissionStrength * object.emissionColor[0]) * rayColor[0];
				incomingLight[1] += (object.emissionStrength * object.emissionColor[1]) * rayColor[1];
				incomingLight[2] += (object.emissionStrength * object.emissionColor[2]) * rayColor[2];
				
				rayColor[0] *= object.reflectionColor[0] * strength * 2;
				rayColor[1] *= object.reflectionColor[1] * strength * 2;
				rayColor[2] *= object.reflectionColor[2] * strength * 2;
			}

			if (rayColor[0] < .01 && rayColor[1] < .01 && rayColor[2] < .01){
				break;
			}
		}
	//	incomingLight[0] = Math.min(.9, incomingLight[0]);
	//	incomingLight[1] = Math.min(.9, incomingLight[1]);
	//	incomingLight[2] = Math.min(.9, incomingLight[2]);
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
		Vec3 projectedStart = cam.applyTo(start);
		Vec3 projectedEnd = cam.applyTo(end);
		
		
		if (projectedStart.z < 0 || projectedEnd.z < 0) return;

		
		double x1 = ((focalLength * projectedStart.x / projectedStart.z)+cx);
		double y1 = (cy-(focalLength * projectedStart.y / projectedStart.z));

		double x2 = ((focalLength * projectedEnd.x / projectedEnd.z)+cx);
		double y2 = (cy-(focalLength * projectedEnd.y / projectedEnd.z));

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

		int width = raster.getWidth();
		int height = raster.getHeight();

		int[] color = {
			255,
			255,
			255,
			255
		};
		for (double x = x1, y = y1; x < x2; x+=dx, y+=dy){
			if (x < 0 || (int)x >= width || y < 0 || (int)y >= height){
				break;
			}
			raster.setPixel((int)x, (int)y, color);
		}
		for (double x = x2, y = y2; x > x1; x-=dx, y-=dy){
			if (x < 0 || (int)x >= width || y < 0 || y >= height){
				break;
			}
			raster.setPixel((int)x, (int)y, color);
		}
	}
}
