
import Game.Game;
import Game.Window;
import Math.Vec3;
import java.awt.Color;
import java.util.concurrent.locks.LockSupport;


public class Main {
	public static void main(String[] args){
		String model = "chain";
		double fps = 0;
		int size = 512;
		if (args.length > 0){
			model = args[0];
			size = Integer.parseInt(args[1]);
		}


		Environment env = new Environment();
		
		env.add(Mesh.loadObj(model));

		// sun
		env.add(new Sphere(new Vec3(0, 20, 15), 20, Material.LIGHT));
		
		env.add(new Sphere(new Vec3(0, .25, 2.5), 1, Material.solid(Color.RED)));
		env.add(new Sphere(new Vec3(2.5, .25, 0), 1, Material.solid(Color.GREEN)));
		env.add(new Sphere(new Vec3(-2.5, .25, 0), 1, Material.solid(Color.BLUE)));

		// floor
		env.add(Mesh.rectangle(0, -1, 0, 40, Material.SOLID));
		
		
		runGame(new RaytracedGame(size, size, Math.PI*.5, 1, env), fps);
	}
	public static Thread runGame(final Game game, final double fps){
		Thread t1;
		if (fps == 0){
			// frame unlimited
			t1 = new Thread(
				() -> {
					final Window window = new Window(game);
					long lastTime = System.nanoTime();
					while (true){
						long now = System.nanoTime();
						double deltaTime = (now - lastTime) / 1_000_000.0;
						lastTime = now;
						game.tick(deltaTime);
						game.generateFrame();
						window.render();
					}
				}
			);
		} else {
			t1 = new Thread(
				() -> {
					final Window window = new Window(game);
					final long frameLength = (long) (1_000_000_000/fps);
					
					while (true){
						long targetTime = System.nanoTime() + frameLength;

						game.tick(frameLength/1_000_000.0);
						game.generateFrame();
						window.render();
						long remaining = targetTime - System.nanoTime();
						if (remaining > 100_000) {
							LockSupport.parkNanos(remaining - 2_000);
						}
						while (System.nanoTime() < targetTime) {
							Thread.onSpinWait();
						}
					}
				}
			);
		}
		t1.start();
		return t1;
	}
}