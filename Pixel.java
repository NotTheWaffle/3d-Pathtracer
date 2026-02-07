public class Pixel {
	public int[] runningColor;
	public int samples;
	public Pixel(double[] color, int weight){
		this.runningColor = new int[] {
			(int) (255 * color[0]),
			(int) (255 * color[1]),
			(int) (255 * color[2])
		};
		this.samples = weight;
	}
	public Pixel(){
		this.runningColor = new int[3];
		this.samples = 0;
	}
	static int count = 0;
	static int count2 = 0;
	public void addSample(int[] color, int weight){
		if (color[0] > 255*weight || color[1] > 255*weight || color[2] > 255*weight)
			count++; 
		else 
			count2++;
		this.runningColor[0] += color[0];
		this.runningColor[1] += color[1];
		this.runningColor[2] += color[2];
		this.samples+=weight;
	}
	public int[] getColor(){
		if (samples == 0) return new int[] {0, 0, 0, 255};
		return new int[] {runningColor[0]/samples, runningColor[1]/samples, runningColor[2]/samples, 255};
	}
	public void clear(){
		runningColor[0] = runningColor[1] = runningColor[2] = 0;
		samples = 0;
	}
}