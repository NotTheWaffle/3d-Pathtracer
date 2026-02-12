
import Game.Game;
import Game.Window;


public class Main {
	public static void main(String[] args){
		String model = "Models/"+"dragon1mil"+".obj";
		int size = 512;
		
		Environment env = new Environment();
		env.add(MeshLoader.loadObj(model, new Transform(), 1, Material.SOLID, true));

		env.addSphereTest();
		env.addStanfordBox(2, 2.1);
		
		runGame(new AsyncVirtualThreadedPathtracedGame(new Viewport(Math.PI*.5, size, size), env));
	}
	public static Thread startGame(final Game game){
		Thread t1 = new Thread(() -> runGame(game));
		t1.start();
		return t1;
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