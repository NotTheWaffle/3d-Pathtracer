

import Math.FloatMath;
import Math.Vec3;
import java.awt.image.WritableRaster;
import java.util.Random;

public final class Ray {
	public final static float EPSILON = PhysicalObject.EPSILON;
	private Ray(){}
	public static float[] trace(Vec3 rayOrigin, Vec3 rayDirection, Environment env, int maxDepth, Random random){
		float[] rayColor = {1.0f, 1.0f, 1.0f};
		float[] incomingLight = {0.0f, 0.0f, 0.0f};
		
		for (int i = 0; i < maxDepth; i++){
			// find nearest intersection
			Intersection intersection = null;
			for (PhysicalObject p : env.physicalObjects){
				Intersection localIntersection = p.getTransformedintersection(rayOrigin, rayDirection);
				if (localIntersection == null) continue;
				if (intersection == null || rayOrigin.dist(intersection.pos) > rayOrigin.dist(localIntersection.pos)){
					intersection = localIntersection;
				}
			}

			// background light
			if (intersection == null) {
				intersection = env.getBackgroundEnvironment(rayDirection);
				if (intersection == null) break;
			}

			float distance = rayOrigin.dist(intersection.pos);
			
			
			Material material = intersection.material;
			Vec3 normal = intersection.normal;


			// find next ray
			Vec3 nextDirection;
			boolean applyColor = false;
			if (material.transparent){
				// handle transparent object
				Vec3 I = rayDirection.normalize();
				Vec3 N = normal.normalize();

				float iorA = 1.0f;
				float iorB = material.refractiveIndex;

				if (intersection.backface) {
					// ok so the distance from the last intersection to here isn't **necessarily** the distance in the thing, but like close enough
					float absorption = FloatMath.exp(-distance * material.absorption);
					rayColor[0] *= material.reflectionColor[0] * absorption;
					rayColor[1] *= material.reflectionColor[1] * absorption;
					rayColor[2] *= material.reflectionColor[2] * absorption;
					N = N.mul(-1);
					float temp = iorA;
					iorA = iorB;
					iorB = temp;
				}

				float cosI = -I.dot(N);
				float eta = iorA/iorB;

				float sinT2 = eta * eta * (1.0f - cosI * cosI);
				
				if (sinT2 >= 1.0){
					nextDirection = getSpecularReflectionVector(I, N);
				} else {
					float cosT = FloatMath.sqrt(1.0f - sinT2);
			
					float r0 = (iorA - iorB) / (iorA + iorB);
					r0 *= r0;

					float c = (iorA <= iorB) ? cosI : cosT;
					float reflectance = r0 + (1.0f - r0) * FloatMath.pow(1 - c, 5.0f);

					Vec3 diffuseDirection = getDiffuseReflectionVector(N, random);

					if (FloatMath.random() < reflectance){
						nextDirection = lerp(diffuseDirection, getSpecularReflectionVector(I, N), material.specularity).normalize();
					} else {
						nextDirection = lerp(diffuseDirection, getRefractionVector(I, N, eta, cosI, cosT), material.specularity).normalize();
					}
				}
			} else {
				if (intersection.backface){
					normal = normal.mul(-1);
				}
				Vec3 diffuseDirection = getCosineWeightedDiffuseReflectionVector(normal, random);
				
				if (FloatMath.random() < material.specularityChance){
					Vec3 specularDirection = getSpecularReflectionVector(rayDirection, normal);
					nextDirection = lerp(diffuseDirection, specularDirection, material.specularity).normalize();
					applyColor = false;
				} else {
					nextDirection = diffuseDirection;
					applyColor = true;
				}
			}
			
			rayDirection = nextDirection;
			rayOrigin = intersection.pos.add(rayDirection.mul(EPSILON));

			
			//calculate colors
			// emissionStrengh * emissionColor = emitted light, multiply with ray color to get the intersection of the colors
			if (applyColor){
				incomingLight[0] += (material.emissionStrength * material.emissionColor[0]) * rayColor[0];
				incomingLight[1] += (material.emissionStrength * material.emissionColor[1]) * rayColor[1];
				incomingLight[2] += (material.emissionStrength * material.emissionColor[2]) * rayColor[2];
				
				rayColor[0] *= material.reflectionColor[0];
				rayColor[1] *= material.reflectionColor[1];
				rayColor[2] *= material.reflectionColor[2];
			}

			if (rayColor[0] < .01 && rayColor[1] < .01 && rayColor[2] < .01){
				break;
			}
		}

		return incomingLight;
	}
	
	private static Vec3 getSpecularReflectionVector(Vec3 rayDirection, Vec3 normal){
		return rayDirection.sub(normal.mul(2 * rayDirection.dot(normal))).normalize();
	}

	private static Vec3 getDiffuseReflectionVector(Vec3 normal, Random random){
		Vec3 vec = Vec3.randomUnit(random).normalize();
		if (vec.dot(normal) < 0) vec = vec.mul(-1);
		return vec;
	}

	private static Vec3 getCosineWeightedDiffuseReflectionVector(Vec3 normal, Random random){
		Vec3 vec = normal.add(Vec3.randomUnit(random)).normalize();
		if (vec.dot(normal) < 0) vec = vec.mul(-1);
		return vec;

	}

	private static Vec3 getRefractionVector(Vec3 rayDirection, Vec3 normal, float eta, float cosI, float cosT){
		return rayDirection.mul(eta).add(normal.mul(eta * cosI - cosT)).normalize();
	}

	public static Vec3 lerp(final Vec3 start, final Vec3 end, final float a){
		final float ia = 1-a;
		return new Vec3(
			start.x * ia + end.x * a,
			start.y * ia + end.y * a,
			start.z * ia + end.z * a
		);
	}


	public static void render(Vec3 start, Vec3 end, WritableRaster raster, float[][] zBuffer, Viewport camera, int[] color) {
		Vec3 projectedStart = camera.applyTo(start);
		Vec3 projectedEnd = camera.applyTo(end);
		
		// don't attempt to render points behind the camera
		if (projectedStart.z < 0 || projectedEnd.z < 0) return;

		
		float x1 = camera.getX(projectedStart);
		float y1 = camera.getY(projectedStart);

		float x2 = camera.getX(projectedEnd);
		float y2 = camera.getY(projectedEnd);

		if (x1 > x2){
			float temp = x1;
			x1 = x2;
			x2 = temp;
			temp = y1;
			y1 = y2;
			y2 = temp;
		}

		float c = FloatMath.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
		float dx = (x2-x1)/c;
		float dy = (y2-y1)/c;

		int width = raster.getWidth();
		int height = raster.getHeight();
		
		for (float x = x1, y = y1; x < x2; x+=dx, y+=dy){
			if (x < 0 || (int)x >= width || y < 0 || (int)y >= height){
				break;
			}
			zBuffer[(int)x][(int)y] = 0f;
			raster.setPixel((int)x, (int)y, color);
		}
		for (float x = x2, y = y2; x > x1; x-=dx, y-=dy){
			if (x < 0 || (int)x >= width || y < 0 || y >= height){
				break;
			}
			zBuffer[(int)x][(int)y] = 0f;
			raster.setPixel((int)x, (int)y, color);
		}
	}
}
