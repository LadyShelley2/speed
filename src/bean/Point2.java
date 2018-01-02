package bean;

import java.io.Serializable;

public class Point2 implements Serializable{
	double x,y; //¾­¶È Î³¶È
	
	public Point2() {
		super();
	}
	public Point2(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(x);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(y);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Point2 other = (Point2) obj;
		if (Math.abs(x-other.x)>1e-5)
			return false;
		if (Math.abs(y-other.y)>1e-5)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return x + "\t" + y ;
	}
	public static void main(String[] args) {
		System.out.println(new Point2(0.00001,0.00002).equals(new Point2(0.00001,0.00002)));
		System.out.println(new Point2(0.00001,0.00002).equals(new Point2(0.000015,0.000022)));
		System.out.println(new Point2(0.00001,0.00002).equals(new Point2(0.000021,0.00002)));
	}
}
