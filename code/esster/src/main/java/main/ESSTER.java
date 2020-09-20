package main.java.main; /**
 * 
 */
//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;



//import cn.edu.nju.ws.es.web.EntityInfo;
//import cn.edu.nju.ws.es.web.User.TOPK;
//import cn.edu.nju.ws.es.web.algo.cd.mysql.ClassHirarchy;
//import cn.edu.nju.ws.es.web.algo.cd.mysql.PropertyHirarchy;

import main.java.util.Constants;
import main.java.util.Constants.dbName;
import main.java.util.Constants.TOPK;
import main.java.util.Familarity;
import main.java.util.Familarity.FW_TYPE;
import main.java.util.FeatureManager;
import main.java.util.FeatureNode;
import main.java.util.GRASP;
import main.java.util.ISub;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.Constants;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.Constants.dbName;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.Familarity;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.Familarity.FW_TYPE;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.FeatureManager;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.FeatureNode;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.GRASP;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.ISub;
import qxliu.tools.tuples.ThreeTuple;

/**
 * @author qxliu 2020 2020-7-12 9:20:56
 *
 */
public class ESSTER {
	//==== params
	Double factor_info = null;//gamma, factor_info
	Double factor_inner = null;//delta, factor_inner, weight for redundancy, 1-gamma
	
	//==== for Combiner_jws
	private Integer K_fNumLimit = null;
//	private Integer tripleNum = null;
	protected dbName dname = null;// name of database
	
	//========= params for T(Readability)
	boolean isFamWeighted = false;
	FW_TYPE fwType = FW_TYPE.twogram; //default setting
	Integer readNum = 40; //default setting
	Double fmFactor = null;

	//========= param for wether use IMP (i.e. S, for structure)
	boolean isIMP = true;

	//======== params for featureCombine (i.e. pweight) (detailed strategy of structure)
	String fnameStr = null;
	String propfnameStr = null;//initially pFreqInE
	Double alpha = null;
	Integer combineType = -1;

	//===== for cache
	public boolean useCache = false;
	public static boolean useMoreHierarchy = true;//default true, for R

	public static void main(String[] args) {
		System.out.println("abc");
//		int eid=118;
//		int K = 5;
//		main.java.main.ESSTER algo = new main.java.main.ESSTER();
//		algo.setFeatureCombine("vFreqInT_norm", 0.5, 0);
//		algo.setMainParam(1.0);
//		algo.setParamForU(FW_TYPE.twogram, 40, 0.01);
//		List<Integer> summ1 = algo.getSummResult(eid, K);
//		System.out.println("summ1:"+summ1);
	}
	/**
	 * 
	 * @param fwType
	 * @param readNum
	 * @author qxliu 2019年10月24日 下午8:34:04
	 */
	public ESSTER setParamForU(Familarity.FW_TYPE fwType, Integer readNum, Double fmFactor){
		this.isFamWeighted = true;
		this.fwType = fwType==null ? this.fwType: fwType;
		this.readNum = readNum==null? this.readNum: readNum;
		this.fmFactor = fmFactor==null?0.01:fmFactor;
		useMoreHierarchy = true;
		return this;
	}

	public ESSTER clearParamForU(){
		this.isFamWeighted = false;
		this.fwType = null;
		this.readNum = null;
		useMoreHierarchy = true;
		return this;
	}
	
	public ESSTER setIsIMP(boolean isIMP){
		this.isIMP = isIMP;
		return this;
	}
	public boolean getIsIMP(){
		return this.isIMP;
	}
	public String getParams(){
		return "useMoreHierarchy="+useMoreHierarchy
				+", factor_info(gamma)="+factor_info+", factor_inner="+factor_inner
				+", isFamWeighted="+isFamWeighted+", fwType="+fwType+", readNum="+readNum
				+", alpha="+alpha//+", fnameStr="+this.fnameStr
				+", isComb="+isFComb()+", pweightType="+combineType;
	}
	/**
	 * 
	 * @param fnameStr
	 * @param alpha
	 * @return
	 * @author qxliu 2019年11月4日 上午10:06:24
	 */
	public ESSTER setFeatureCombine(String fnameStr, double alpha, int combineType){
		this.fnameStr = fnameStr;
//		this.structName = "FComb_"+this.fnameStr;
		this.alpha = alpha;
		this.combineType = combineType;
		return this;
	}
	/**
	 * 
	 * @return
	 * @author qxliu 2019年11月4日 上午10:06:31
	 */
	private boolean isFComb(){
		if(this.fnameStr==null){
			return false;
		}else{
			return true;
		}
	}

