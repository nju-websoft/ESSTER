/**
 * 
 */
//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.whirlycott.cache.Cache;

import java.util.HashMap;
import java.util.List;

import cn.edu.nju.ws.es.db.DBConnector;
//import cn.edu.nju.ws.es.web.algo.util.Constants;

/**
 * @author qxliu 2019 2019年8月25日 下午2:20:32
 *
 */
public class FeatureManager {
	public static Cache tFeatCache = Constants.initCache("tFeatCache");

	/**
	 * 
	 * @param fnameStr
	 * @param tidList
	 * @return
	 * @author qxliu 2019年11月1日 下午5:30:16
	 */
	public static Map<Integer, Double> getFeature(String fnameStr, List<Integer> tidList){
		String key = fnameStr+"|"+tidList;
		Object obj = tFeatCache.retrieve(key);
		if(obj!=null) return (Map<Integer, Double>)obj;
		
		
		Map<Integer, Double> tFeatureMap = new HashMap<>();
		try {
			Connection conn = DBConnector.getDsSumEval2016Connection();
			
			PreparedStatement pstmt = conn.prepareStatement("select tid, "+fnameStr
					+" from jwsall_triple_mlfeatures where tid in "+tidList.toString().replace("[", "(").replace("]", ")"));
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				int tid = rs.getInt(1);
				double feature = rs.getDouble(2);
				tFeatureMap.put(tid, feature);
			}
			rs.close();
			pstmt.close();
			conn.close();
			if(tFeatureMap!=null){
				tFeatCache.store(key, tFeatureMap);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return tFeatureMap;
	}
}
