
import Game.Game;
import Game.Window;
import Math.FloatMath;
import Math.Vec3;
import java.awt.Color;

//TODO add multi importance sampling
public class Main {
	public static void main(String[] args){
		int size = 1024;
		Viewport camera = new Viewport(FloatMath.PI*.5f, 0, 0, 1024, 1024);
		camera.translate(0, 0, 0);

		Environment env = new Environment(false);
		//env.add(MeshLoader.loadObj("Models/dragon1mil.obj", new Transform(), 1, Material.SOLID));
			//ImplicitEquation eq = new ImplicitEquation((x, y, z) -> (float) ((y * Math.sqrt(-2*Math.log(x*x+y*y)/(x*x+y*y)))-z));
			//env.add(MeshLoader.loadImplicitEquation(eq, new AABB(-4, -4, -4, 4, 4,4), .1f, Material.solid(Color.RED)));
		//env.add(new Sphere(1, Material.GLASS));
		int k = 8;
		for (int x = 0; x < k; x++){
			for (int y = 0; y < k; y++){
				env.add(new Sphere(new Vec3(x, 0, y), .5f, Material.reflective(Color.RED, x/(float)k, y/(float)k)));
			}
		}

		//env.add(new RectangularPrism(0, 0, 0, 2, 2, 2, Material.GLASS, 0));
		env.addFloor();
		//env.addHueSpheres(8, 1f);
		//env.addSphereTest();
		//env.addCornellBox(2, 2.5f);

		runGame(new AsyncVirtualThreadedPathtracedGame(camera, env));
	}
	
	public static Thread startGame(Game game){
		Thread thread = new Thread(() -> runGame(game));
		thread.start();
		return thread;
	}
	public static void runGame(final Game game){
		final Window window = new Window(game);
		long lastTime = System.nanoTime();
		while (true){
			final long now = System.nanoTime();
			final double deltaTime = (now - lastTime) / 1_000_000.0;
			lastTime = now;
			game.tick(deltaTime);
			game.generateFrame();
			window.render();
		}
	}
}