	/**
	 * 
	 * @param param
	 * @return
	 * @author qingxialiu Jan 13, 2017 11:07:13 AM
	 */
//	@Override
	public ESSTER setMainParam(double param){
		if(param>1.0||param<0){
			throw new IllegalArgumentException("Main param should be in [0,1], param="+param);
		}
		setParamGama(param); // weight for imp+fw
		setParamDelta(1-param); //weight for redundancy
		return this;
	}

	public Double getMainParam(){
		return this.factor_info;
	}
	

	/**
	 * importance of characteristic, default=8
	 * @param gama
	 * @return
	 * @author qingxialiu Jan 10, 2017 3:11:21 PM
	 */
	private ESSTER setParamGama(Double gama){
		factor_info = gama;
		return this;
	}
	/**
	 * repaired a bug 
	 * @author qxliu Mar 8, 2019 9:34:59 PM
	 * importance of diverse, default=1
	 * @param delta
	 * @return
	 * @author qingxialiu Jan 10, 2017 3:11:43 PM
	 */
	private ESSTER setParamDelta(Double delta){
		factor_inner = delta;
		return this;
	}
	
	
	/**
	 * @param eid
	 * @param K
	 * @return
	 * @author qxliu Mar 8, 2019 9:10:16 PM
	 */
	protected List<Integer> getSummResult(int eid, Integer K) {
		if(factor_info==null || factor_inner==null){
			throw new IllegalArgumentException("please setMainParam() first! gama="+factor_info+", delta="+factor_inner);
		}


		TOPK topK = TOPK.valueOf("top"+K);
		
		ArrayList<FeatureNode>  fList = initFeatureList_withCombine2(eid, topK);

		int fnum = fList.size();
		double[][] profit = null;
		int[] weight = null;
		profit = new double[fnum][fnum];
		weight = new int[fnum];
		initPW_fast(profit, weight, fList, this.factor_info, this.factor_inner);

		this.K_fNumLimit = (K==null)?fnum:K; //=====
//		this.tripleNum = fnum;
		//solve QKP using GRASP algorithm
		GRASP grasps = new GRASP(weight.length, this.K_fNumLimit, profit, weight);
		List<Integer> Index = grasps.ProcessGRASPmain(1, 1, 1, 1);
		List<Integer> summary = new ArrayList<Integer>();
		//collect selected features
		for(int i=0; i<Index.size(); i++){
			int index = Index.get(i);
			int fid = fList.get(index).getId();
			summary.add(fid);
		}
		return summary;
	}
	

	/**
	 * 
	 * @param eid
	 * @param topK
	 * @return
	 * @author qxliu 2020年8月22日 上午10:10:37
	 */
	protected ArrayList<FeatureNode> initFeatureList_withCombine2(int eid, TOPK topK){
		ArrayList<FeatureNode> fList = new ArrayList<FeatureNode>();
		
		EntityInfo e = EntityInfo.getInstance(eid);
		ArrayList<Integer> tidList = e.getTripleIds();
		
		
		Map<Integer, Double> vfreqMap = getFeatureScore("vFreqInT", eid, topK);//normed
		Map<Integer, Double> pweightMap_local = getFeatureScore("pFreqInE", eid, topK);//======<tid, feat>
		Map<Integer, Double> pweightMap_global = getFeatureScore("pFreqInD", eid, topK);

		int TNum = Constants.tnum.get(dname);
		int SNum = Constants.snum.get(dname);
		int tnum = tidList.size();
		
		for(int i=0; i<tidList.size(); i++){
			int tid = tidList.get(i);
			ThreeTuple<String, String, String> triple = e.getTripleUrisBracked().get(i);
			String suri = EntityInfo.getUriUnBracked(triple.getFirst());
			String puri = EntityInfo.getUriUnBracked(triple.getSecond());
			String ouri = EntityInfo.getUriUnBracked(triple.getThird());
			ThreeTuple<String, String, String> labels = e.getTripleLabels().get(i);
			String slabel = labels.getFirst();
			String plabel = labels.getSecond();
			String olabel = labels.getThird();
			boolean inverse = !e.isForward(tid);
			FeatureNode fn = new FeatureNode(tid, suri, puri, ouri, slabel, plabel, olabel, inverse);//, line);
			Double fw = 0.0;
			if(isFamWeighted){// if _T
				fw = Familarity.getFamWeight(puri, dname, fwType, readNum);
				fw = Math.log10(fw+1);
			}
			double comb = 0;
			if(isIMP){// whether to use importance score
				double vpop = getPopularByFreq(vfreqMap.get(tid)+1, TNum+1);//different with 2
				double ppop_globalD = getPopularByFreq(pweightMap_global.get(tid)+1, SNum+1);//different with 2
				double ppop_local = getPopularByFreq(pweightMap_local.get(tid)+1, tnum+1);
				comb = alpha * ppop_globalD * (1-vpop) + (1-alpha) * (1-ppop_local)*vpop;
			}
			comb += fw; 
			fn.setSelfChab(comb);
			fList.add(fn);
		}
		return fList;
	}
	
