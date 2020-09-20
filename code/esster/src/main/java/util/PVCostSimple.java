/**
 * 
 */
//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.whirlycott.cache.Cache;

import cn.edu.nju.ws.es.db.DBConnector;
//import cn.edu.nju.ws.es.db.ensec2016.utils.ClassList;
//import cn.edu.nju.ws.es.db.ensec2016.utils.URIHelper;
//import cn.edu.nju.ws.es.utils.StringUtil;
//import cn.edu.nju.ws.es.web.algo.util.Constants;
//import cn.edu.nju.ws.es.web.algo.util.Constants.dbName;
//import cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util.Constants.dbName;
import main.java.util.Constants.dbName;
/**
 * @author qxliu 2019 2019年10月21日 下午4:53:18
 *
 */
public class PVCostSimple {
	public static Cache cacheCost = Constants.initCache("cost");
	public static Cache cacheName = Constants.initCache("name");
	public static Set<String> uriPropSet;
	static{
		
		uriPropSet = new HashSet<String>();
		//dbp
		uriPropSet.add("http://xmlns.com/foaf/0.1/homepage");
		uriPropSet.add("http://xmlns.com/foaf/0.1/depiction");
		uriPropSet.add("http://dbpedia.org/ontology/thumbnail");
		
		//lmdb
		uriPropSet.add("http://xmlns.com/foaf/0.1/page");
		uriPropSet.add("http://xmlns.com/foaf/0.1/based_near");////lmdb, based_near: value: http://sws.geonames.org/130758/
		uriPropSet.add("http://dbpedia.org/property/hasPhotoCollection");//value: http://www4.wiwiss.fu-berlin.de/flickrwrappr/photos/Are_We_Done_Yet?
		
	}
	public static int getCostByCh(String pvText){
		int cost = pvText.length();
		System.out.println("pv:"+pvText+"\t"+cost);
		return cost;
	}
	public static int getCostByWord(String pvText){
		int cost = pvText.trim().split(" ").length;
		System.out.println("pv:"+pvText+"\t"+cost);
		return cost;
	}
	public static int getCostByTriple(int tid){
		int cost = 1;
		return cost;
	}
	/**
	 * 
	 * @param valueFullStr (get from EntityInfo.getPropValueNoBracketList(), i.e. full for literal, noBracket for uri;
	 * @param puriBracket
	 * @param dbname
	 * @return
	 * @author qxliu 2019年10月22日 下午8:45:12
	 */
	public static String getValueText(String valueFullStr, String puri, dbName dbname){
		String vlabel = null;
		Object obj = null;
		if((obj=cacheName.retrieve(valueFullStr))!=null){
			vlabel = (String)obj;
		}else{
			if(valueFullStr.startsWith("\"")){//for literal
				int lastQuoteIdx = valueFullStr.lastIndexOf("\"");
				vlabel = valueFullStr.substring(1,lastQuoteIdx);
			} else{// if(valueFullStr.startsWith("<")){//for resource
				String vuri = valueFullStr;
				if(isDocumentProp(puri)){
					vlabel = vuri;
				}else{
					vlabel = getUriTextFromDB(vuri, dbname, true);
				}
			}
			if(vlabel!=null){
				cacheName.store(valueFullStr, vlabel);
			}else{
				System.err.println("no vlabel! value="+valueFullStr+"prop="+puri+"\tvlabel="+vlabel);
			}
		}
		return vlabel;
	}
	/**
	 * 
	 * @param puri
	 * @param dbname
	 * @return
	 * @author qxliu 2019年10月22日 上午10:28:36
	 */
	public static String getPropText(String puri, dbName dbname){
		Object obj;
		if((obj=cacheName.retrieve(puri))!=null){
				return (String)obj;
		}else{
			String plabel = getUriTextFromDB(puri, dbname, true);
			if(plabel==null){
				System.err.println("no vlabel! puri="+puri+"\tplabel="+plabel);
			}
			return plabel;
			
		}
	}
	
