
import java.util.concurrent.locks.LockSupport;


public class Main {
	public static void main(String[] args){
		Environment env = new Environment("teapot");
		runGame(new RasterizedGame(512, 512, Math.PI/2, env), 30);
		//runGame(new RaytracedGame(512, 512, Math.PI/2, env), 30);
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