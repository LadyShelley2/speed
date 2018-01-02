package methods;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jblas.DoubleMatrix;

import bean.Road;
import util.FileUtil;
import util.TimeFormat;

/**
 * done: read road data and speed data normally;
 * undo: S matrix remained to be fixed; estimated;
 * @author lbb
 *
 */
public class CSSpatialTemporal{
	String base_url = "E:\\mobiquitous2017";
	private String base_indir = "E:\\mobiquitous2017\\vdres15\\";
	private String base_outdir = "E:\\mobiquitous2017\\vdres15\\";
	
	private static int slotsCount = 4*24*7;
	static String ratio = "-0.9";
	public static int timeSlot = 15*60;
	
	
	private double[][] M;
	private double[][] S;
	private double[][] T;
	
	private Set<String> validSeg = new HashSet<String>(); // 采样路段
	
	private int trainingStart = 20161115;
	private int trainingEnd = 20161128;
	private int testingStart = 20161129;
	private int testingEnd = 20161130;
	

	private RoadPicker roadPicker;
	static Map<String,Road> roads = new HashMap<String, Road>(); 
	static List<String> names = new ArrayList<String>();
	static int[] segNums;
	
	CSSpatialTemporal(){
		roadPicker = new RoadPicker();
		segNums = roadPicker.getSegNums();
		roads = roadPicker.getRoads();
		names = roadPicker.getNames();
		
		M = new double[slotsCount][segNums[segNums.length-1]];
		
		System.out.println("M matrix is " + M.length + " x " + M[0].length);
		
		// fill data in the matrix
		
		readValidSeg();
		
		fillData(trainingStart,testingEnd);
		
		S = getSmatrix();
		T = genToep();
		
		System.out.println("M matrix is filled ");
	}
	
	public double[][] estimate(){
		double res[][] = new CSstALS(new DoubleMatrix(M),new DoubleMatrix(S),
				new DoubleMatrix(T)).estimate();
		return res;
	}
	
	private void fillData(int fromDay, int toDay) {
		int currRow = -1;
		for(int curr = fromDay; curr < toDay; curr++) {
			currRow = dayToM(curr, currRow);
		}
	}
	
	private int dayToM( int day, int currRow) {
		
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		FileUtil fin = new FileUtil(base_indir + day + ".txt");
		FileUtil fout = new FileUtil(base_outdir + day + ratio + "-" + CSstALS.r + 
				String.format("-%.1f-%.1f", CSstALS.lamda,CSstALS.yita)+"-5.txt");
		
		String line = fin.readLine();		
		long endTime = TimeFormat.parse(day+" 24:00:00").getTime()/1000;		
		for(long T = TimeFormat.parse(day +" 00:15:00").getTime()/1000;T<=endTime;T+=timeSlot) {
			String time = df.format(new Date(T*1000));
			currRow = (currRow+1) % slotsCount;
			Arrays.fill(M[currRow], -1);
			
			while(line!=null && line.contains(time)){
				String strs[] = line.split("\t");
				if(validSeg.contains(strs[1]+"\t"+strs[2]+"\t"+strs[3])) {
					itemToM(M, currRow, line);
				}
				line = fin.readLine();				
			}
		}
		
		fin.close();
		return currRow;
	}
	
	static void itemToM(double[][] flows,int nowIndex,String line){
		String parts[] = line.split("\t");
		int seg = Integer.valueOf(parts[2]);
		int dir = Integer.valueOf(parts[3]);
		
		int index = seg + dir*roads.get(parts[1]).getFsegs().size()
				+ segNums[names.indexOf(parts[1])];
		
		double speed = Double.valueOf(parts[4]);
		
		flows[nowIndex][index] = speed;
	}
	
	private void readValidSeg(){
		FileUtil fin = new FileUtil(base_indir+"vd"+ratio+"-5.txt");
		String str = null;
		while((str = fin.readLine())!=null)
			validSeg.add(str);
	}
	
	private double[][] genToep() {
		int m = slotsCount;
		double[][]Toep = new double[m-1][m];
		for(int i=0;i<m-1;++i){
			Toep[i][i]=1;
			Toep[i][i+1]=-1;
		}		
		return Toep;
	}
	
	private double[][] getSmatrix(){
		Smatrix sm  =  new Smatrix();
		for(int i = trainingStart;i<trainingEnd;i++) {
			sm.readData(i);
		}
		double[][] S = sm.calcS();
		return S;
	}
	
	public static void main(String[] args){
		double[][] res = new CSSpatialTemporal().estimate();
		System.out.print("Res length:"+res.length+" * "+res[0].length);
	}
		
}

/**
 * Read Serialized road data from roadObj
 * @author lbb
 *
 */
class RoadPicker{
	
	private List<String> names = new ArrayList<String>();
	private Map<String,Road> roads = new HashMap<String,Road>();
	private int[] segNums;
	
	RoadPicker(){
		
		String[] all = new String[]{"G3","G15","G25","G70","G72","G76","G1501","G1514","S35"};
		
		roads = (HashMap<String,Road>)new FileUtil("fujian/roadObj").readObj();
		segNums = new int[all.length+1]; //正反向路段数不一样，线路之前的路段数
		
		for(int i=1;i<=all.length;++i){
			segNums[i] = roads.get(all[i-1]).getFsegs().size()+roads.get(all[i-1]).getBsegs().size()+segNums[i-1];
			names.add(all[i-1]);
 		}
	}
	
	public List<String> getNames(){
		return this.names;
	}
	
	public Map<String,Road> getRoads(){
		return this.roads;
	}
	
	/**
	 * get total road segments number
	 * @return
	 */
	public int[] getSegNums() {
		return this.segNums;
	}
}						