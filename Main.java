
public class Main {
	public static void main(String[] args){
		int size = 1024;
		Viewport camera = new Viewport(.25f, 0, 0, size, size);
		camera.translate(0, 0, -3);

		Environment env = new Environment(Environment.SKY);

		env.addSphereTest();
		env.add(MeshLoader.loadObj("Models/dragon60k.obj", new Transform(), 5, Material.LAMBERTIAN, true, true, true));


		new Pathtracer(camera, env).run();
	}
}