	/**
	 * replaced by initPW_cofigProfit()
	 * @param profit
	 * @param weight
	 * @param feature_list
	 * @param gamma
	 * @param delta
	 * @author qxliu Mar 16, 2019 3:25:58 PM
	 */
	public void initPW_fast(double[][] profit, int[] weight
			, ArrayList<FeatureNode> feature_list
			, Double gamma
			, Double delta
			){{
//				
		for(int i = 0; i < feature_list.size(); i++){
			weight[i] = 1;

			profit[i][i] = gamma*feature_list.get(i).getSelfChab();
			if(Double.isInfinite(profit[i][i])){
				throw new IllegalArgumentException("profit is inifinte for ("+i+", "+i+"), selfi="+feature_list.get(i).getSelfChab());
			}
			
			for(int j = i+1; j< feature_list.size(); j++){
				double effectiveness = 0;//info overlap
				if(delta>0){//20191106: else no need to compute overlap when not use _R, to save time
					String piLabel = feature_list.get(i).getPLabel();
					String pjLabel = feature_list.get(j).getPLabel();
					double ppair = Similarity(piLabel, pjLabel, 0);
					String viLabel = feature_list.get(i).getInv()?feature_list.get(i).getSLabel():feature_list.get(i).getOLabel();
					String vjLabel = feature_list.get(j).getInv()?feature_list.get(j).getSLabel():feature_list.get(j).getOLabel();
					double vpair = Similarity(viLabel, vjLabel, 0);
					effectiveness = vpair>ppair?vpair:ppair;
					effectiveness = (effectiveness <= 0)?0:0-effectiveness;
					if(viLabel.equals(vjLabel)){
						if(PropertyHirarchy.isHirarchy(feature_list.get(i).getPro(), feature_list.get(j).getPro())){
							effectiveness = -1;
						}
					}
					if(piLabel.equals("type")&&pjLabel.equals("type")){
						if(ClassHirarchy.isHirarchy(feature_list.get(i).getObj(), feature_list.get(j).getObj())){
							effectiveness = -1;
						}
					}
//				}else{
//					System.out.println("not use _R");
				}
				profit[i][j] = delta*effectiveness;
				profit[j][i] = delta*effectiveness;
			}//end for_j
			}
		}
	}

	/**
	 * coppied from ValuePair.Similarity()
	 * @param str1
	 * @param str2
	 * @param casenum
	 * @return
	 * @author qxliu Mar 15, 2019 6:25:06 PM
	 */
	public static double Similarity(String str1, String str2,int casenum){
		//直接计算两个属性的相似度
		double similarity = -1;
		if(NumberUtils.isNumber(str1) && NumberUtils.isNumber(str2)){
			double d1 = Double.parseDouble(str1);
			double d2 = Double.parseDouble(str2);
			if(d1 == 0 || d2 == 0)
				similarity = 0;
			else{
				if(d1/d2 < 0)// case 2
					similarity = -1;
				else{ // case 3, case 1
					if(d1/d2 <= 1)
						similarity = d1/d2;
					else{
						similarity = d2/d1;
					}
				}
			}
		}
		else{
			similarity = ISub.getSimilarity(str1, str2);//[-1,1]
		}
		return similarity;
	}
	
	/**
	 * 
	 * @param fnameStr
	 * @param eid
	 * @param topK
	 * @return
	 * @author qxliu 2019年11月1日 下午5:32:03
	 */
	public static Map<Integer, Double> getFeatureScore(String fnameStr, int eid, TOPK topK){
//		System.out.println("scores for feature:\t"+fnameStr);
		List<Integer> tidList = EntityInfo.getInstance(eid).getTripleIds();
//		this.tripleNum = tidList.size();
		return FeatureManager.getFeature(fnameStr, tidList);
	}
	/**
	 * smooth (add 1) to avoid log(1) when freq=1 for computing pop
	 * @param freq
	 * @param maxFreq
	 * @return
	 * @author qxliu 2019年12月14日 下午6:06:28
	 */
	public static double getPopularByFreq(double freq, int maxFreq){
		return Math.log(freq+1)/Math.log(maxFreq+1);//=== 20191214 add: to change to smooth inside
	}
}
