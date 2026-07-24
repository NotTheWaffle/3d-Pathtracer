import Math.Vec3;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses the wavefront .obj file format. Able to reconstruct vertex normals. Currently not supporting large amounts of the standard.
 */
public class ObjParser {
	private ObjParser(){}
	public static ObjStruct parseObj(String filename){
		List<Vec3> vertices = new ArrayList<>();
		List<Vec3> normals = new ArrayList<>();
		List<Vec3> textures = new ArrayList<>();
		List<ObjFace> faces = new ArrayList<>();

		List<Integer> vertexIndexBuffer = new ArrayList<>();
		List<Integer> normalIndexBuffer = new ArrayList<>();
		List<Integer> textureIndexBuffer = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
			for (String line = reader.readLine(); line != null; line = reader.readLine()){
				if (line.length() == 0) continue;
				if (line.charAt(0) == '#') continue;
				String[] lineContents = line.split(" ");
				switch (lineContents[0]){
					case "v" -> {
						// vertex
						if (lineContents.length == 5){
							float scale = Float.parseFloat(lineContents[4]);
							vertices.add(new Vec3(Float.parseFloat(lineContents[1]), Float.parseFloat(lineContents[2]), Float.parseFloat(lineContents[3])).mul(1/scale));
						} else {
							vertices.add(new Vec3(Float.parseFloat(lineContents[1]), Float.parseFloat(lineContents[2]), Float.parseFloat(lineContents[3])));
						}
					}
					case "vt" -> {
						// vertex texture. currently not supported
						if (lineContents.length == 4){
							textures.add(new Vec3(Float.parseFloat(lineContents[1]), Float.parseFloat(lineContents[2]), Float.parseFloat(lineContents[3])));
						} else if (lineContents.length == 3){
							textures.add(new Vec3(Float.parseFloat(lineContents[1]), Float.parseFloat(lineContents[2]), 0.f));
						} else {
							textures.add(new Vec3(Float.parseFloat(lineContents[1]), 0.f, 0.f));
						}
					}
					case "vn" -> {
						// vertex normal
						normals.add(new Vec3(Float.parseFloat(lineContents[1]), Float.parseFloat(lineContents[2]), Float.parseFloat(lineContents[3])));
					}
					case "vp" -> {
						System.out.println("Parameter Space Verticies not supported");
					}
					case "f" -> {
						// face
						vertexIndexBuffer.clear();
						normalIndexBuffer.clear();
						textureIndexBuffer.clear();

						for (int i = 1; i < lineContents.length; i++){
							String[] data = lineContents[i].split("/");

							vertexIndexBuffer.add(Integer.parseInt(data[0])-1);

							if (data.length > 1 && data[1].length() > 0){
								normalIndexBuffer.add(Integer.parseInt(data[1])-1);
							}
							if (data.length > 2 && data[2].length() > 0){
								textureIndexBuffer.add(Integer.parseInt(data[2])-1);
							}
						}

						if (!normalIndexBuffer.isEmpty() && vertexIndexBuffer.size() != normalIndexBuffer.size()){
							System.out.println("mismatched faces");
						}
						if (!textureIndexBuffer.isEmpty() && vertexIndexBuffer.size() != textureIndexBuffer.size()){
							System.out.println("mismatched faces");
						}

						while (vertexIndexBuffer.size() > 2){
							faces.add(new ObjFace(vertexIndexBuffer, normalIndexBuffer, textureIndexBuffer));
							vertexIndexBuffer.remove(1);
							if (!normalIndexBuffer.isEmpty()) normalIndexBuffer.remove(1);
							if (!textureIndexBuffer.isEmpty()) textureIndexBuffer.remove(1);
						}
					}
					case "l" -> {
						System.out.println("Lines not supported");
					}

					case "mtllib" -> {
						System.out.println("Materials are not supported");
					}
					case "usemtl" -> {
						System.out.println("Materials are not supported");
					}
					case "o" -> {
						System.out.println("Objects are not supported");
					}
					case "g" -> {
						System.out.println("Groups are not supported");
					}
					case "s" -> {
						System.out.println("Shading is not supported");
					}
					default -> {}
				}
			}
		} catch (IOException exception){
			System.out.println("  Failed to load" + filename);
		}

		System.out.println("Parsed a .obj with "+faces.size()+" triangles");
		System.out.println("  and "+vertices.size()+" points");

