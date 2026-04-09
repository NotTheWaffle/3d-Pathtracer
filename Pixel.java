

public class Pixel {
	private int rColor;
	private int gColor;
	private int bColor;
	private int samples;
	private boolean reset;

	public Pixel(){
		this(new float[3], 0);
	}
	public Pixel(float[] color, int weight){
		this.rColor = (int) (255 * color[0]);
		this.gColor = (int) (255 * color[1]);
		this.bColor = (int) (255 * color[2]);
		this.samples = weight;
	}
	public void addSample(float[] color){
		if (reset){
			clear();
		}
		rColor += (int) (255.0 * color[0]);
		gColor += (int) (255.0 * color[1]);
		bColor += (int) (255.0 * color[2]);
		samples++;
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
		reset = false;
	}
	public void reset(){
		reset = true;
	}
	public int getSamples(){
		return samples;
	}
}