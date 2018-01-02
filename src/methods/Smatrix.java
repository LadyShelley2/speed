package methods;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.jblas.DoubleMatrix;
import org.jblas.Solve;

import bean.Link;
import bean.Point2;
import bean.Road;
import bean.Segment;
import util.FileUtil;
import util.GPS2Dist;
import util.TimeFormat;

public class Smatrix {
	static int historySize =4*24*21,topK = 10; //三周的数据构造多元线性回归
	private double[][] flows = null;
	int nowIndex;
	private double[][] dist = null; //两两路段之间的距离
	public Smatrix() {
		flows = new double[historySize][CSst.segNums[CSst.segNums.length-1]];
	}
	public Smatrix(Map<String,Road> roads){
		flows = new double[historySize][CSst.segNums[CSst.segNums.length-1]];
		dist = new double[flows[0].length][flows[0].length];
		String[] all = new String[]{"G3","G15","G25","G70","G72","G76","G1501","G1514","S35"};
		int i=0;
		for(String name:all){
			List<Segment> segs = roads.get(name).getFsegs();
			for(Segment seg:segs){
				int j=0;
				Point2 p1 = midPoint(seg.getLinks());
				for(String name2:all){
					List<Segment> segs2 = roads.get(name2).getFsegs();
					for(Segment seg2:segs2){
						Point2 p2 = midPoint(seg2.getLinks());
						dist[i][j] = GPS2Dist.distance(p1, p2);
						++j;
					}
					segs2 = roads.get(name2).getBsegs();
					for(Segment seg2:segs2){
						Point2 p2 = midPoint(seg2.getLinks());
						dist[i][j] = GPS2Dist.distance(p1, p2);
						++j;
					}
				}
				++i;
			}
			segs = roads.get(name).getBsegs();
			for(Segment seg:segs){
				int j=0;
				Point2 p1 = midPoint(seg.getLinks());
				for(String name2:all){
					List<Segment> segs2 = roads.get(name2).getFsegs();
					for(Segment seg2:segs2){
						Point2 p2 = midPoint(seg2.getLinks());
						dist[i][j] = GPS2Dist.distance(p1, p2);
						++j;
					}
					segs2 = roads.get(name2).getBsegs();
					for(Segment seg2:segs2){
						Point2 p2 = midPoint(seg2.getLinks());
						dist[i][j] = GPS2Dist.distance(p1, p2);
						++j;
					}
				}
				++i;
			}
		}
//		System.out.println(dist[0][dist.length-1]);
	}
	private Point2 midPoint(List<Link> links){
		List<Point2> ps = new ArrayList<Point2>();
		for(Link link:links)
			ps.addAll(link.getPoints());
		int index = ps.size()/2;
		return ps.get(index);
	}
	public void readData(int day){
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		FileUtil fin = new FileUtil("E:\\mobiquitous2017\\sigres15\\"+day+".txt");
		String line = fin.readLine();
		long endTime = TimeFormat.parse(day+" 24:00:00").getTime()/1000;
		
		for(long T=  TimeFormat.parse(day+" 00:15:00").getTime()/1000;T<=endTime;T+=CSst.timeSlot){
			String time = df.format(new Date(T*1000));
			nowIndex = (nowIndex+1)%historySize;
			for(int i=0;i<flows[0].length;++i)
				flows[nowIndex][i] = 0;
			while(line!=null&&line.contains(time)){
				CSst.add2M(flows,nowIndex,line);
				line = fin.readLine();
			}
			for(int i=0;i<flows[0].length;++i) //如果没有数据，用前4个时间片的平均来代替
				if(flows[nowIndex][i]==0){
					int pre=4;
					double sum = 0;
					for(int j=(nowIndex-pre+historySize)%historySize;j!=nowIndex;j=(j+1)%historySize)
						sum+=flows[j][i];
					flows[nowIndex][i] = sum/pre;
				}
		}
	}
	/**
	 * 获得路段的线性回归矩阵，每一行代表一个路段与其它路段间的相关系数
	 * @return
	 */
	double[][] calcS(){
		int n=flows[0].length;
		double S[][] = new double[n][n];
		double[][]pear = getPearson(); 
		for(int i=0;i<n;++i){
			int[] top = selectTopk(pear, i); 
			double x[][] = new double[historySize][topK];
			double y[] = new double[historySize];
			for(int j=0;j<top.length;++j)
				for(int k=0;k<historySize;++k)
					x[k][j] = flows[k][top[j]];
			for(int k=0;k<historySize;++k)
				y[k] = flows[k][i];
			
			DoubleMatrix xm = new DoubleMatrix(x);
			DoubleMatrix ym= new DoubleMatrix(y);
			double[] coef=Solve.pinv(xm.transpose().mmul(xm)).mmul(xm.transpose()).mmul(ym).toArray();
			for(int j=0;j<top.length;++j)
				S[i][top[j]] = coef[j];
			S[i][i] = -1;
		}
		return S;
	}
	//根据相似函数矩阵close，选出与i最近的topK个
	int[] selectTopk(double[][] close,int i){
		int[] top = new int[topK];
		int n= flows[0].length;
		PriorityQueue<Integer> pq = new PriorityQueue<Integer>(new Comparator<Integer>(){ //建立小顶堆，存储i的K近邻
			@Override
			public int compare(Integer arg0, Integer arg1) {
				if(close[i][arg0]<close[i][arg1]) return -1;
				else if(close[i][arg0]>close[i][arg1]) return 1;
				return 0;
			}
		});
		for(int j=0;j<n;++j)
			if(j!=i&&pq.size()<topK)
				pq.add(j);
			else if(j!=i&&close[i][j]>pq.peek()){
				pq.remove();
				pq.add(j);
			}
		int k=0;
		for(Integer t:pq)
			top[k++] = t;
		return top;
	}
	//计算任意两列的     相关系数/距离
	double[][] getPearson(){
		int n = flows[0].length,m=flows.length;
		double pear[][] = new double[n][n];
		for(int i=0;i<n;++i){
			for(int j=0;j<i;++j){
				double x=0,y=0,xy=0,x2=0,y2=0;
				for(int k=0;k<m;++k){
					x+=flows[k][i];
					y+=flows[k][j];
					xy+=flows[k][i]*flows[k][j];
					x2+=flows[k][i]*flows[k][i];
					y2+=flows[k][j]*flows[k][j];
				}
				pear[i][j] = pear[j][i]=(m*xy-x*y)/Math.sqrt((m*x2-x*x)*(m*y2-y*y));///dist[i][j];
			}
			pear[i][i]=1;
		}
		return pear;
	}
	public static void main(String[] args) {
//		Smatrix sm = new Smatrix();
//		sm.flows = new double[][]{{1,40,50},{2,50,60},{3,70,70},{4,80,80}};
//		double p[][] = sm.getPearson();
//		for(int i=0;i<p.length;++i){
//			for(int j=0;j<p[0].length;++j)
//				System.out.print(p[i][j]+" ");
//			System.out.println();
//		}
	}
}
