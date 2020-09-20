//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

/**
 * syso: blb:, lb:
 * state-of-the-art heuristic algorithm of
 * QAKP(quadratic knapsack problem) 
 * @author 
 *
 */
public class GRASP {
	ArrayList<Integer> Q_Sum;
	ArrayList<Integer> Set;
	//ArrayList<Integer> SetPrecise;
	ArrayList<Integer> r;
	double[][] profit;
	int[] weight;
	int scale;
	int constraint;
	Random rand;
	public GRASP(int scale, int constraint, double[][] profit, int[] weight){
		this.scale = scale;
		this.constraint = constraint;
		rand = new Random();
		Q_Sum = new ArrayList<Integer>();
		this.profit = new double[scale][scale];//my: 初始化profit
		for(int i = 0; i < scale; i++){
			Q_Sum.add(0);
			for(int j = 0; j< scale; j++){
				this.profit[i][j] = profit[i][j];
			}
		}
		r = new ArrayList<Integer>();
		this.weight = new int[scale];
		System.arraycopy(weight, 0, this.weight, 0, scale);
	}
	
	public double f_greedy(ArrayList<Integer> Set, int index, int currentweight, double objS){
		double numerator = objS, denominator = currentweight;
		for(int i = 0; i< Set.size(); i++){
			numerator = numerator + profit[Set.get(i)][index];
		}
		numerator = numerator + profit[index][index];
		denominator = denominator + weight[index];
		return numerator/denominator;
	}
	
	public int min(int MinLen, int MaxLen, int left){
		int temp = rand.nextInt(MaxLen-MinLen+1) + MinLen;
		if(temp <= left)
			return temp;
		else
			return left;
	}
	
	public int CaculateQ_kl(TreeMap<Double,Integer> RCL, int k, ArrayList<Integer>Qk){
		Iterator<Double> iterator = RCL.keySet().iterator();
		int sum = 0;
		while(iterator.hasNext()){
			double key = iterator.next();
			sum = sum + (k -Qk.get(RCL.get(key)));
		}
		return sum;
	}
	
	public ArrayList<Integer> CaculateRS(ArrayList<Integer> Set){
		ArrayList<Integer> R_S = new ArrayList<Integer>();
		int current_weight = 0;
		for(int i = 0; i < Set.size(); i++){
			current_weight = current_weight + weight[Set.get(i)];
		}
		for(int i = 0; i < scale; i++){
			if(!Set.contains(i)){
				if(weight[i] + current_weight <= constraint ){
					R_S.add(i);
				}
			}
		}
		return R_S;
	}
	public void Construction(int MinLen, int MaxLen, ArrayList<Integer> IndexSet, int k){
		int l = 1;
		int currentweight = 0;
		double objS = 0.0;
		Set = new ArrayList<Integer>(IndexSet);

		ArrayList<Integer> R_S = new ArrayList<Integer>(CaculateRS(IndexSet));
		int iteration = 0;
		while(R_S.size() > 0){
			iteration ++;
			TreeMap<Double,Integer> RCL = new TreeMap<Double,Integer>();
			TreeMap<Double,Double> q_kl = new TreeMap<Double,Double>();
			for(int i = 0; i< R_S.size(); i++){
				RCL.put(f_greedy(Set,R_S.get(i),currentweight,objS), R_S.get(i));
			}
			int len = min(MinLen, MaxLen,R_S.size());
			int RCL_len = RCL.size();
			for(int i = 0; i< RCL_len-len; i++){
				RCL.remove(RCL.firstKey());
			}
			int Q_kl = CaculateQ_kl(RCL,k,Q_Sum);
			Iterator<Double> iterator = RCL.keySet().iterator();
			while(iterator.hasNext()){
				double key = iterator.next();
				double value = (k-Q_Sum.get(RCL.get(key)))/(double)Q_kl;
				q_kl.put(key, value);
			}
			int m = rand.nextInt(Q_kl) + 1;
			double temp_sum = 0,compare = (double)m/Q_kl;
			iterator = RCL.keySet().iterator();
			double key = 0;
			while(iterator.hasNext() && temp_sum < compare){
				key = iterator.next();
				temp_sum = temp_sum + q_kl.get(key);
			}
			
			int index = 0;
			if(k == 1 && isSame(RCL) == true && iteration == 0){
				index = Max(profit,weight,constraint);
			}
			else{
				index = RCL.get(key);
			}
			Set.add(index);
			
			currentweight = currentweight + weight[index];
			for(int i = 0; i < Set.size(); i++){
				objS = objS + profit[Set.get(i)][index];
			}
			objS = objS + profit[index][index];
			R_S = CaculateRS(Set);
			l = l + 1;
		}
	}
	
	
	public void LocalSearch(){
		boolean Terminate = false;
		ArrayList<Integer> R_S = new ArrayList<Integer>(CaculateRS(Set));
		while(!Terminate){
			double Addition = 0;
			double HighestAddition = 0;
			int AddIndex = 0;
			int ReplaceIndexS = 0;
			int ReplaceIndexRS = 0;
			for(int i = 0; i < R_S.size(); i++){
				for(int j = 0; j < Set.size(); j++){
					Addition = Addition + profit[Set.get(j)][R_S.get(i)];
				}
				Addition = Addition + profit[R_S.get(i)][R_S.get(i)];
				if(Addition > HighestAddition){
					HighestAddition = Addition;
					AddIndex = R_S.get(i);
				}
				else{
					Addition = 0;
				}
			}
			for(int i = 0; i < Set.size(); i++){
				int index = Set.get(i);
				ArrayList<Integer> SetTest = new ArrayList<Integer>(Set);
				SetTest.remove(i);
				ArrayList<Integer> SetTemp = new ArrayList<Integer>(SetTest);
				ArrayList<Integer> RSTemp = new ArrayList<Integer>(CaculateRS(SetTemp));
				Addition = 0 - profit[index][index];
				for(int k = 0; k < SetTemp.size(); k++){
					Addition = Addition - profit[index][SetTemp.get(k)];
				}
				double Additiontemp = Addition;
				for(int k = 0; k < RSTemp.size(); k++){
					Addition = Additiontemp;
					for(int j = 0; j < SetTemp.size(); j++){
						Addition = Addition + profit[SetTemp.get(j)][RSTemp.get(k)];
					}
					Addition = Addition + profit[RSTemp.get(k)][RSTemp.get(k)];
					if(Addition - HighestAddition > 0.00001){ 
						HighestAddition = Addition;
						ReplaceIndexS = index;
						ReplaceIndexRS = RSTemp.get(k);
					}
				}
			}
			if(HighestAddition > 0.0000000001){
				if(ReplaceIndexS == 0 && ReplaceIndexRS == 0){
					Set.add(AddIndex);
				}
				else{
					Set.remove((Integer)ReplaceIndexS);
					Set.add(ReplaceIndexRS);
				}
			}
			else{
				Terminate = true;
			}
		}
	}
	
