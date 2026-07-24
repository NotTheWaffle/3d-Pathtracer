package Game;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Game {
	public final Input input;

	protected BufferedImage nextFrame;
	protected String debug;

	public final int width;
	public final int height;

	protected Game(int width, int height){
		this.debug = "";
		this.nextFrame = new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR);
		input = new Input(this);
		this.width = width;
		this.height = height;
	}
	public String name(){
		return "Default";
	}
	public void tick(double dt){

	}
	public void generateFrame(){

	}
	public final void updateFrame(Graphics2D g2d){
		g2d.drawImage(nextFrame, 0, 0, null);
		g2d.setColor(Color.RED);
		g2d.drawString(debug, 0, 20);
	}
	public Thread start(){
		return Game.start(this);
	}
	public void run(){
		Game.run(this);
	}

	public static Thread start(Game game){
		Thread thread = new Thread(() -> Game.run(game));
		thread.start();
		return thread;
	}
	public static void run(final Game game){
		final Window window = new Window(game);
		long lastTime = System.nanoTime();
		while (true){
			final long now = System.nanoTime();
			final double deltaTime = (now - lastTime) / 1_000_000_000.0;
			lastTime = now;
			game.tick(deltaTime);
			game.generateFrame();
			window.render();
		}
	}
}