	/**
	 * @author qxliu 2019年10月22日 上午10:16:01
	 * 
	 * @param uri
	 * @param dbname
	 * @return
	 * @author qxliu 2019年6月8日 下午4:44:51
	 */
	private static String getUriTextFromDB(String uri, dbName dbname, boolean parseLnIfNull){
		String text = null;
		try {
			Connection conn = DBConnector.getDsSumEval2016Connection();
			PreparedStatement pstmt = conn.prepareStatement("select readable,uri from all_enames_"+dbname.name()+" where uri=?");
			pstmt.setString(1, uri);
			ResultSet rs = pstmt.executeQuery();
			while(rs.next()){
				text = rs.getString(1);
				if(text==null){//for readable=null uris(like pages)
					text = rs.getString(2);
				}
			}
			rs.close();
			pstmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(parseLnIfNull && text==null){//for not in db
//			text = ClassList.getReadableLocalName(uri, dbname);
			text = getReadableLocalName(uri, dbname);
//			text = StringUtil.getRefinedName(text, uri);
			text = getRefinedName(text, uri);
			if(text.trim().length()==0){//no local name, for exceptions get by DataPrepare.checkVLabels()
				text = uri;
			}
		}

		return text;
	}

	private static boolean isDocumentProp(String puri) {
		if (uriPropSet.contains(puri)){
			return true;
		} else {
			return false;
		}
	}

	/**
	 * from ClassList
	 * @param uri
	 * @param dbname
	 * @return
	 * @author qxliu Mar 5, 2019 2:59:36 PM
	 */
	public static String getReadableLocalName(String uri, dbName dbname){
		String ln = URIHelper.getNormalizedLocalName(uri);
		if(dbname!=null && dbname.equals(dbName.lmdb)){
			if(ln.matches(".\\d+$")){
				String nsUri = URIHelper.guessNSURI(uri);
				String clsName = URIHelper.getLocalName(nsUri.substring(0, nsUri.length()-1));

				ln = clsName+" #"+ln; // e.g. http://data.linkedmdb.org/resource/performance/187510 to performance #187510
			}//end if
		}
		return ln;
	}



	/**
	 * 20200920: copied from StringUtil
	 * 20180722: copied (and modified) from eSummRL project
	 * @param name
	 * @param uri
	 * @return
	 * @author qxliu Dec 14, 2017 9:19:06 PM
	 */
	public static String getRefinedName(String name, String uri){

		boolean isYago = uri.startsWith("http://dbpedia.org/class/yago");
		boolean isCategory = uri.startsWith("http://dbpedia.org/resource/Category:");
		String newName = name.trim();
		if(newName.length()==0) return newName;
		//0. decode (should do before handling numbers) @deprecated will cause IllegalArugmentExcept when contains % (should replace % with %25 first),
		//e.g. "PopulatedPlacesInTheMunicipalityOf%C5%A0kocjan"
		try {
			newName = newName.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
			newName = URLDecoder.decode(newName, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(isYago){
			//1. remove redundant numbers
			boolean hasRedundantNum = name.matches("^\\w+\\d{9}$");
			if(hasRedundantNum){
				newName = newName.substring(0, newName.length()-9);
			}
			//2. split with numbers
			boolean hasYearNum = newName.matches("\\S*\\d+\\S*");//("^\\w*\\d{4}\\w*$");
			if(!newName.contains(" ") && !newName.contains("_") && hasYearNum){
				String[] chItems = newName.split("\\d+");
				List<String> numItems = getMatcher("(\\d+)", newName);
				StringBuffer sb = new StringBuffer();
				sb.append(chItems[0]);
				for(int i=0; i<numItems.size(); i++){
					sb.append(" ");
					sb.append(numItems.get(i));
					sb.append(" ");
					if(i+1<chItems.length) sb.append(chItems[i+1]);
				}
				newName = sb.toString().trim();
			}
			//3. add space around brackets
			newName = newName.replace("(", " (").replace(")", ") ").replace("  ", " ");
			//4. replace underline
			newName = newName.replace("_", " ");
			//5. repair camel case, and replace under-line with space
			if(!name.contains(" ")){//has no space in original str, maybe camel case
				String[] tokens = newName.split(" ");
				StringBuffer buffer = new StringBuffer();
				for(String rawToken : tokens){
					String newStr = repairCamelCase(rawToken);
					buffer.append(newStr);
					buffer.append(' ');
				}//end_for
				newName = buffer.toString().replace("( ", "(");
			}
		}//end if_yago
		else{//for normal euri
			if(isCategory && name.startsWith("Category:") && !newName.contains(" ")){// category name from local name
				newName = newName.substring("Category:".length());
			}
			//1. replace underline
			newName = newName.replace("_", " ").trim();
			//2. remove camel case (only for no space)
			if(!newName.contains("-")
					&& !newName.contains(" ") //===== 20190218 add, for e.g. uri=http://data.linkedmdb.org/resource/actor/29494
			){
				newName = repairCamelCase(newName);
			}
		}
		return newName.trim();
	}

	/**
	 *
	 * 20200920: copied from StringUtil
	 * @param regex
	 * @param source
	 * @return
	 * @author qxliu Dec 14, 2017 4:54:01 PM
	 */
	public static List<String> getMatcher(String regex, String source) {
		List<String> result = new ArrayList<>();;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(source);
		while (matcher.find()) {
			result.add(matcher.group(1));
		}
		return result;
	}
	/**
	 *
	 * 20200920: copied from StringUtil
	 * @param camelCasedStr
	 * @return
	 * @author qxliu Dec 21, 2017 9:55:59 PM
	 */
	private static String repairCamelCase(String camelCasedStr){
//		System.out.println("camel:\t"+camelCasedStr);
		int start = 0;
		int end = 0;
		StringBuffer buffer = new StringBuffer();
		while (end < camelCasedStr.length() - 1) {
			char curr = camelCasedStr.charAt(end);
			char next = camelCasedStr.charAt(end + 1);
			boolean lastIsDash = (end-1>=0)?(camelCasedStr.charAt(end-1)=='-'):false;//my modify, for e.g. "St.Mary'sSeminary_Mary-the-Good(Ohio)1968Alumni337"
			if (Character.isLowerCase(curr) && Character.isUpperCase(next)) {//end a token
				String token = camelCasedStr.substring(start, end + 1);
				buffer.append(token);
				buffer.append(' ');
				start = end + 1;
			} else if (Character.isUpperCase(curr)
					&& Character.isLowerCase(next)
					&& !lastIsDash
			) {//start a token
				String token = camelCasedStr.substring(start, end);
				if (token.length() != 0) {
					buffer.append(token);
					buffer.append(' ');
					start = end;
				}
			}//else continue a token
			end++;
		}//end_while
		String token = camelCasedStr.substring(start, end + 1);

		buffer.append(token);
//		buffer.append(' ');

		return buffer.toString().trim().replace("  ", " ");
	}
}
