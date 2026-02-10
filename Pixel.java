

public class Pixel {
	private int rColor;
	private int gColor;
	private int bColor;
	public int samples;

	public Pixel(){
		this(new double[3], 0);
	}
	public Pixel(double[] color, int weight){
		this.rColor = (int) (255 * color[0]);
		this.gColor = (int) (255 * color[1]);
		this.bColor = (int) (255 * color[2]);
		this.samples = weight;
	}
	public void addSample(int[] color, int weight){
		this.rColor += color[0];
		this.gColor += color[1];
		this.bColor += color[2];
		this.samples+=weight;
	}
	public int[] getColor(){
		if (samples == 0) return new int[] {0, 0, 0, 255};
		return new int[] {rColor/samples, gColor/samples, bColor/samples, 255};
	}
	public void clear(){
		rColor = gColor = bColor = 0;
		samples = 0;
	}
}