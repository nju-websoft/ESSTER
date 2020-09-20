/**
 * 
 */
//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

//import cn.edu.nju.ws.es.db.DBConnector;

/**
 * @author qxliu 2019 2019年10月21日 下午4:09:14
 *
 */
public class FamDB {
	
	private static Map<String, HashMap<String, Long>> cacheWordNGram = new HashMap<>();
	
	/**
	 * called by Familarity.getVolumeCountOfOneGram
	 * @param onegram
	 * @param restrictions
	 * @return
	 * @author Qingxia Liu before 2015-11-6 上午11:54:53
	 */
	public static HashMap<String, Long> getFormsAndVolumeCountOfOneGram(String onegram, String restrictions){
		HashMap<String, Long> volumeCount = new HashMap<String, Long>();//different forms
		Object obj = null;
//		if((obj=cacheWordNGram.retrieve(onegram)) != null){
		if((obj=cacheWordNGram.get(onegram)) != null){
			volumeCount = (HashMap<String, Long>)obj;
			return volumeCount;
		}
		Connection ngconn;
		try {
			ngconn = DBConnector.getNGramConnection();
			String sql = null;
			if(restrictions==null || restrictions.trim().length()==0){//usually used
				sql = "SELECT ngram,volume_count FROM ngram WHERE ngram=?";
			}else{
				sql = "SELECT ngram,volume_count FROM ngram WHERE ngram=? AND "+restrictions;//E.G.  "SELECT ngram,volume_count FROM ngram WHERE ngram=? AND year>=2000"
			}
			PreparedStatement pgetcount = ngconn.prepareStatement(sql);
			pgetcount.setString(1, onegram);
			ResultSet rs = pgetcount.executeQuery();
			String ngram;
			Long tmpCount = null;
			while(rs.next()){
				ngram = rs.getString(1);
				if ((tmpCount = volumeCount.get(ngram)) == null) {
					volumeCount.put(ngram, rs.getLong(2));
//					System.out.println("putting..."+rs.getLong(2));
				} else {
					volumeCount.put(ngram, tmpCount + rs.getLong(2));
//					System.out.println("adding..."+rs.getLong(2));
				}
			}
			rs.close();
			pgetcount.close();
			ngconn.close();
			if(volumeCount != null){
//				cacheWordNGram.store(onegram, volumeCount);
				cacheWordNGram.put(onegram, volumeCount);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return volumeCount;
	}

	/**
	 * call by Familiarity.getMaxVolumeCountOfBiGram(bigram)
	 * @param bigram
	 * @param restrictions
	 * @return
	 */
	public static HashMap<String, Long> getFormsAndVolumeCountOfBiGram(String bigram, String restrictions){
		HashMap<String, Long> volumeCount = new HashMap<String, Long>();//different forms
		Connection ngconn;
		try {
			ngconn = DBConnector.getNGramConnection();
			String sql = null;
			if(restrictions==null || restrictions.trim().length()==0){//usually used
				sql = "SELECT ngram,volume_count FROM ngram2 WHERE ngram=?";
			}else{
				sql = "SELECT ngram,volume_count FROM ngram2 WHERE ngram=? AND "+restrictions;//E.G.  "SELECT ngram,volume_count FROM ngram2 WHERE ngram=? AND year>=2000"
			}
			PreparedStatement pgetcount = ngconn.prepareStatement(sql);
			pgetcount.setString(1, bigram);
			ResultSet rs = pgetcount.executeQuery();
			String ngram;
			Long tmpCount = null;
			while(rs.next()){
				ngram = rs.getString(1);
				if ((tmpCount = volumeCount.get(ngram)) == null) {
					volumeCount.put(ngram, rs.getLong(2));
//					System.out.println("putting..."+rs.getLong(2));
				} else {
					volumeCount.put(ngram, tmpCount + rs.getLong(2));
//					System.out.println("adding..."+rs.getLong(2));
				}
			}
			rs.close();
			pgetcount.close();
			ngconn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return volumeCount;
	}
	
}
