
import Game.Game;
import Game.Window;
import Math.Vec3;
import java.awt.Color;
import java.util.concurrent.locks.LockSupport;


public class Main {
	public static void main(String[] args){
		String model = "tetrahedron";
		double fps = 0;
		int size = 512;
		if (args.length > 0){
			model = args[0];
			size = Integer.parseInt(args[1]);
		}
		Environment env = new Environment();
		// model
		env.physicalObjects.add(Mesh.loadObj(model, true, Color.WHITE, Material.MIRROR));

		// floor
		env.physicalObjects.add(PhysicalObject.rectangle(0, -.5, 0, 20, Color.WHITE, Material.SOLID));

		// sun
		env.physicalObjects.add(new Sphere(new Vec3(0, 4, 7), 5, Color.WHITE, Material.LIGHT));

		// refraction test
		env.physicalObjects.add(new Sphere(new Vec3(2, 0, 0), 1, Color.WHITE, Material.MIRROR));
		
		runGame(new RaytracedGame(size, size, Math.PI/2, 1, env), fps);
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