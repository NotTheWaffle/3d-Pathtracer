

import Math.Vec3;
import java.awt.Color;
import java.awt.image.WritableRaster;
import java.util.Random;

public final class Ray {
	private Ray(){}
	private final static PhysicalObject err = new Sphere(null, 0, Material.light(Color.GREEN));
	public static double[] trace(Vec3 origin, Vec3 direction, Environment env, int maxDepth, Random random){
		double[] rayColor = {1.0, 1.0, 1.0};
		double[] incomingLight = {0.0, 0.0, 0.0};
		
		for (int i = 0; i < maxDepth; i++){
			// find intersection
			Intersection intersection = null;
			for (PhysicalObject p : env.physicalObjects){
				Intersection localIntersection = p.getIntersection(origin, direction);
				if (localIntersection == null || (intersection != null && origin.dist(intersection.pos) < origin.dist(localIntersection.pos))) continue;
				if (localIntersection.normal.dot(direction) > 0){
					localIntersection = new Intersection(localIntersection.pos, err, localIntersection.normal, false);
				}
				intersection = localIntersection;
			}

			// background light
			if (intersection == null) {
				if (direction.dist(env.sunVec) < .75){
					intersection = new Intersection(Vec3.ZERO_VEC, env.sun, Vec3.ZERO_VEC, false);
				} else {
					break;
				}
			}
			
			
			PhysicalObject object = intersection.object;

			
			
			// find next ray
			Vec3 nextDirection;
			boolean specularReflection = random.nextDouble() < object.specularityChance;
			if (object.transparency == 0){
				Vec3 diffuseDirection = intersection.normal.add(Vec3.random(random)).normalize();
				
				if (specularReflection){
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

			
			//calculate colors
			// emissionStrengh * emissionColor = emitted light, multiply with ray color to get the intersection of the colors
			if (!specularReflection){
				incomingLight[0] += (object.emissionStrength * object.emissionColor[0]) * rayColor[0];
				incomingLight[1] += (object.emissionStrength * object.emissionColor[1]) * rayColor[1];
				incomingLight[2] += (object.emissionStrength * object.emissionColor[2]) * rayColor[2];
				
				rayColor[0] *= object.reflectionColor[0];
				rayColor[1] *= object.reflectionColor[1];
				rayColor[2] *= object.reflectionColor[2];
			}

			if (rayColor[0] < .01 && rayColor[1] < .01 && rayColor[2] < .01){
				break;
			}
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


	public static void render(Vec3 start, Vec3 end, WritableRaster raster, double[][] zBuffer, Viewport camera) {
		Vec3 projectedStart = camera.applyTo(start);
		Vec3 projectedEnd = camera.applyTo(end);
		
		
		if (projectedStart.z < 0 || projectedEnd.z < 0) return;

		
		double x1 = camera.getX(projectedStart);
		double y1 = camera.getY(projectedStart);

		double x2 = camera.getX(projectedEnd);
		double y2 = camera.getY(projectedEnd);

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
