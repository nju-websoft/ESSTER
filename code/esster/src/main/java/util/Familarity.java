/**
 * 
 */
//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.special.Gamma;

import com.whirlycott.cache.Cache;

//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.Constants.dbName;
import main.java.util.Constants.dbName;
//import cn.edu.nju.ws.es.web.algo.util.Constants;
//import cn.edu.nju.ws.es.web.algo.util.Constants.dbName;
import qxliu.tools.tuples.TwoTuple;

/**
 * @author qxliu 2019 2019年10月21日 下午3:53:24
 *
 */
public class Familarity {
	public static Cache propFamCache = Constants.initCache("propFam");

	public static long totalPage = 15199285L;// min=6.5792568532e-8
	public static long totalVolume = 999999L;//all
	
	public static enum FW_TYPE{onegram, twogram, onetwogram};
	/**
	 * for old implements
	 * @param puriBracketOrNot
	 * @param dbname
	 * @param fwType
	 * @param readNum
	 * @return
	 * @author qxliu 2019年11月10日 上午11:28:10
	 */
	public static Double getFamWeight(String puriBracketOrNot, dbName dbname, FW_TYPE fwType, long readNum){
		return getFamWeight(puriBracketOrNot, dbname, fwType, readNum, true);
	}
	/**
	 * 
	 * @param fwType
	 * @param readNum i.e. original L (M in paper), number of documents the user has read
	 * @return
	 * @author qxliu 2019年10月23日 下午12:11:07
	 */
	public static Double getFamWeight(String puriBracketOrNot, dbName dbname, FW_TYPE fwType, long readNum, Boolean byMin){
		String key = fwType.name()+"|"+readNum+"|"+puriBracketOrNot+"|"+byMin;
		Object obj = null;
		if((obj=propFamCache.retrieve(key))!=null){
			return (Double)obj;
		}else{
			String pText = PVCostSimple.getPropText(puriBracketOrNot.replaceFirst("<", "").replace(">", ""), dbname);//EntityInfo.getLabelFromDB(puri.replaceFirst("<", "").replace(">", "")).getFirst();//.getLabelForTid(tid).getSecond();//getPropText(tid);
//			System.out.println("ptext:\t"+pText);
			double famw = getVolumeProbOfString(pText, fwType, readNum, byMin);
//			System.out.println("famw:\t"+famw);
			propFamCache.store(key, famw);
			return famw;
		}
	}
	/**
	 * for old implementations
	 * @param vStr
	 * @param puriNoBracket
	 * @param dbname
	 * @param fwType
	 * @param readNum
	 * @param isVLabel
	 * @return
	 * @author qxliu 2019年11月10日 上午11:28:38
	 */
	public static Double getFamWeightForVal(String vStr, String puriNoBracket, dbName dbname, FW_TYPE fwType, long readNum, Boolean isVLabel){
		return getFamWeightForVal(vStr, puriNoBracket, dbname, fwType, readNum, isVLabel, true);
	}
	/**
	 * 
	 * @param puriBracketOrNot
	 * @param dbname
	 * @param fwType
	 * @param readNum
	 * @return
	 * @author qxliu 2019年11月10日 上午9:44:35
	 */
	public static Double getFamWeightForVal(String vStr, String puriNoBracket, dbName dbname, FW_TYPE fwType, long readNum, Boolean isVLabel, Boolean byMin){
		String key = fwType.name()+"|"+readNum+"|"+vStr;
		Object obj = null;
		if((obj=propFamCache.retrieve(key))!=null){
			return (Double)obj;
		}else if(isVLabel){
			double famw = getVolumeProbOfStringForVal(vStr, fwType, readNum, byMin);//====
//			System.out.println("famw:\t"+famw);
			propFamCache.store(key, famw);
			return famw;
		}else{
			String vText = PVCostSimple.getValueText(vStr.replaceFirst("<", "").replace(">", ""), puriNoBracket, dbname);//EntityInfo.getLabelFromDB(puri.replaceFirst("<", "").replace(">", "")).getFirst();//.getLabelForTid(tid).getSecond();//getPropText(tid);
//			System.out.println("ptext:\t"+pText);
			double famw = getVolumeProbOfStringForVal(vText, fwType, readNum, byMin);//====
//			System.out.println("famw:\t"+famw);
			propFamCache.store(key, famw);
			return famw;
		}
	}
	/**
	 * @param propText
	 * @param fwType
	 * @param readNum i.e. original L (M in paper), number of documents the user has read
	 * @return
	 * @author qxliu 2019年10月21日 下午4:18:20
	 */
	private static Double getVolumeProbOfString(String propText, FW_TYPE fwType, long readNum, Boolean byMin){//, long L){//, long K){
		if(byMin==null) byMin = true; //old default
		
		TwoTuple<Long, Long> volcountTuple = null;
		Long volcount = null;
		Double exp = null;
		ArrayList<String> words = splitText(propText, true);//=== 20191215 add true (previously is false)
//		System.out.println("words:"+words);
		if (words == null || words.size() == 0) {
			System.out.println("error! get no words! :" + propText);
			System.exit(-1);
		}
		switch(fwType){
		case onegram://use min/max book-count of one-grams as text count;
			volcountTuple = countVolumeOneGram(words);//, K);
			volcount = byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			break;
		case twogram://use min/max book-count of bi-grams as text count;
			volcountTuple = countVolumeTwoGram(words);
			volcount = byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			break;
		case onetwogram://use mean of min/max book-count of onegram and bi-tram;
			volcountTuple = countVolumeOneGram(words);//, K);
			volcount = byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			volcountTuple = countVolumeTwoGram(words);//, K);
			volcount += byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			
			volcount = (long)(volcount/2.0); //avg
			break;
		default:
			throw new IllegalArgumentException("wrong fwType! "+fwType);	
		}
		exp = expectationFunction(readNum, volcount);//, K);
		return exp;
	}
	/**
	 * modified
	 * @author qxliu 2019年11月10日 上午11:02:41
	 * @param propText
	 * @param fwType
	 * @param readNum
	 * @return
	 * @author qxliu 2019年11月10日 上午10:21:48
	 */
	private static Double getVolumeProbOfStringForVal(String valText, FW_TYPE fwType, long readNum, Boolean byMin){//, long L){//, long K){
		if(byMin==null) byMin = true; //old default
		
		TwoTuple<Long, Long> volcountTuple = null;
		Long volcount = null;
		Double exp = null;
		ArrayList<String> words = splitTextForVal(valText);//remain numbers
		if (words == null || words.size() == 0) {
			System.err.println("error! get no words! :" + valText);
//			System.exit(-1);
			return 0.0;
		}
		switch(fwType){
		case onegram:
			volcountTuple = countVolumeOneGram(words);//, K);
			volcount = byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			break;
		case twogram:
			volcountTuple = countVolumeTwoGram(words);
			volcount = byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			break;
		case onetwogram:
			volcountTuple = countVolumeOneGram(words);//, K);
			volcount = byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			volcountTuple = countVolumeTwoGram(words);//, K);
			volcount += byMin?volcountTuple.getFirst():volcountTuple.getSecond();
			
			volcount = (long)(volcount/2.0); //avg
			break;
		default:
			throw new IllegalArgumentException("wrong fwType! "+fwType);	
		}
		exp = expectationFunction(readNum, volcount);//, K);
		return exp;
	}
	/**
	 * for volume familiar
	 * @param words
	 * @param readNum i.e. original L (M in paper), number of documents the user has read
	 * @return
	 */
	private static TwoTuple<Long, Long> countVolumeOneGram(ArrayList<String> words){//long L){//, long K){
//		Double exp = null;
		long tmpCount = 0;
		long minCount = totalVolume;//.totalPage;
		boolean counted = false;

		long avgCountSum = 0;
		int avgCountNum = 0;
		for (String w : words) {
			 if(w.length() == 0 ){
				 continue;
			 }
			tmpCount = getVolumeCountOfOneGram(w);
			 avgCountSum+= tmpCount;
			 avgCountNum++;
			if (minCount > tmpCount) {
				minCount = tmpCount;
			}
			counted = true;
		}// end of for_w
		if(!counted){
			minCount = 1;
		}
		long avgCount = Math.round(avgCountSum/(double)avgCountNum);
		return new TwoTuple<Long, Long>(minCount, avgCount);
	}

