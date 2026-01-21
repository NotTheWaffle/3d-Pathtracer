import java.awt.Color;
import java.awt.Graphics2D;

public class Game {
	public final Input input;

	public final int width;
	public final int height;

	protected Game(int width, int height){
		input = new Input(this);
		this.width = width;
		this.height = height;
	}
	public String name(){
		return "Default";
	}
	public void tick(){
		
	}
	public void updateFrame(Graphics2D g2d){
		int xWidth = width/16;
		int yHeight = height/16;
		
		g2d.setColor(Color.gray);
		g2d.fillRect(0,0,xWidth*16,yHeight*16);
		g2d.setColor(Color.black);
		for (int i = 0; i < 16; i++){
			g2d.fillRect(xWidth*i, 0, 1, 16*yHeight);
			g2d.fillRect(0, xWidth*i, 16*xWidth, 1);
		}
		for (int y = 0; y < 16; y++){
			for (int x = 0; x < 16; x++){
				if (input.keys[y*16+x]) g2d.fillRect(x * xWidth, y * yHeight, xWidth, yHeight);
			}
		}

		switch (input.mouseDown){
			case 0 -> g2d.setColor(Color.black);
			case Input.MOUSE_LEFT -> g2d.setColor(Color.red);
			case Input.MOUSE_MIDDLE -> g2d.setColor(Color.orange);
			case Input.MOUSE_RIGHT -> g2d.setColor(Color.yellow);
			case Input.MOUSE_4 -> g2d.setColor(Color.green);
			case Input.MOUSE_5 -> g2d.setColor(Color.blue);
			default -> g2d.setColor(Color.magenta);
		}
		g2d.fillOval(input.mouseX-16, input.mouseY-16, 32, 32);
		g2d.setColor(Color.white);
		g2d.drawLine(input.mouseX, input.mouseY, (int) (Math.cos(input.mouseWheel)*16+input.mouseX), (int) (Math.sin(input.mouseWheel)*16+input.mouseY));
	}
}