		// this nonsense is used to join vertices and regenerate vertex normals
		return new ObjStruct(new ObjStruct(faces, vertices, normals, textures).getTriangles());
	}




	/**
	 * A clean representation of a .obj. Contains vertices, faces, and normals.
	 */
	public static class ObjStruct{
		public final List<Vec3> vertices;
		public final List<Vec3> normals;
		public final List<Vec3> textures;
		private final List<ObjFace> faces;
		// Constructs a ObjStruct given triangles. Regenerates vertex normals from averaging triangles and combines verticies via an octree.
		public ObjStruct(List<Triangle> triangles){
			this.vertices = new ArrayList<>();
			this.normals = new ArrayList<>();
			this.faces = new ArrayList<>(triangles.size());
			this.textures = null;
			AABB bounds = new AABB().grow(triangles);

			Octree pointHolder = new Octree(bounds);
			for (Triangle triangle : triangles){
				int i1 = findOrAdd(triangle.p1, pointHolder);
				int i2 = findOrAdd(triangle.p2, pointHolder);
				int i3 = findOrAdd(triangle.p3, pointHolder);
				faces.add(new ObjFace(i1, i2, i3));
			}

			int count = vertices.size();
			// initialize vertex normals as 0
			for (int i = 0; i < count; i++){
				normals.add(Vec3.ZERO_VEC);
			}
			// accumulate the normals of each triangle they are in
			for (ObjFace face : faces){
				Vec3 normal = face.toTriangle(vertices).normal();
				normals.set(face.v1, normals.get(face.v1).add(normal));
				normals.set(face.v2, normals.get(face.v2).add(normal));
				normals.set(face.v3, normals.get(face.v3).add(normal));
			}
			// renormalize the normals
			for (int i = 0; i < count; i++){
				normals.set(i, normals.get(i).normalize());
			}

			// add them to the faces
			for (int i = 0; i < faces.size(); i++){
				ObjFace tri = faces.get(i);
				// if we make the vertex normals they are numbered identically with their verticies
				faces.set(i, tri.addVertexNormals(tri.v1, tri.v2, tri.v3));
			}
		}
		private int findOrAdd(Vec3 vec, Octree pointHolder){
			int i = pointHolder.get(vec);
			if (i == -1){
				i = vertices.size();
				pointHolder.put(vec, i);
				vertices.add(vec);
			}
			return i;
		}
		/**
		 * i feel ashamed for making this class
		 */
		static class Vec3AndInt{
			public final Vec3 vec3;
			public final int integer;
			public Vec3AndInt(Vec3 vec3, int integer){
				this.vec3 = vec3;
				this.integer = integer;
			}
			@Override
			public boolean equals(Object o){
				if (o == this) return true;
				if (o instanceof Vec3AndInt v3ai){
					return v3ai.vec3.equals(vec3) && v3ai.integer == integer;
				} else {
					return false;
				}
			}
			@Override
			public int hashCode(){
				return vec3.hashCode() ^ integer;
			}
			@Override
			public String toString(){
				return vec3.toString()+": "+integer;
			}
		}
		/**
		 * A fancy Mapping between vec3 and int that utilizes an octree for speedy lookups
		 */
		static class Octree{
			static final int MAX_POINTS = 30;
			final AABB bounds;
			Octree[] children;
			List<Vec3AndInt> points;
			public Octree(AABB bounds){
				this.bounds = bounds;
				this.points = new ArrayList<>();
				children = new Octree[8];
			}

			public int get(Vec3 point){
				if (!bounds.isContained(point)) return -1;
				if (this.points == null){
					for (int i = 0; i < 8; i++){
						int a = children[i].get(point);
						if (a >= 0) return a;
					}
					return -1;
				}
				for (Vec3AndInt p : points){
					if (p.vec3.dist(point) < PhysicalObject.EPSILON*10){
						return p.integer;
					}
				}
				return -1;
			}
			public boolean put(Vec3 point, int integer){
				if (!bounds.isContained(point)) {
					return false;
				}
				if (this.points == null){
					for (int i = 0; i < 8; i++){
						if (children[i].put(point, integer)) return true;
					}
					return false;
				}
				points.add(new Vec3AndInt(point, integer));
				if (points.size() > MAX_POINTS){
					float midX = (bounds.maxX+bounds.minX)/2;
					float midY = (bounds.maxY+bounds.minY)/2;
					float midZ = (bounds.maxZ+bounds.minZ)/2;

					children[0] = new Octree(new AABB(bounds.minX, bounds.minY, bounds.minZ, midX, midY, midZ));
					children[1] = new Octree(new AABB(bounds.minX, bounds.minY, midZ, midX, midY, bounds.maxZ));
					children[2] = new Octree(new AABB(bounds.minX, midY, bounds.minZ, midX, bounds.maxY, midZ));
					children[3] = new Octree(new AABB(bounds.minX, midY, midZ, midX, bounds.maxY, bounds.maxZ));

					children[4] = new Octree(new AABB(midX, bounds.minY, bounds.minZ, bounds.maxX, midY, midZ));
					children[5] = new Octree(new AABB(midX, bounds.minY, midZ, bounds.maxX, midY, bounds.maxZ));
					children[6] = new Octree(new AABB(midX, midY, bounds.minZ, bounds.maxX, bounds.maxY, midZ));
					children[7] = new Octree(new AABB(midX, midY, midZ, bounds.maxX, bounds.maxY, bounds.maxZ));

				outer:
					for (Vec3AndInt p : this.points){
						for (Octree child : children){
							if (child.put(p.vec3, p.integer)) continue outer;
						}
						System.out.println("octree construction issue");
					}
					this.points = null;
				}
				return true;
			}
		}


		// a normal constructor that takes in what a .obj gives
		private ObjStruct(List<ObjFace> faces, List<Vec3> vertices, List<Vec3> normals, List<Vec3> textures){
			this.faces = faces;
			this.vertices = vertices;
			this.normals = normals;
			this.textures = textures;
		}
		/**
		 * Generates a list of triangles from the vertex and face lists
		 * @return
		 */
		public List<Triangle> getTriangles(){
			List<Triangle> triangles = new ArrayList<>(faces.size());
			for (ObjFace face : faces){
				triangles.add(face.toTriangle(vertices));
			}
			return triangles;
		}
		/**
		 * Generates a list of interpolated and normal triangles from the vertex, face, and normals list.
		 * @return
		 */
		public List<Triangle> getInterpolatedTriangles(){
			if (normals == null) return null;
			List<Triangle> triangles = new ArrayList<>();
			for (ObjFace face : faces){
				triangles.add(face.toTriangle(vertices, normals));
			}
			return triangles;
		}
		@Override
		public boolean equals(Object o){
			if (o == this) return true;
			if (o instanceof ObjStruct struct){
				return
					struct.faces.equals(faces) && struct.vertices.equals(vertices)
					&& ((struct.normals == null && normals == null) || (struct.normals.equals(normals)))
					&& ((struct.textures == null && textures == null) || struct.textures.equals(textures));
			} else {
				return false;
			}
		}
		@Override
		public int hashCode(){
			return faces.hashCode() ^ vertices.hashCode() ^ (normals == null ? 0 : normals.hashCode()) ^ (textures == null ? 0 : textures.hashCode());
		}
		@Override
		public String toString(){
			return "Obj file with "+vertices+" vertices and "+faces+" triangles";
		}
	}

	/**
	 * A triangle that has indicies instead of vectors.
	 */
	private static class ObjFace{
		private final int v1, v2, v3, n1, n2, n3, t1, t2, t3;

		private ObjFace(List<Integer> vertexIndexBuffer, List<Integer> normalIndexBuffer, List<Integer> textureIndexBuffer){
			boolean hasNormals = !normalIndexBuffer.isEmpty();
			boolean hasTextures = !textureIndexBuffer.isEmpty();

			this(
				vertexIndexBuffer.get(0),
				vertexIndexBuffer.get(1),
				vertexIndexBuffer.get(2),

				hasNormals ? normalIndexBuffer.get(0) : -1,
				hasNormals ? normalIndexBuffer.get(1) : -1,
				hasNormals ? normalIndexBuffer.get(2) : -1,

				hasTextures ? textureIndexBuffer.get(0) : -1,
				hasTextures ? textureIndexBuffer.get(1) : -1,
				hasTextures ? textureIndexBuffer.get(2) : -1
			);
		}
		private ObjFace(int v1, int v2, int v3){
			this(v1, v2, v3, -1, -1, -1, -1, -1, -1);
		}
		private ObjFace(int v1, int v2, int v3, int n1, int n2, int n3, int t1, int t2, int t3){
			this.v1 = v1;
			this.v2 = v2;
			this.v3 = v3;

			this.n1 = n1;
			this.n2 = n2;
			this.n3 = n3;

			this.t1 = t1;
			this.t2 = t2;
			this.t3 = t3;
		}
		public ObjFace addVertexNormals(int n1, int n2, int n3){
			if (this.n1 >= 0 || this.n2 >= 0 || this.n3 >= 0){
				System.out.println("attempting to add normals to a triangle with normals");
			}
			return new ObjFace(v1, v2, v3, n1, n2, n3, t1, t2, t3);
		}
		public Triangle toTriangle(List<Vec3> points){
			return new Triangle(v1, v2, v3, points);
		}
		public Triangle toTriangle(List<Vec3> points, List<Vec3> normals){
			if (n1 == -1 || n2 == -1 || n3 == -1) return new Triangle(v1, v2, v3, points);
			return new InterpolatedTriangle(v1, v2, v3, points, n1, n2, n3, normals);
		}
		@Override
		public boolean equals(Object o){
			if (o == this) return true;
			if (o instanceof ObjFace f){
				return f.v1 == v1 && f.v2 == v2 && f.v3 == v3 &&
					f.n1 == n1 && f.n2 == n2 && f.n3 == n3 &&
					f.t1 == t1 && f.t2 == t2 && f.t3 == t3;
			} else {
				return false;
			}
		}
		@Override
		public int hashCode(){
			return v1 ^ v2 ^ v3 ^ n1 ^ n2 ^ n3 ^ t1 ^ t2 ^ t3;
		}
		@Override
		public String toString(){
			return "A face from a .obj: "+v1+"/"+t1+"/"+n1+" "+v2+"/"+t2+"/"+n2+" "+v3+"/"+t3+"/"+n3;
		}
	}
}