//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.whirlycott.cache.Cache;
import com.whirlycott.cache.CacheConfiguration;
import com.whirlycott.cache.CacheException;
import com.whirlycott.cache.CacheManager;


public class Constants {

	private static String backend = "com.whirlycott.cache.impl.ConcurrentHashMapImpl";
	private static String policy = "com.whirlycott.cache.policy.LFUMaintenancePolicy";
	private static int tunerSleepTime = 36000000;
	private static int maxSize = 10000;//1000;//10000;
	public static HashMap<dbName, Integer> snum = new HashMap<dbName, Integer>(); //num of entities
	public static HashMap<dbName, Integer> tnum = new HashMap<dbName, Integer>(); //num of triples
	
	public static enum dbName {dbpedia, lmdb, dsfaces, all}; // moved from Combiner.java and ensec2016.ESelector.java

    public static enum TOPK{
        top5(5), top10(10);
        private int idx; //feature index in the feature vector from FeatureManager.getFeatureStaticAll(tid)
        private TOPK(int idx){
            this.idx = idx;
        }
        public int getIdx(){
            return idx;
        }
    };

	static{
		snum.put(dbName.dbpedia, 13655588+1);//select count(distinct ?s) where {?s ?p ?o. filter regex(?s, '^http://dbpedia.org/resource/')}
		snum.put(dbName.lmdb, 694400+1);//select count(distinct ?s) where {?s ?p ?o. filter regex(?s, "^http://data.linkedmdb.org/resource/")} 


		tnum.put(dbName.dbpedia, 163572681);//select count(*) from <summeval.dbpedia2015-10.org> where {?s ?p ?o} 
		tnum.put(dbName.lmdb, 6147996);//select count(*) from <summeval.LinkedMDB2012-0210.org> where {?s ?p ?o}
		
	}
	
	
	public static Cache initCache(String name) {
		Cache cache = null;
		try {
			if (name == null) {
				cache = CacheManager.getInstance().getCache();
			}else{
				String cacheNames[] = CacheManager.getInstance().getCacheNames();
				List cacheNameList = Arrays.asList(cacheNames);
				if(cacheNameList.contains(name)){
					cache = CacheManager.getInstance().getCache(name);
				}else{
					CacheConfiguration cacheConfig = new CacheConfiguration();
					cacheConfig.setName(name);
					cacheConfig.setBackend(backend);
					cacheConfig.setPolicy(policy);
					cacheConfig.setTunerSleepTime(tunerSleepTime);
					cacheConfig.setMaxSize(maxSize);
					cache = CacheManager.getInstance().createCache(cacheConfig);
				}
			}
		} catch (CacheException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cache;
	}
	public static synchronized void writeLog(String filepath, String content){
		//写到文件
		File out = new File(filepath);
		try {
			FileWriter fw = new FileWriter(out,true);
			fw.write(System.currentTimeMillis()+": "+content+"\r\n");
			fw.flush();
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 
	 * @param value
	 * @param floatNum > 0
	 * @return
	 * @author Qingxia Liu 2015-5-3 上午11:50:33
	 */
	public static double getRound(double value, int floatNum){
		if(floatNum<=0){
			throw new IllegalArgumentException("floatNum must > 0, floatNum="+floatNum);
		}
		return (long)(value * Math.pow(10d, floatNum) + 0.5d) / Math.pow(10d, floatNum);
	}
}
