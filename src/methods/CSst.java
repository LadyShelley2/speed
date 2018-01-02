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

public class CSst {
	static int historySize =4*24*7,timeSlot = 15*60,beginDay = 20161129;
	public static List<String> names = new ArrayList<String>();
	public static int segNums[] = null;
	static Map<String,Road> roads = (HashMap<String,Road>)new FileUtil("fujian/roadObj").readObj();
	private double[][] flows = null;
	int nowIndex;
	static FileUtil fout = null;
	static String ratio = "-0.9";
	Set<String> validSeg = new HashSet<String>(); //采样的路段
	double[][] S = null,Toep = null;
	
	static {
		String[] all = new String[]{"G3","G15","G25","G70","G72","G76","G1501","G1514","S35"};
		segNums = new int[all.length+1]; //正反向路段数不一样，线路之前的路段数
		for(int i=1;i<=all.length;++i){
			segNums[i] = roads.get(all[i-1]).getFsegs().size()+roads.get(all[i-1]).getBsegs().size()+segNums[i-1];
			names.add(all[i-1]);
 		}
	}
	public CSst(){
		flows = new double[historySize][segNums[segNums.length-1]];
		System.out.println("matrix size "+historySize+" x "+flows[0].length);
	}
	/**
	 * 将line中的信息解析出来存入flows的第nowIndex行中
	 * @param flows
	 * @param nowIndex
	 * @param line
	 */
	static void add2M(double[][] flows,int nowIndex,String line){
		String parts[] = line.split("\t");
		int seg = Integer.valueOf(parts[2]);
		int dir = Integer.valueOf(parts[3]);
		int index = seg+dir*roads.get(parts[1]).getFsegs().size()+segNums[names.indexOf(parts[1])];
		double speed = Double.valueOf(parts[4]);
		flows[nowIndex][index] = speed;
	}
	public void prepareData(String inDir,int day,String outDir){
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		FileUtil fin = new FileUtil(inDir+day+".txt");
		fout = new FileUtil(outDir+day+ratio+"-"+CSstALS.r+
//				String.format("-%.1f", CSstALS.lamda)+".txt");
				String.format("-%.1f-%.1f", CSstALS.lamda,CSstALS.yita)+"-5.txt");
		String line = fin.readLine();
		
		long endTime = TimeFormat.parse(day+" 24:00:00").getTime()/1000;
		for(long T=  TimeFormat.parse(day+" 00:15:00").getTime()/1000;T<=endTime;T+=timeSlot){
			String time = df.format(new Date(T*1000));
			nowIndex = (nowIndex+1)%historySize;
			Arrays.fill(flows[nowIndex], -1);
//			System.out.println("CSst read data begin");
			while(line!=null&&line.contains(time)){
				String strs[] = line.split("\t");
				if(validSeg.contains(strs[1]+"\t"+strs[2]+"\t"+strs[3]))
					add2M(flows,nowIndex,line);
				line = fin.readLine();
			}
//			System.out.println("CSst read data end");
			if(day>=beginDay){
				double res[][] = new CSstALS(new DoubleMatrix(flows),new DoubleMatrix(S),
						new DoubleMatrix(Toep)).estimate();
				output(res,time,nowIndex);
				System.out.println(time+" is done "+System.currentTimeMillis()/1000);
			}
		}
		fin.close();
//		fout.close();
	}
	static void output(double[][] res,String time,int nowIndex){
		int start = 0;
//		System.out.println("output");
		for(String name:names){
			int size = roads.get(name).getFsegs().size();
			for(int i=0;i<size;++i,++start)
				if(res[nowIndex][start]>=0)
					fout.writeLine(time+"\t"+name+"\t"+i+"\t0\t"+String.format("%.2f", res[nowIndex][start]));
			size = roads.get(name).getBsegs().size();
			for(int i=0;i<size;++i,++start)
				if(res[nowIndex][start]>=0)
					fout.writeLine(time+"\t"+name+"\t"+i+"\t1\t"+String.format("%.2f", res[nowIndex][start]));
		}
	}
	private void genToep(){
		int m = historySize;
		Toep = new double[m-1][m];
		for(int i=0;i<m-1;++i){
			Toep[i][i]=1;
			Toep[i][i+1]=-1;
		}
	}
	private void readValidSeg(){
		FileUtil fin = new FileUtil("E:\\mobiquitous2017\\vdres15\\vd"+ratio+"-5.txt");
		String str = null;
		while((str = fin.readLine())!=null)
			validSeg.add(str);
	}
	public static void main(String[] args) {
		CSst sense = new CSst();
		Smatrix sm = new Smatrix();//roads
		for(int day = 20161101;day<beginDay;++day)
			sm.readData(day);
		sense.S = sm.calcS();
		sense.genToep();
		sense.readValidSeg();
		
		for(int day = 20161115;day<=20161130;++day){
//			sense.prepareData("E:\\mobisys2017\\regress\\ridgeUpdate-2-4\\",day,"E:\\mobisys2017\\cs\\ridgeUpdate-2-4\\");
			sense.prepareData("E:\\mobiquitous2017\\vdres15\\",day,"E:\\mobiquitous2017\\cs\\st0.9\\");
			if(day>=beginDay){
				sm.readData(day);
				sense.S = sm.calcS();
			}
		}
	}
}
