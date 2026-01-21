
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JPanel;

public class DisplayPanel extends JPanel{

	public double scaling;
	public int offsetX;
	public int offsetY;
	public final Game3d game;
	
	public DisplayPanel(int width, int height, Display d, Game3d game){
		scaling = 1;
		this.game = game;
		this.setFocusTraversalKeysEnabled(false);
		this.setFocusable(true);
	}
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D) g;
		
		g2d.translate(offsetX, offsetY);
		g2d.scale(scaling, scaling);
		game.updateDisplay(g2d);
		
		g2d.dispose();
	}
}