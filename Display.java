import java.awt.event.*;
import javax.swing.*;

public class Display {
	
	private final JFrame frame;

	protected final DisplayPanel gamePanel;

	public Display(Game3d game){
		int width  = 8  + game.width  + 8;
		int height = 31 + game.height + 8;

		this.frame = new JFrame(game.name());
		this.frame.setBounds(0, 0, width, height);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true); 
		this.frame.setLayout(null);
		this.frame.setResizable(true);
		

		this.gamePanel = new DisplayPanel(game.width, game.height, this, game);
		this.gamePanel.setBounds(0, 0, game.width, game.height);
		this.frame.add(gamePanel);
		
		
		

		frame.addComponentListener(new ComponentAdapter(){
			@Override
			public void componentResized(ComponentEvent e){
				gamePanel.setSize(e.getComponent().getWidth(), e.getComponent().getHeight());
				gamePanel.scaling = Math.min((gamePanel.getWidth()-16)/(double)game.width,(gamePanel.getHeight()-39)/(double)game.height);
				gamePanel.offsetX = (int) ((getWidth()-(game.width*gamePanel.scaling))/2);
				gamePanel.offsetY = (int) ((getHeight()-(game.height*gamePanel.scaling))/2);
			}
		});


		gamePanel.requestFocus();
	}

	public int getWidth(){
		return frame.getWidth()-16;
	}

	public int getHeight(){
		return frame.getHeight()-39;
	}

	public void render(){
		gamePanel.repaint();
	}
}