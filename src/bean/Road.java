package bean;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class Road implements Serializable{
	String name;
	List<Segment> fsegs; //按照匝道划分形成的路段
	List<Segment> bsegs;
	
	List<Point2> fsites; //一个方向上有序的基站信息
	Map<Point2,Integer> fsiteSegs; //一个基站对应的该线路的路段编号
	List<Point2> bsites;
	Map<Point2,Integer> bsiteSegs;
	
	public Road(String name) {
		super();
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Segment> getFsegs() {
		return fsegs;
	}
	public void setFsegs(List<Segment> fsegs) {
		this.fsegs = fsegs;
	}
	public List<Segment> getBsegs() {
		return bsegs;
	}
	public void setBsegs(List<Segment> bsegs) {
		this.bsegs = bsegs;
	}
	public List<Point2> getFsites() {
		return fsites;
	}
	public void setFsites(List<Point2> fsites) {
		this.fsites = fsites;
	}
	public Map<Point2, Integer> getFsiteSegs() {
		return fsiteSegs;
	}
	public void setFsiteSegs(Map<Point2, Integer> fsiteSegs) {
		this.fsiteSegs = fsiteSegs;
	}
	public List<Point2> getBsites() {
		return bsites;
	}
	public void setBsites(List<Point2> bsites) {
		this.bsites = bsites;
	}
	public Map<Point2, Integer> getBsiteSegs() {
		return bsiteSegs;
	}
	public void setBsiteSegs(Map<Point2, Integer> bsiteSegs) {
		this.bsiteSegs = bsiteSegs;
	}
	
}
