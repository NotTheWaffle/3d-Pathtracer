package Math;

import java.util.Objects;

public class Trio<T0, T1, T2>{
	public final T0 t0;
	public final T1 t1;
	public final T2 t2;
	public Trio(T0 t0, T1 t1, T2 t2){
		this.t0 = t0;
		this.t1 = t1;
		this.t2 = t2;
	}
	@Override
	public int hashCode(){
		return Objects.hashCode(t0) ^ Objects.hashCode(t1) ^ Objects.hashCode(t2);
	}
	@Override
	public boolean equals(Object o){
		if (o == this) return true;
		return (o instanceof Trio p && Objects.equals(p.t0, t0) && Objects.equals(p.t1, t1) && Objects.equals(p.t2, t2));
	}
	@Override
	public String toString(){
		return "("+Objects.toString(t0)+", "+Objects.toString(t1)+", "+Objects.toString(t2)+")";
	}
}
