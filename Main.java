
import Game.Game;
import Game.Window;
import Math.FloatMath;

//TODO add multi importance sampling
public class Main {
	public static void main(String[] args){
		int size = 1024;
		Viewport camera = new Viewport(FloatMath.PI*.5f, 0, 0, size, size);
		camera.translate(0, 0, -1);

		Environment env = new Environment(true);
		//env.add(MeshLoader.loadObj("Models/dragon1mil.obj", new Transform(), 1, Material.SOLID));
			//ImplicitEquation eq = new ImplicitEquation((x, y, z) -> (float) ((y * Math.sqrt(-2*Math.log(x*x+y*y)/(x*x+y*y)))-z));
			//env.add(MeshLoader.loadImplicitEquation(eq, new AABB(-4, -4, -4, 4, 4,4), .1f, Material.solid(Color.RED)));
		//env.add(new Sphere(1, Material.GLASS));
		//env.addFloor();
		//env.addSphereTest();
		env.addCornellBox(2, 2.5f);

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