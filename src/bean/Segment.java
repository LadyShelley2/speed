package bean;

import java.io.Serializable;
import java.util.List;

public class Segment implements Serializable{
	double len; //��ǰ·�εĳ���
	double dist; //·��ĩβ������·��ʼ��ľ���
	int lane;
	List<Link> links = null; //һ��Segment�ɶ��link���
	
	public Segment(double len, double dist, List<Link> links) {
		super();
		this.len = len;
		this.dist = dist;
		this.links = links;
	}
	public double getLen() {
		return len;
	}
	public void setLen(double len) {
		this.len = len;
	}
	public double getDist() {
		return dist;
	}
	public void setDist(double dist) {
		this.dist = dist;
	}
	public int getLane() {
		return lane;
	}
	public void setLane(int lane) {
		this.lane = lane;
	}
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}
	@Override
	public String toString() {
		return lane+"\t"+len + "\t" + dist+"\t"+links.get(0).getPoints().get(0);
//		Link last = links.get(links.size()-1);
//		return links.get(0).getPoints().get(0)+"\t"+last.getPoints().get(last.getPoints().size()-1);
	}
	
}
