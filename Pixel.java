public class Pixel {
	public double[] color;
	public int weight;
	public Pixel(double[] color, int weight){
		this.color = color;
		this.weight = weight;
	}
	public Pixel(){
		this.color = new double[] {0, 0, 0};
		this.weight = 0;
	}
	public void addSample(double[] color, int weight){
		this.color[0] = (this.color[0]*this.weight + color[0] * weight)/(this.weight+weight);
		this.color[1] = (this.color[1]*this.weight + color[1] * weight)/(this.weight+weight);
		this.color[2] = (this.color[2]*this.weight + color[2] * weight)/(this.weight+weight);
		this.weight += weight;
	}
}
