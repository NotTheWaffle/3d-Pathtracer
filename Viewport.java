
import Math.FloatMath;
import Math.Vec3;


public class Viewport extends Transform {
	public final float fov;
	public final float focalLength;

	public final float focusDistance;
	public final float focus;

	public final int screenWidth;
	public final int screenHeight;
	public final int pixels;
	public final int cx;
	public final int cy;

	public Viewport(float fov, int screenWidth, int screenHeight){
		this(fov, 0, 0, screenWidth, screenHeight);
	}
	public Viewport(float fov, float focusDistance, float focus, int screenWidth, int screenHeight){
		super();
		this.fov = FloatMath.PI * 2 * Math.max(Math.min(fov, .49f), 0f);
		this.focalLength = (screenWidth / (2 * FloatMath.tan(this.fov/2)));

		this.focusDistance = focusDistance;
		this.focus = focus;

		this.screenWidth = screenWidth;
		this.screenHeight = screenHeight;
		pixels = screenWidth * screenHeight;
		this.cx = screenWidth/2;
		this.cy = screenHeight/2;
	}

	public float getX(Vec3 p){
		return ((focalLength * p.x / p.z)+cx);
	}
	public float getY(Vec3 p){
		return (cy-(focalLength * p.y / p.z));
	}

	public Viewport moveX(float x){
		move(x, 0, 0);
		return this;
	}
	public Viewport moveY(float y){
		move(0, y, 0);
		return this;
	}
	public Viewport moveZ(float z){
		move(0, 0, z);
		return this;
	}

	public Viewport translateX(float x){
		translate(x, 0, 0);
		return this;
	}
	public Viewport translateY(float y){
		translate(0, y, 0);
		return this;
	}
	public Viewport translateZ(float z){
		translate(0, 0, z);
		return this;
	}

	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		if (o instanceof Viewport v){
			return v.translation.equals(translation) && v.rot.equals(rot)
				&& v.fov == fov && v.focalLength == focalLength
				&& v.focusDistance == focusDistance && v.focus == focus
				&& v.screenWidth == screenWidth && v.screenHeight == screenHeight
				&& v.pixels == pixels && v.cx == cx && v.cy == cy;
		} else {
			return false;
		}
	}
	@Override
	public int hashCode(){
		return translation.hashCode() ^ rot.hashCode() ^ Float.hashCode(fov) ^ Float.hashCode(focalLength) ^ Float.hashCode(focusDistance) ^ Float.hashCode(focus) ^ screenWidth ^ screenHeight ^ cx ^ cy;
	}
	@Override
	public String toString(){
		return "Viewport "+screenWidth+"x"+screenHeight+" camera with transform"+super.toString();
	}
}
