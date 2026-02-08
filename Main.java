
import Game.Game;
import Game.Window;


public class Main {
	public static void main(String[] args){
		String model = "Models/"+"trispervertex"+".obj";
		int size = 512;

		Environment env = new Environment();
		
		env.add(Mesh.loadObj(model, new Transform(), 2, Material.MIRROR, true));


		env.addStanfordBox(2.5, 3);
		
		runGame(new PathTracedGame(new Viewport(Math.PI*.5, size), env));
	}
	public static Thread runGame(final Game game){
		Thread t1 = new Thread(
			() -> {
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
		);
		
		t1.start();
		return t1;
	}
}