import java.awt.event.*;
import javax.swing.*;

public class Window {
	
	private final JFrame frame;

	protected final GamePanel gamePanel;
	protected final Input input;

	public Window(Game game){
		this.input = game.input;
		int width  = 8  + game.width  + 8;
		int height = 31 + game.height + 8;

		this.frame = new JFrame(game.name());
		this.frame.setBounds(0, 0, width, height);
		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.frame.setVisible(true); 
		this.frame.setLayout(null);
		this.frame.setResizable(true);
		

		this.gamePanel = new GamePanel(game.width, game.height, this, game);
		this.gamePanel.setBounds(0, 0, game.width, game.height);
		this.frame.add(gamePanel);
		
		
		frame.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e){
				gamePanel.paused = false;
			}
			@Override
			public void focusLost(FocusEvent e){
				gamePanel.paused = true;
				input.reset();
			}
		});

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