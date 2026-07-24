
import Math.Pair;


public class Main {
	public static void main(String[] args){
		int size = 1024;
		Viewport camera = new Viewport(.25f, 0, 0, size, size);
		camera.translate(0, 0, -3);

		Environment env = new Environment(Environment.SUN);

		env.addSphereTest();
		Pair<Portal, Portal> portals = Portal.makePortalPair(1, 1);
		env.add(portals.t0);
		env.add(portals.t1);

		new Pathtracer(camera, env).run();
	}
}