	public double CaculateLB(){
		double lb = 0;
		for(int i = 0; i< Set.size(); i++){
			for(int j = 0; j < Set.size(); j++){
				if(Set.get(i) >= Set.get(j)){
					lb += profit[Set.get(i)][Set.get(j)];
				}
			}
		}
		return lb;
	}
	
	public void UpdateQSum(){
		for(int i = 0; i< Set.size(); i++){
			int current = Q_Sum.get(Set.get(i));
			Q_Sum.set(Set.get(i), current+1);
		}
	}
	
	public boolean isZero(ArrayList<Integer> QSum){
		for(int i = 0; i< QSum.size(); i++){
			if(QSum.get(i) != 0)
				return false;
		}
		return true;
	}
	
	public int Max(double[][] profit,int[] weight,int constraint){
		double[] middle = new double[weight.length];
		for(int i = 0; i< profit.length; i++){
			middle[i] = 0;
			for(int j = 0; j < profit[0].length; j++){
				middle[i] += profit[i][j];
			}
		}
		double max = middle[0];
		int index = 0;
		for(int i = 1; i< middle.length; i++){
			if(middle[i] > max){
				max = middle[i];
				index = i;
			}
		}
		return index;
	}
	public boolean isSame(TreeMap<Double,Integer> RCLL){
		Iterator<Double> it = RCLL.keySet().iterator();
		double compare = it.next();
		while(it.hasNext()){
			double current = it.next();
			if(Math.abs(current-compare) > 0.0000001)
				return false;
		}
		return true;
	}

	/**
	 *
	 * @param gamma MinLen
	 * @param beta MaxLen=gamma+beta
	 * @param lambda count
	 * @param sigma
	 * @param LBmain
	 * @return
	 */
	public ArrayList<Integer> ProcessGRASP(int gamma,int beta,int lambda,int sigma,ArrayList<Double> LBmain){
		ArrayList<Integer> Index = new ArrayList<Integer>();
		int MinLen = gamma, MaxLen = gamma+beta, count = 0, m = 0;
		double BestLB = 0, LB = Integer.MIN_VALUE;
		ArrayList<Integer> TempSet = new ArrayList<Integer>();
		ArrayList<Integer> TempQ_Sum = new ArrayList<Integer>();
		while(count != lambda){
			int k = 0;
			for(k = m*sigma+1; k <= (m+1)*sigma; k++){
				if(k == m*sigma+1){
					TempQ_Sum.clear();
					for(int i = 0; i < Q_Sum.size(); i++)
						TempQ_Sum.add(Q_Sum.get(i));
				}
				Construction(MinLen, MaxLen, TempSet, k);
				LocalSearch();
				BestLB = CaculateLB();
//				System.out.println("blb:\t"+BestLB);
				UpdateQSum();
			}
			if(BestLB > LB){
				LB = BestLB;
				Index.clear();
				for(int x = 0; x < Set.size(); x++){
					Index.add(Set.get(x));
				}
				count = 0;
			}
			else{
				count = count + 1;
				MinLen = MaxLen + 1;
				MaxLen = MaxLen + beta;
			}
			TempSet.clear();
			for(int j = 0; j < scale; j++){
				if(TempQ_Sum.get(j) == m*sigma && m > 0){
					TempSet.add(j);
				}
			}
			m = m + 1;
		}
		//System.out.println(LB);
		LBmain.add(LB);
		return Index;
	}
	
	public ArrayList<Integer> ProcessGRASPmain(int gamma,int beta,int lambda,int sigma){
		ArrayList<ArrayList<Integer>> Index = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> Indextemp = new ArrayList<Integer>();
		ArrayList<Double> LB = new ArrayList<Double>();
		for(int i = 0; i< 5; i++){
			r = new ArrayList<Integer>();
			Q_Sum = new ArrayList<Integer>();
			for(int j = 0; j < scale; j++){
				Q_Sum.add(0);
			}
			Index.add(ProcessGRASP(gamma,beta,lambda,sigma,LB));
		}
		double tempLB = Integer.MIN_VALUE;
		for(int i = 0; i< LB.size(); i++){
//			System.out.println("lb:"+LB.get(i));
			if(tempLB < LB.get(i)){
				tempLB = LB.get(i);
				Indextemp = Index.get(i);
			}
		}
		return Indextemp;
	}
}
