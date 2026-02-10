

public class PixelSTDEV extends Pixel{
	private double rMean;
	private double rM2;
	private double gMean;
	private double gM2;
	private double bMean;
	private double bM2;

	private int n;

	public PixelSTDEV(){
		this(new double[3], 0);
	}
	public PixelSTDEV(double[] color, int weight){
		super(color, weight);
	}
	public void addSample(int[] color, int weight){
		super.addSample(color, weight);

		n++;
		double rDelta1 = color[0] - rMean;
		double gDelta1 = color[1] - gMean;
		double bDelta1 = color[2] - bMean;
		rMean += rDelta1/n;
		gMean += gDelta1/n;
		bMean += bDelta1/n;
		double rDelta2 = color[0] - rMean;
		double gDelta2 = color[1] - gMean;
		double bDelta2 = color[2] - bMean;
		rM2 += rDelta1*rDelta2;
		gM2 += gDelta1*gDelta2;
		bM2 += bDelta1*bDelta2;
	}
	private double getRSTDEV() {
		return Math.sqrt(n > 3 ? rM2 / n : Double.POSITIVE_INFINITY);
	}
	private double getGSTDEV() {
		return Math.sqrt(n > 3 ? gM2 / n : Double.POSITIVE_INFINITY);
	}
	private double getBSTDEV() {
		return Math.sqrt(n > 3 ? bM2 / n : Double.POSITIVE_INFINITY);
	}
	public double getSTDEV(){
		return (getRSTDEV()+getGSTDEV()+getBSTDEV())/3;
	}
	public void clear(){
		super.clear();
		rMean = gMean = bMean = rM2 = gM2 = bM2 = 0.0;
		n = 0;
	}
}