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
		this.runningColor = new int[] {0, 0, 0};
		this.samples = 0;
	}
	public void addSample(int[] color, int weight){
		this.runningColor[0] += color[0];
		this.runningColor[1] += color[1];
		this.runningColor[2] += color[2];
		this.samples+=weight;
	}
	public void addSample(int[] color, int weight, boolean flag){
		this.runningColor[0] = (this.runningColor[0] * this.samples + color[0] * weight) / (this.samples + weight);
		this.runningColor[1] = (this.runningColor[1] * this.samples + color[1] * weight) / (this.samples + weight);
		this.runningColor[2] = (this.runningColor[2] * this.samples + color[2] * weight) / (this.samples + weight);
		this.samples += weight;
	}
	public int[] getColor(){
		if (samples == 0) return new int[] {0, 0, 0};
		return new int[] {runningColor[0]/samples, runningColor[1]/samples, runningColor[2]/samples};
	}
	public void clear(){
		runningColor[0] = runningColor[1] = runningColor[2] = 0;
		samples = 0;
	}
}
