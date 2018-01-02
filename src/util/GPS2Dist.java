package util;

import bean.Point2;


public class GPS2Dist {
	public static double interval = 111.31955;
	
	public static double distance(Point2 a,Point2 b){
		if(a==null||b==null) return -1;
		double y = (a.getY()-b.getY())*interval;
		double x = (a.getX()-b.getX())*Math.cos((a.getY()+b.getY())/2.0/180*Math.PI)*interval;
		return Math.sqrt(x*x+y*y);
	}
	public static void main(String[] args) {
		Point2 a = new Point2(119.09529,26.13662);
		Point2 b = new Point2(119.0917852,26.13790474);
		System.out.println(distance(a,b));
	}
}
