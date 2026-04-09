
import Math.ImplicitEquation;
import Math.Pair;
import Math.Vec3;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MeshLoader{
	public static final float EPSILON = PhysicalObject.EPSILON;
	private MeshLoader(){}

	public static Mesh loadImplicitEquation(ImplicitEquation f, AABB box, float d, Material material) {
		if (material.transparent){
			System.out.println("Transparency not supported with Implicit Equations");
		}
		List<Triangle> tris = new ArrayList<>();
		float iso = 0;
		System.out.println("Loading a implicit Equation");
		for (float x = box.minX; x < box.maxX; x += d) {
			for (float y = box.minY; y < box.maxY; y += d) {
		outer:	for (float z = box.minZ; z < box.maxZ; z += d) {
					// cube corners
					Vec3[] p = new Vec3[8];
					p[0] = new Vec3(x,   y,   z  );
					p[1] = new Vec3(x+d, y,   z  );
					p[2] = new Vec3(x,   y,   z+d);
					p[3] = new Vec3(x+d, y,   z+d);
					p[4] = new Vec3(x,   y+d, z  );
					p[5] = new Vec3(x+d, y+d, z  );
					p[6] = new Vec3(x,   y+d, z+d);
					p[7] = new Vec3(x+d, y+d, z+d);

					float[] v = new float[8];
					for (int i = 0; i < 8; i++){
						v[i] = f.apply(p[i].x, p[i].y, p[i].z);
						if (Float.isNaN(v[i])){
							continue outer;
						}
					}

					int cubeIndex = 0;
					if (v[0] < 0) cubeIndex |= 1;
					if (v[1] < 0) cubeIndex |= 2;
					if (v[2] < 0) cubeIndex |= 4;
					if (v[3] < 0) cubeIndex |= 8;
					if (v[4] < 0) cubeIndex |= 16;
					if (v[5] < 0) cubeIndex |= 32;
					if (v[6] < 0) cubeIndex |= 64;
					if (v[7] < 0) cubeIndex |= 128;

					int edges = EDGE_TABLE[cubeIndex];

					Vec3[] verticies = new Vec3[12];

					if ((edges & 1) != 0) verticies[0] = interp(p[0], p[1], v[0], v[1], iso);
					if ((edges & 2) != 0) verticies[1] = interp(p[1], p[3], v[1], v[3], iso);
					if ((edges & 4) != 0) verticies[2] = interp(p[3], p[2], v[3], v[2], iso);
					if ((edges & 8) != 0) verticies[3] = interp(p[2], p[0], v[2], v[0], iso);

					if ((edges & 16) != 0) verticies[4] = interp(p[4], p[5], v[4], v[5], iso);
					if ((edges & 32) != 0) verticies[5] = interp(p[5], p[7], v[5], v[7], iso);
					if ((edges & 64) != 0) verticies[6] = interp(p[7], p[6], v[7], v[6], iso);
					if ((edges & 128) != 0) verticies[7] = interp(p[6], p[4], v[6], v[4], iso);

					if ((edges & 256) != 0) verticies[8] = interp(p[0], p[4], v[0], v[4], iso);
					if ((edges & 512) != 0) verticies[9] = interp(p[1], p[5], v[1], v[5], iso);
					if ((edges & 1024) != 0) verticies[10] = interp(p[3], p[7], v[3], v[7], iso);
					if ((edges & 2048) != 0) verticies[11] = interp(p[2], p[6], v[2], v[6], iso);

					

					byte[] triangles = TRI_TABLE[cubeIndex];
					
					for (int i = 0; i < triangles.length; i += 3){
						tris.add(new Triangle(
							verticies[triangles[i]],
							verticies[triangles[i+1]],
							verticies[triangles[i+2]]
						));
					}
				}
			}
		}
		System.out.println("Loaded a implicit equation with "+tris.size()+" triangles");
		return new Mesh(tris, material, new Transform());
	}
	private static Vec3 interp(Vec3 p1, Vec3 p2, float v1, float v2, float iso) {
		if (Math.abs(v1) < EPSILON) return p1;
		if (Math.abs(v2) < EPSILON) return p2;
		if (Math.abs(v1 - v2) < EPSILON) return p1;

		float t = v1 / (v1 - v2);
		return p1.add(p2.sub(p1).mul(t));
	}
	private static final int[] EDGE_TABLE = {0x0, 0x109, 0x203, 0x30a, 0x80c, 0x905, 0xa0f, 0xb06, 0x406, 0x50f, 0x605, 0x70c, 0xc0a, 0xd03, 0xe09, 0xf00, 0x190, 0x99, 0x393, 0x29a, 0x99c, 0x895, 0xb9f, 0xa96, 0x596, 0x49f, 0x795, 0x69c, 0xd9a, 0xc93, 0xf99, 0xe90, 0x230, 0x339, 0x33, 0x13a, 0xa3c, 0xb35, 0x83f, 0x936, 0x636, 0x73f, 0x435, 0x53c, 0xe3a, 0xf33, 0xc39, 0xd30, 0x3a0, 0x2a9, 0x1a3, 0xaa, 0xbac, 0xaa5, 0x9af, 0x8a6, 0x7a6, 0x6af, 0x5a5, 0x4ac, 0xfaa, 0xea3, 0xda9, 0xca0, 0x8c0, 0x9c9, 0xac3, 0xbca, 0xcc, 0x1c5, 0x2cf, 0x3c6, 0xcc6, 0xdcf, 0xec5, 0xfcc, 0x4ca, 0x5c3, 0x6c9, 0x7c0, 0x950, 0x859, 0xb53, 0xa5a, 0x15c, 0x55, 0x35f, 0x256, 0xd56, 0xc5f, 0xf55, 0xe5c, 0x55a, 0x453, 0x759, 0x650, 0xaf0, 0xbf9, 0x8f3, 0x9fa, 0x2fc, 0x3f5, 0xff, 0x1f6, 0xef6, 0xfff, 0xcf5, 0xdfc, 0x6fa, 0x7f3, 0x4f9, 0x5f0, 0xb60, 0xa69, 0x963, 0x86a, 0x36c, 0x265, 0x16f, 0x66, 0xf66, 0xe6f, 0xd65, 0xc6c, 0x76a, 0x663, 0x569, 0x460, 0x460, 0x569, 0x663, 0x76a, 0xc6c, 0xd65, 0xe6f, 0xf66, 0x66, 0x16f, 0x265, 0x36c, 0x86a, 0x963, 0xa69, 0xb60, 0x5f0, 0x4f9, 0x7f3, 0x6fa, 0xdfc, 0xcf5, 0xfff, 0xef6, 0x1f6, 0xff, 0x3f5, 0x2fc, 0x9fa, 0x8f3, 0xbf9, 0xaf0, 0x650, 0x759, 0x453, 0x55a, 0xe5c, 0xf55, 0xc5f, 0xd56, 0x256, 0x35f, 0x55, 0x15c, 0xa5a, 0xb53, 0x859, 0x950, 0x7c0, 0x6c9, 0x5c3, 0x4ca, 0xfcc, 0xec5, 0xdcf, 0xcc6, 0x3c6, 0x2cf, 0x1c5, 0xcc, 0xbca, 0xac3, 0x9c9, 0x8c0, 0xca0, 0xda9, 0xea3, 0xfaa, 0x4ac, 0x5a5, 0x6af, 0x7a6, 0x8a6, 0x9af, 0xaa5, 0xbac, 0xaa, 0x1a3, 0x2a9, 0x3a0, 0xd30, 0xc39, 0xf33, 0xe3a, 0x53c, 0x435, 0x73f, 0x636, 0x936, 0x83f, 0xb35, 0xa3c, 0x13a, 0x33, 0x339, 0x230, 0xe90, 0xf99, 0xc93, 0xd9a, 0x69c, 0x795, 0x49f, 0x596, 0xa96, 0xb9f, 0x895, 0x99c, 0x29a, 0x393, 0x99, 0x190, 0xf00, 0xe09, 0xd03, 0xc0a, 0x70c, 0x605, 0x50f, 0x406, 0xb06, 0xa0f, 0x905, 0x80c, 0x30a, 0x203, 0x109, 0x0,};
	private static final byte[][] TRI_TABLE = {new byte[] {}, new byte[] {0, 3, 8}, new byte[] {0, 9, 1}, new byte[] {3, 8, 1, 1, 8, 9}, new byte[] {2, 11, 3}, new byte[] {8, 0, 11, 11, 0, 2}, new byte[] {3, 2, 11, 1, 0, 9}, new byte[] {11, 1, 2, 11, 9, 1, 11, 8, 9}, new byte[] {1, 10, 2}, new byte[] {0, 3, 8, 2, 1, 10}, new byte[] {10, 2, 9, 9, 2, 0}, new byte[] {8, 2, 3, 8, 10, 2, 8, 9, 10}, new byte[] {11, 3, 10, 10, 3, 1}, new byte[] {10, 0, 1, 10, 8, 0, 10, 11, 8}, new byte[] {9, 3, 0, 9, 11, 3, 9, 10, 11}, new byte[] {8, 9, 11, 11, 9, 10}, new byte[] {4, 8, 7}, new byte[] {7, 4, 3, 3, 4, 0}, new byte[] {4, 8, 7, 0, 9, 1}, new byte[] {1, 4, 9, 1, 7, 4, 1, 3, 7}, new byte[] {8, 7, 4, 11, 3, 2}, new byte[] {4, 11, 7, 4, 2, 11, 4, 0, 2}, new byte[] {0, 9, 1, 8, 7, 4, 11, 3, 2}, new byte[] {7, 4, 11, 11, 4, 2, 2, 4, 9, 2, 9, 1}, new byte[] {4, 8, 7, 2, 1, 10}, new byte[] {7, 4, 3, 3, 4, 0, 10, 2, 1}, new byte[] {10, 2, 9, 9, 2, 0, 7, 4, 8}, new byte[] {10, 2, 3, 10, 3, 4, 3, 7, 4, 9, 10, 4}, new byte[] {1, 10, 3, 3, 10, 11, 4, 8, 7}, new byte[] {10, 11, 1, 11, 7, 4, 1, 11, 4, 1, 4, 0}, new byte[] {7, 4, 8, 9, 3, 0, 9, 11, 3, 9, 10, 11}, new byte[] {7, 4, 11, 4, 9, 11, 9, 10, 11}, new byte[] {9, 4, 5}, new byte[] {9, 4, 5, 8, 0, 3}, new byte[] {4, 5, 0, 0, 5, 1}, new byte[] {5, 8, 4, 5, 3, 8, 5, 1, 3}, new byte[] {9, 4, 5, 11, 3, 2}, new byte[] {2, 11, 0, 0, 11, 8, 5, 9, 4}, new byte[] {4, 5, 0, 0, 5, 1, 11, 3, 2}, new byte[] {5, 1, 4, 1, 2, 11, 4, 1, 11, 4, 11, 8}, new byte[] {1, 10, 2, 5, 9, 4}, new byte[] {9, 4, 5, 0, 3, 8, 2, 1, 10}, new byte[] {2, 5, 10, 2, 4, 5, 2, 0, 4}, new byte[] {10, 2, 5, 5, 2, 4, 4, 2, 3, 4, 3, 8}, new byte[] {11, 3, 10, 10, 3, 1, 4, 5, 9}, new byte[] {4, 5, 9, 10, 0, 1, 10, 8, 0, 10, 11, 8}, new byte[] {11, 3, 0, 11, 0, 5, 0, 4, 5, 10, 11, 5}, new byte[] {4, 5, 8, 5, 10, 8, 10, 11, 8}, new byte[] {8, 7, 9, 9, 7, 5}, new byte[] {3, 9, 0, 3, 5, 9, 3, 7, 5}, new byte[] {7, 0, 8, 7, 1, 0, 7, 5, 1}, new byte[] {7, 5, 3, 3, 5, 1}, new byte[] {5, 9, 7, 7, 9, 8, 2, 11, 3}, new byte[] {2, 11, 7, 2, 7, 9, 7, 5, 9, 0, 2, 9}, new byte[] {2, 11, 3, 7, 0, 8, 7, 1, 0, 7, 5, 1}, new byte[] {2, 11, 1, 11, 7, 1, 7, 5, 1}, new byte[] {8, 7, 9, 9, 7, 5, 2, 1, 10}, new byte[] {10, 2, 1, 3, 9, 0, 3, 5, 9, 3, 7, 5}, new byte[] {7, 5, 8, 5, 10, 2, 8, 5, 2, 8, 2, 0}, new byte[] {10, 2, 5, 2, 3, 5, 3, 7, 5}, new byte[] {8, 7, 5, 8, 5, 9, 11, 3, 10, 3, 1, 10}, new byte[] {5, 11, 7, 10, 11, 5, 1, 9, 0}, new byte[] {11, 5, 10, 7, 5, 11, 8, 3, 0}, new byte[] {5, 11, 7, 10, 11, 5}, new byte[] {6, 7, 11}, new byte[] {7, 11, 6, 3, 8, 0}, new byte[] {6, 7, 11, 0, 9, 1}, new byte[] {9, 1, 8, 8, 1, 3, 6, 7, 11}, new byte[] {3, 2, 7, 7, 2, 6}, new byte[] {0, 7, 8, 0, 6, 7, 0, 2, 6}, new byte[] {6, 7, 2, 2, 7, 3, 9, 1, 0}, new byte[] {6, 7, 8, 6, 8, 1, 8, 9, 1, 2, 6, 1}, new byte[] {11, 6, 7, 10, 2, 1}, new byte[] {3, 8, 0, 11, 6, 7, 10, 2, 1}, new byte[] {0, 9, 2, 2, 9, 10, 7, 11, 6}, new byte[] {6, 7, 11, 8, 2, 3, 8, 10, 2, 8, 9, 10}, new byte[] {7, 10, 6, 7, 1, 10, 7, 3, 1}, new byte[] {8, 0, 7, 7, 0, 6, 6, 0, 1, 6, 1, 10}, new byte[] {7, 3, 6, 3, 0, 9, 6, 3, 9, 6, 9, 10}, new byte[] {6, 7, 10, 7, 8, 10, 8, 9, 10}, new byte[] {11, 6, 8, 8, 6, 4}, new byte[] {6, 3, 11, 6, 0, 3, 6, 4, 0}, new byte[] {11, 6, 8, 8, 6, 4, 1, 0, 9}, new byte[] {1, 3, 9, 3, 11, 6, 9, 3, 6, 9, 6, 4}, new byte[] {2, 8, 3, 2, 4, 8, 2, 6, 4}, new byte[] {4, 0, 6, 6, 0, 2}, new byte[] {9, 1, 0, 2, 8, 3, 2, 4, 8, 2, 6, 4}, new byte[] {9, 1, 4, 1, 2, 4, 2, 6, 4}, new byte[] {4, 8, 6, 6, 8, 11, 1, 10, 2}, new byte[] {1, 10, 2, 6, 3, 11, 6, 0, 3, 6, 4, 0}, new byte[] {11, 6, 4, 11, 4, 8, 10, 2, 9, 2, 0, 9}, new byte[] {10, 4, 9, 6, 4, 10, 11, 2, 3}, new byte[] {4, 8, 3, 4, 3, 10, 3, 1, 10, 6, 4, 10}, new byte[] {1, 10, 0, 10, 6, 0, 6, 4, 0}, new byte[] {4, 10, 6, 9, 10, 4, 0, 8, 3}, new byte[] {4, 10, 6, 9, 10, 4}, new byte[] {6, 7, 11, 4, 5, 9}, new byte[] {4, 5, 9, 7, 11, 6, 3, 8, 0}, new byte[] {1, 0, 5, 5, 0, 4, 11, 6, 7}, new byte[] {11, 6, 7, 5, 8, 4, 5, 3, 8, 5, 1, 3}, new byte[] {3, 2, 7, 7, 2, 6, 9, 4, 5}, new byte[] {5, 9, 4, 0, 7, 8, 0, 6, 7, 0, 2, 6}, new byte[] {3, 2, 6, 3, 6, 7, 1, 0, 5, 0, 4, 5}, new byte[] {6, 1, 2, 5, 1, 6, 4, 7, 8}, new byte[] {10, 2, 1, 6, 7, 11, 4, 5, 9}, new byte[] {0, 3, 8, 4, 5, 9, 11, 6, 7, 10, 2, 1}, new byte[] {7, 11, 6, 2, 5, 10, 2, 4, 5, 2, 0, 4}, new byte[] {8, 4, 7, 5, 10, 6, 3, 11, 2}, new byte[] {9, 4, 5, 7, 10, 6, 7, 1, 10, 7, 3, 1}, new byte[] {10, 6, 5, 7, 8, 4, 1, 9, 0}, new byte[] {4, 3, 0, 7, 3, 4, 6, 5, 10}, new byte[] {10, 6, 5, 8, 4, 7}, new byte[] {9, 6, 5, 9, 11, 6, 9, 8, 11}, new byte[] {11, 6, 3, 3, 6, 0, 0, 6, 5, 0, 5, 9}, new byte[] {11, 6, 5, 11, 5, 0, 5, 1, 0, 8, 11, 0}, new byte[] {11, 6, 3, 6, 5, 3, 5, 1, 3}, new byte[] {9, 8, 5, 8, 3, 2, 5, 8, 2, 5, 2, 6}, new byte[] {5, 9, 6, 9, 0, 6, 0, 2, 6}, new byte[] {1, 6, 5, 2, 6, 1, 3, 0, 8}, new byte[] {1, 6, 5, 2, 6, 1}, new byte[] {2, 1, 10, 9, 6, 5, 9, 11, 6, 9, 8, 11}, new byte[] {9, 0, 1, 3, 11, 2, 5, 10, 6}, new byte[] {11, 0, 8, 2, 0, 11, 10, 6, 5}, new byte[] {3, 11, 2, 5, 10, 6}, new byte[] {1, 8, 3, 9, 8, 1, 5, 10, 6}, new byte[] {6, 5, 10, 0, 1, 9}, new byte[] {8, 3, 0, 5, 10, 6}, new byte[] {6, 5, 10}, new byte[] {10, 5, 6}, new byte[] {0, 3, 8, 6, 10, 5}, new byte[] {10, 5, 6, 9, 1, 0}, new byte[] {3, 8, 1, 1, 8, 9, 6, 10, 5}, new byte[] {2, 11, 3, 6, 10, 5}, new byte[] {8, 0, 11, 11, 0, 2, 5, 6, 10}, new byte[] {1, 0, 9, 2, 11, 3, 6, 10, 5}, new byte[] {5, 6, 10, 11, 1, 2, 11, 9, 1, 11, 8, 9}, new byte[] {5, 6, 1, 1, 6, 2}, new byte[] {5, 6, 1, 1, 6, 2, 8, 0, 3}, new byte[] {6, 9, 5, 6, 0, 9, 6, 2, 0}, new byte[] {6, 2, 5, 2, 3, 8, 5, 2, 8, 5, 8, 9}, new byte[] {3, 6, 11, 3, 5, 6, 3, 1, 5}, new byte[] {8, 0, 1, 8, 1, 6, 1, 5, 6, 11, 8, 6}, new byte[] {11, 3, 6, 6, 3, 5, 5, 3, 0, 5, 0, 9}, new byte[] {5, 6, 9, 6, 11, 9, 11, 8, 9}, new byte[] {5, 6, 10, 7, 4, 8}, new byte[] {0, 3, 4, 4, 3, 7, 10, 5, 6}, new byte[] {5, 6, 10, 4, 8, 7, 0, 9, 1}, new byte[] {6, 10, 5, 1, 4, 9, 1, 7, 4, 1, 3, 7}, new byte[] {7, 4, 8, 6, 10, 5, 2, 11, 3}, new byte[] {10, 5, 6, 4, 11, 7, 4, 2, 11, 4, 0, 2}, new byte[] {4, 8, 7, 6, 10, 5, 3, 2, 11, 1, 0, 9}, new byte[] {1, 2, 10, 11, 7, 6, 9, 5, 4}, new byte[] {2, 1, 6, 6, 1, 5, 8, 7, 4}, new byte[] {0, 3, 7, 0, 7, 4, 2, 1, 6, 1, 5, 6}, new byte[] {8, 7, 4, 6, 9, 5, 6, 0, 9, 6, 2, 0}, new byte[] {7, 2, 3, 6, 2, 7, 5, 4, 9}, new byte[] {4, 8, 7, 3, 6, 11, 3, 5, 6, 3, 1, 5}, new byte[] {5, 0, 1, 4, 0, 5, 7, 6, 11}, new byte[] {9, 5, 4, 6, 11, 7, 0, 8, 3}, new byte[] {11, 7, 6, 9, 5, 4}, new byte[] {6, 10, 4, 4, 10, 9}, new byte[] {6, 10, 4, 4, 10, 9, 3, 8, 0}, new byte[] {0, 10, 1, 0, 6, 10, 0, 4, 6}, new byte[] {6, 10, 1, 6, 1, 8, 1, 3, 8, 4, 6, 8}, new byte[] {9, 4, 10, 10, 4, 6, 3, 2, 11}, new byte[] {2, 11, 8, 2, 8, 0, 6, 10, 4, 10, 9, 4}, new byte[] {11, 3, 2, 0, 10, 1, 0, 6, 10, 0, 4, 6}, new byte[] {6, 8, 4, 11, 8, 6, 2, 10, 1}, new byte[] {4, 1, 9, 4, 2, 1, 4, 6, 2}, new byte[] {3, 8, 0, 4, 1, 9, 4, 2, 1, 4, 6, 2}, new byte[] {6, 2, 4, 4, 2, 0}, new byte[] {3, 8, 2, 8, 4, 2, 4, 6, 2}, new byte[] {4, 6, 9, 6, 11, 3, 9, 6, 3, 9, 3, 1}, new byte[] {8, 6, 11, 4, 6, 8, 9, 0, 1}, new byte[] {11, 3, 6, 3, 0, 6, 0, 4, 6}, new byte[] {8, 6, 11, 4, 6, 8}, new byte[] {10, 7, 6, 10, 8, 7, 10, 9, 8}, new byte[] {3, 7, 0, 7, 6, 10, 0, 7, 10, 0, 10, 9}, new byte[] {6, 10, 7, 7, 10, 8, 8, 10, 1, 8, 1, 0}, new byte[] {6, 10, 7, 10, 1, 7, 1, 3, 7}, new byte[] {3, 2, 11, 10, 7, 6, 10, 8, 7, 10, 9, 8}, new byte[] {2, 9, 0, 10, 9, 2, 6, 11, 7}, new byte[] {0, 8, 3, 7, 6, 11, 1, 2, 10}, new byte[] {7, 6, 11, 1, 2, 10}, new byte[] {2, 1, 9, 2, 9, 7, 9, 8, 7, 6, 2, 7}, new byte[] {2, 7, 6, 3, 7, 2, 0, 1, 9}, new byte[] {8, 7, 0, 7, 6, 0, 6, 2, 0}, new byte[] {7, 2, 3, 6, 2, 7}, new byte[] {8, 1, 9, 3, 1, 8, 11, 7, 6}, new byte[] {11, 7, 6, 1, 9, 0}, new byte[] {6, 11, 7, 0, 8, 3}, new byte[] {11, 7, 6}, new byte[] {7, 11, 5, 5, 11, 10}, new byte[] {10, 5, 11, 11, 5, 7, 0, 3, 8}, new byte[] {7, 11, 5, 5, 11, 10, 0, 9, 1}, new byte[] {7, 11, 10, 7, 10, 5, 3, 8, 1, 8, 9, 1}, new byte[] {5, 2, 10, 5, 3, 2, 5, 7, 3}, new byte[] {5, 7, 10, 7, 8, 0, 10, 7, 0, 10, 0, 2}, new byte[] {0, 9, 1, 5, 2, 10, 5, 3, 2, 5, 7, 3}, new byte[] {9, 7, 8, 5, 7, 9, 10, 1, 2}, new byte[] {1, 11, 2, 1, 7, 11, 1, 5, 7}, new byte[] {8, 0, 3, 1, 11, 2, 1, 7, 11, 1, 5, 7}, new byte[] {7, 11, 2, 7, 2, 9, 2, 0, 9, 5, 7, 9}, new byte[] {7, 9, 5, 8, 9, 7, 3, 11, 2}, new byte[] {3, 1, 7, 7, 1, 5}, new byte[] {8, 0, 7, 0, 1, 7, 1, 5, 7}, new byte[] {0, 9, 3, 9, 5, 3, 5, 7, 3}, new byte[] {9, 7, 8, 5, 7, 9}, new byte[] {8, 5, 4, 8, 10, 5, 8, 11, 10}, new byte[] {0, 3, 11, 0, 11, 5, 11, 10, 5, 4, 0, 5}, new byte[] {1, 0, 9, 8, 5, 4, 8, 10, 5, 8, 11, 10}, new byte[] {10, 3, 11, 1, 3, 10, 9, 5, 4}, new byte[] {3, 2, 8, 8, 2, 4, 4, 2, 10, 4, 10, 5}, new byte[] {10, 5, 2, 5, 4, 2, 4, 0, 2}, new byte[] {5, 4, 9, 8, 3, 0, 10, 1, 2}, new byte[] {2, 10, 1, 4, 9, 5}, new byte[] {8, 11, 4, 11, 2, 1, 4, 11, 1, 4, 1, 5}, new byte[] {0, 5, 4, 1, 5, 0, 2, 3, 11}, new byte[] {0, 11, 2, 8, 11, 0, 4, 9, 5}, new byte[] {5, 4, 9, 2, 3, 11}, new byte[] {4, 8, 5, 8, 3, 5, 3, 1, 5}, new byte[] {0, 5, 4, 1, 5, 0}, new byte[] {5, 4, 9, 3, 0, 8}, new byte[] {5, 4, 9}, new byte[] {11, 4, 7, 11, 9, 4, 11, 10, 9}, new byte[] {0, 3, 8, 11, 4, 7, 11, 9, 4, 11, 10, 9}, new byte[] {11, 10, 7, 10, 1, 0, 7, 10, 0, 7, 0, 4}, new byte[] {3, 10, 1, 11, 10, 3, 7, 8, 4}, new byte[] {3, 2, 10, 3, 10, 4, 10, 9, 4, 7, 3, 4}, new byte[] {9, 2, 10, 0, 2, 9, 8, 4, 7}, new byte[] {3, 4, 7, 0, 4, 3, 1, 2, 10}, new byte[] {7, 8, 4, 10, 1, 2}, new byte[] {7, 11, 4, 4, 11, 9, 9, 11, 2, 9, 2, 1}, new byte[] {1, 9, 0, 4, 7, 8, 2, 3, 11}, new byte[] {7, 11, 4, 11, 2, 4, 2, 0, 4}, new byte[] {4, 7, 8, 2, 3, 11}, new byte[] {9, 4, 1, 4, 7, 1, 7, 3, 1}, new byte[] {7, 8, 4, 1, 9, 0}, new byte[] {3, 4, 7, 0, 4, 3}, new byte[] {7, 8, 4}, new byte[] {11, 10, 8, 8, 10, 9}, new byte[] {0, 3, 9, 3, 11, 9, 11, 10, 9}, new byte[] {1, 0, 10, 0, 8, 10, 8, 11, 10}, new byte[] {10, 3, 11, 1, 3, 10}, new byte[] {3, 2, 8, 2, 10, 8, 10, 9, 8}, new byte[] {9, 2, 10, 0, 2, 9}, new byte[] {8, 3, 0, 10, 1, 2}, new byte[] {2, 10, 1}, new byte[] {2, 1, 11, 1, 9, 11, 9, 8, 11}, new byte[] {11, 2, 3, 9, 0, 1}, new byte[] {11, 0, 8, 2, 0, 11}, new byte[] {3, 11, 2}, new byte[] {1, 8, 3, 9, 8, 1}, new byte[] {1, 9, 0}, new byte[] {8, 3, 0}, new byte[] {}};
	
	public static Mesh loadObj(String filename, Transform transform, float size, Material material){
		return loadObj(filename, transform, size, material, true);
	}
	public static Mesh loadObj(String filename, Transform transform, float size, Material material, boolean maintainAspectratio){
		compact(filename);
		System.out.println("Loading "+filename+"... ");
		
		Pair<List<Vec3>, List<IndexedTriangle>> pair = readFile(filename);
		List<Vec3> points = pair.t0;
		List<IndexedTriangle> indexedTriangles = pair.t1;
		AABB bounds = new AABB(points);


		
		System.out.println("  Loaded "+indexedTriangles.size()+" triangles");
		System.out.println("  Loaded "+points.size()+" points");
		System.out.println(filename+" successfully loaded");
		
		float xrange = bounds.maxX-bounds.minX;
		float yrange = bounds.maxY-bounds.minY;
		float zrange = bounds.maxZ-bounds.minZ;
		float maxRange = Math.max(Math.max(xrange,yrange),zrange);


		float xscale, yscale, zscale;
		if (maintainAspectratio){
			float scale = (size == 0 ? 1 : size/maxRange);
			xscale = yscale = zscale = scale;
		} else {
			xscale = (size == 0 ? 1 : size/xrange);
			yscale = (size == 0 ? 1 : size/yrange);
			zscale = (size == 0 ? 1 : size/zrange);
		}


		xrange *= xscale;
		yrange *= yscale;
		zrange *= zscale;
		for (int i = 0; i < points.size(); i++){
			Vec3 p = points.get(i);
			points.set(i,
				new Vec3(
					(p.x - bounds.minX)*xscale-xrange/2,
					(p.y - bounds.minY)*yscale-yrange/2,
					(p.z - bounds.minZ)*zscale-zrange/2
				)
			);
		}
		
		
		List<Triangle> triangles = new ArrayList<>(indexedTriangles.size());
		for (int i = 0; i < indexedTriangles.size(); i++){
			IndexedTriangle itri = indexedTriangles.get(i);
			triangles.add(new Triangle(itri.i1, itri.i2, itri.i3, points));
		}

		return new Mesh(triangles, material, transform);
	}
	private static Pair<List<Vec3>, List<IndexedTriangle>> readFile(String filename){
		List<Vec3> points = new ArrayList<>();
		List<IndexedTriangle> indexedTriangles = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			List<Integer> pointBuffer = new ArrayList<>();
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				char type = line.charAt(0);
				if (type == 'v'){
					if (line.charAt(1) == 'n' || line.charAt(1) == 't') continue;
					
					String[] rawValues = line.split(" ");
					
					points.add(new Vec3(Float.parseFloat(rawValues[1]), Float.parseFloat(rawValues[2]), Float.parseFloat(rawValues[3])));
				} else if (type == 'f'){
					String[] thesePoints = line.split(" ");
					pointBuffer.clear();
					
					for (int i = 1; i < thesePoints.length; i++){
						String rawPoint = thesePoints[i];
						int index = Integer.parseInt(rawPoint.split("/")[0]);
						pointBuffer.add(index-1);
					}
					while (pointBuffer.size()>2){
						indexedTriangles.add(new IndexedTriangle((int)pointBuffer.get(0), (int)pointBuffer.get(1), (int)pointBuffer.get(2)));
						pointBuffer.remove(1);
					}
				}
			}
		} catch (IOException e){
			System.out.println("Failed to load "+filename);
		}
		return new Pair<>(points, indexedTriangles);
	}
	public static boolean compact(String filename){
		System.out.println("Compacting "+filename);
		List<Vec3> points = new ArrayList<>();
		List<int[]> faces = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			List<String> lines = reader.lines().toList();
			if (lines.get(0).equals("# compacted")){
				System.out.println("  "+filename+" is already compact");
				return true;
			}
			for (String line : lines){
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;

				char type = line.charAt(0);
				if (type == 'v'){
					if (line.charAt(1) == 'n' || line.charAt(1) == 't'){
						// skip the texture and normal data
						continue;
					}
					String[] rawValues = line.split(" ");

					points.add(new Vec3(Float.parseFloat(rawValues[1]), Float.parseFloat(rawValues[2]), Float.parseFloat(rawValues[3])));
				} else if (type == 'f'){
					String[] facePointsRaw = line.split(" ");
					int[] facePoints = new int[facePointsRaw.length-1];
					for (int i = 0; i < facePoints.length; i++){
						facePoints[i] = Integer.parseInt(facePointsRaw[i+1].split("/")[0]);
					}
					faces.add(facePoints);
				}
			}
		} catch (IOException e) {
			System.out.println("  Failed to read "+filename);
			return false;
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))){
			writer.write("# compacted\n");
			for (Vec3 point : points){
				writer.write(String.format("v %.6f %.6f %.6f%n", point.x, point.y, point.z));
			}
			StringBuilder str = new StringBuilder();
			for (int[] face : faces){
				str.append('f').append(' ');
				for (int point : face){
					str.append(point);
					str.append(' ');
				}
				str.deleteCharAt(str.length()-1);
				str.append('\n');
			}
			writer.write(str.toString());
		} catch (IOException e){
			System.out.println("  Failed to save "+filename);
		}
		return false;
	}
	private static class IndexedTriangle{
		public final int i1;
		public final int i2;
		public final int i3;
		public IndexedTriangle(int i1, int i2, int i3){
			this.i1 = i1;
			this.i2 = i2;
			this.i3 = i3;
		}
	}
}