	/**
	 * for volume familiar 
	 * @param words
	 * @param readNum i.e. original L (M in paper), number of documents the user has read
	 * @return <min, avg>
	 */
	private static TwoTuple<Long, Long> countVolumeTwoGram(List<String> words){
		long tmpCount = 0;
		long minCount = totalVolume;//.totalPage;
		boolean added = false;
		boolean counted = false;
		String biGram = null;
		
		long avgCountSum = 0;
		int avgCountNum = 0;
		if(words.size()==1){
			minCount = getVolumeCountOfOneGram(words.get(0));
			counted = true;
			
			avgCountSum=minCount;
			avgCountNum=1;
		}
		for (int i=0; i<words.size()-1; i++) {
			 if(words.get(i).length() == 0 ){
				 continue;
			 }
			 added = false;
			 biGram = words.get(i);
			 for(int j=i+1; j<words.size(); j++){
				 if(words.get(j).length()>0){
					 biGram += " "+words.get(j);
					 added = true;
					 break;
				 }
			 }
			 if(added){//has more than two words, use bigram
				tmpCount = getMaxVolumeCountOfBiGram(biGram);
			 }else{//actually only one word, use onegram
				tmpCount = getVolumeCountOfOneGram(biGram);
			 }
			 avgCountSum+= tmpCount;
			 avgCountNum++;
			if (minCount > tmpCount) {
				minCount = tmpCount;
			}
			counted = true;
		}// end of for_w
		if(!counted){
			minCount = 1;
		}
		long avgCount = Math.round(avgCountSum/(double)avgCountNum);
		return new TwoTuple<Long, Long>(minCount, avgCount);
		
	}
	private static long getVolumeCountOfOneGram(String word){
		long maxCount = 1;// never be zero;
		
		HashMap<String, Long> volumeCount = FamDB.getFormsAndVolumeCountOfOneGram(word, null);//new HashMap<String, Long>();
		
		for (long v : volumeCount.values()) {
			if (maxCount < v) {
				maxCount = v;
			}
		}
		return maxCount;
	}
	private static long getMaxVolumeCountOfBiGram(String bigram){
		long maxCount = 1;
//		ResultSet rs = null;
		
		HashMap<String, Long> volumeCount = FamDB.getFormsAndVolumeCountOfBiGram(bigram, null);//new HashMap<String, Long>();//different forms
		
		for (long v : volumeCount.values()) {
			if (maxCount<v) {
				maxCount=v;
			}
		}
		return maxCount;
	}
	public static ArrayList<String> splitText(String text, boolean rmBracket) {
		String[] splits = null;
		text = text.replace("_", " ");//add 20191112
		//==== add 20191215
		if(rmBracket){
			StringBuffer newText = new StringBuffer();
			boolean bracketStart = false;
			for(int i=0; i<text.length(); i++){
				char chari = text.charAt(i);
				if(chari=='('){
					bracketStart=true;
				}else if(chari==')'){
					bracketStart=false;
				}else{
					if(bracketStart) {
						continue;
					}else {
						newText.append(chari);
					}
				}
				
			}
			text = newText.toString();
		}
		//=================
		
		splits = text.split("\\W");
		ArrayList<String> words = new ArrayList<String>();
		for (String s : splits) {
			if (s.length() == 0 || s.matches("\\d+")) {// remove seperate
				// digital words
				continue;
			} else {
				words.add(s);
			}
		}
		return words;
	}
	/**
	 * 
	 * @param text
	 * @return
	 * @author qxliu 2019年11月10日 上午10:20:26
	 */
	private static ArrayList<String> splitTextForVal(String text) {
		String[] splits = null;
		splits = text.split(" ");
//		}
		ArrayList<String> words = new ArrayList<String>();
		for (String s : splits) {
			if (s.length() == 0) {// remove seperate, remain numbers
				// digital words
				continue;
			} else {
				s = s.replace("(", "").replace(")","").replaceAll("\\\"", "");
				words.add(s);
			}
		}
		
		return words;
	}
	
	private static double expectationFunction(long L, long n){
		long max = Math.min(L, n);
		long min = 0;
		double exp = 0;
		double factor = 0;
		long N=totalVolume;//B
		double p=0;
		double fenzi = Gamma.logGamma(n+2)+Gamma.logGamma(N-n+1)+Gamma.logGamma(L+1)+Gamma.logGamma(N-L+2)-Gamma.logGamma(N+2);
		for(long k=min; k<=max; k++){
			factor = Gamma.logGamma(k+1)+Gamma.logGamma(n-k+2)+Gamma.logGamma(L-k+1)+Gamma.logGamma(N-n-L+k+1);
			factor = fenzi-factor;
			exp += Math.pow(Math.E, factor)*familiarityFunction(k, N);//K);
		}
		return exp;
	}
	private static double familiarityFunction(long k, long N){
		if(N<=0){
			System.out.println("error! N="+N);
			return 0;
		}
		return Math.log(k+1)/Math.log(N+1);
	}
}
