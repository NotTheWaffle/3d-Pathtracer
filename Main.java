
import Math.FloatMath;

public class Main {
	public static void main(String[] args){
		int size = 512;
		Viewport camera = new Viewport(.25f, 1f, 0f, size, size);
		camera.translate(0, 3, 0);
		camera.rotateX(FloatMath.PI/2);

		Environment env = new Environment(Environment.VOID);
		env.addHueSpheres(6, 1);
		env.addFloor();


		env.add(MeshLoader.loadObj("Models/teapot.obj", new Transform(), 2, Material.LAMBERTIAN, false, true, true));

		Pathtracer pt = new Pathtracer(camera, env);
		pt.cli(System.in);
	}
}