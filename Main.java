
import Game.Game;
import Game.Window;
import Math.Vec3;
import java.awt.Color;
import java.util.concurrent.locks.LockSupport;


public class Main {
	public static void main(String[] args){
		Environment env = new Environment();
		env.physicalObjects.add(Mesh.loadObj("icosahedron", true, Color.green, Material.SOLID));
		double a = 2;
		double skyHeight = 4;
		double floorHeight = -.5;
		if (floorHeight == -.1){
			env.physicalObjects.add(new Triangle(
				new Point(new Vec3(-a, skyHeight, -a), 0),
				new Point(new Vec3(a, skyHeight, -a), 0),
				new Point(new Vec3(-a, skyHeight, a), 0),
				Material.LIGHT, Color.white)
			);
			env.physicalObjects.add(new Triangle(
				new Point(new Vec3(a, skyHeight, a), 0),
				new Point(new Vec3(-a, skyHeight, a), 0),
				new Point(new Vec3(a, skyHeight, -a), 0),
				Material.LIGHT, Color.white)
			);
		}

		env.physicalObjects.add(new Triangle(
			new Point(new Vec3(-a, floorHeight, a), 0),
			new Point(new Vec3(a, floorHeight, -a), 0),
			new Point(new Vec3(-a, floorHeight, -a), 0),
			Material.SOLID, Color.white)
		);
		env.physicalObjects.add(new Triangle(
			new Point(new Vec3(-a, floorHeight, a), 0),
			new Point(new Vec3(a, floorHeight, a), 0),
			new Point(new Vec3(a, floorHeight, -a), 0),
			Material.SOLID, Color.white)
		);
		env.physicalObjects.add(new Sphere(new Vec3(5, 5,5), 5, Color.white, Material.LIGHT));
		env.physicalObjects.add(new Sphere(new Vec3(2, .5, 0), .5, Color.white, Material.SOLID));
		//env.physicalObjects.add(new Sphere(new Vec3(1, 1, 0), .5, Color.white, Material.SOLID));
		
		System.out.println(env.physicalObjects);
		System.out.println(runGame(new RaytracedGame(512, 512, 2, Math.PI/2, env), 30).isVirtual());
	}
	public static Thread runGame(final Game game, final double fps){
		Thread t1 = new Thread(
			() -> {
				final Window window = new Window(game);
				final long frameLength = (long) (1_000_000_000/fps);
				
				while (true){
					long targetTime = System.nanoTime() + frameLength;

					game.tick();
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
		t1.start();
		return t1;
	}
}