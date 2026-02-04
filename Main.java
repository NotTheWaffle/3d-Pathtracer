
import Game.Game;
import Game.Window;
import Math.Vec3;
import java.awt.Color;


public class Main {
	public static void main(String[] args){
		String model = "chain";
		int size = 100;
		if (args.length > 0){
			model = args[0];
			size = Integer.parseInt(args[1]);
		}


		Environment env = new Environment();
		
		env.add(Mesh.loadObj(model));

		// sun
		env.add(new Sphere(new Vec3(0, 20, 15), 20, Material.LIGHT));
		
		env.add(new Sphere(new Vec3(0, 0, 2.5), 1, Material.plastic(Color.RED)));
		env.add(new Sphere(new Vec3(2.5, 0, 0), 1, Material.plastic(Color.GREEN)));
		env.add(new Sphere(new Vec3(-2.5, 0, 0), 1, Material.plastic(Color.BLUE)));



		env.add(new RectangularPrism(0, 0, -2.5, 1, 1, 1, Material.PLASTIC, 0));

		// floor
		env.add(Mesh.rectangle(0, -.5, 0, 20, Material.METAL));
		
		
		runGame(new RaytracedGame(size*16, size*9, Math.PI*.5, 1, 0, env));
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