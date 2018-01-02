package bean;

import java.io.Serializable;
import java.util.List;

public class Link implements Serializable{
	long id;
	int dir;
	double len; //pline的长度
	int lane; //Pline的车道数
	String kind = null;
	List<Point2> points = null;
	
	public Link(long id, int dir, double len) {
		super();
		this.id = id;
		this.dir = dir;
		this.len = len;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public int getDir() {
		return dir;
	}
	public void setDir(int dir) {
		this.dir = dir;
	}
	public double getLen() {
		return len;
	}
	public void setLen(double len) {
		this.len = len;
	}
	public int getLane() {
		return lane;
	}
	public void setLane(int lane) {
		this.lane = lane;
	}
	public String getKind() {
		return kind;
	}
	public void setKind(String kind) {
		this.kind = kind;
	}
	public List<Point2> getPoints() {
		return points;
	}
	public void setPoints(List<Point2> points) {
		this.points = points;
	}
	@Override
	public String toString() {
		return id + "\t" + dir + "\t" + len + "\t" + kind;
	}
	
}
