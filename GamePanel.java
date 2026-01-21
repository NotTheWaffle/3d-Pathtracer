
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.*;
import javax.swing.JPanel;

public class GamePanel extends JPanel{

	private final Game game;
	private final Input input;

	public boolean paused;
	public double scaling;
	public int offsetX;
	public int offsetY;
	
	public GamePanel(int width, int height, Window window, Game game){
		this.game = game;
		this.input = game.input;
		scaling = 1;
		this.setFocusTraversalKeysEnabled(false);
		this.setFocusable(true);

		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				updateMousePosition(e);
				input.mouseDown &= ~(1<<(e.getButton()-1));
			}
			@Override
			public void mousePressed(MouseEvent e) {
				updateMousePosition(e);
				input.mouseDown |= (1<<(e.getButton()-1));
			}
			@Override
			public void mouseEntered(MouseEvent e) {
				updateMousePosition(e);
			}
			@Override
			public void mouseExited(MouseEvent e) {
				updateMousePosition(e);
			}
		});
		addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent e) {
				updateMousePosition(e);
			}
			@Override
			public void mouseDragged(MouseEvent e) {
				updateMousePosition(e);
			}
		});
		addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e){
				input.mouseWheel += e.getPreciseWheelRotation();
			}
		});
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
				if (e.getKeyCode() < 256) input.keys[e.getKeyCode()] = true;
			}
			@Override
			public void keyReleased(KeyEvent e){
				if (e.getKeyCode() < 256) input.keys[e.getKeyCode()] = false;
			}
		});

	}
	public void updateMousePosition(MouseEvent e){
		input.mouseX = (int) ((e.getX()-offsetX)/scaling);
		input.mouseY = (int) ((e.getY()-offsetY)/scaling);
	}
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		
		g2d.translate(offsetX, offsetY);
		g2d.scale(scaling, scaling);
		game.updateFrame(g2d);
		
		g2d.dispose();
	}
}