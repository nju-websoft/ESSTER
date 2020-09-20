//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

//import cn.edu.nju.ws.falconet.gcheng.util.URIHelper;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.StringTokenizer;


/**
 * modified from Gong Cheng's Falcons
 * cn.edu.nju.ws.falconet.gcheng.util.URIHelper;
 * @author Qingxia Liu 2015-10-3 下午3:11:08
 *
 */
public class URIHelper {
	private static URIHelper INSTANCE = null;

	private static String delimiters = " \t\n\r\f~!@#$%^&*()_+|`-=\\{}[]:\";'<>?,./";

	public static synchronized URIHelper getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new URIHelper();
		}
		return INSTANCE;
	}
	/**
	 * get local name by splitting uri (more prefered than by id)
	 * @param uri
	 * @return
	 * @author Qingxia Liu 2015-9-26 下午8:59:13
	 */
	public static String getLocalName(String uri){
		String nsURI = URIHelper.guessNSURI(uri);
//		System.out.println("nsURI:\t"+nsURI+"\t,uri:\t"+uri);
		String localName = uri.substring(nsURI.length());
		return localName;
	}
	/**
	 * including the last symbol
	 * @param uri
	 * @return
	 * @author Gong cheng before 2015-11-4 下午3:12:48
	 */
	public static String guessNSURI(String uri) {
		if (uri == null) {
			return null;
		}
		int hashPos = uri.lastIndexOf('#');
		if (hashPos != -1) {
			return uri.substring(0, hashPos + 1);
		}
		int slashPos = uri.lastIndexOf('/');
		if (slashPos != -1) {
			return uri.substring(0, slashPos + 1);
		}
		int colonPos = uri.lastIndexOf(':');
		if (colonPos != -1) {
			return uri.substring(0, colonPos + 1);
		}
		return null;
	}
	/**
	 * modify to static
	 * @param str
	 * @return
	 * @author Qingxia Liu 2015-10-3 下午3:22:03
	 */
	public static String normalizeLocalName(String str) {
		String decodedStr = null;
		try {
			decodedStr = URLDecoder.decode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.exit(1);
		}
//		System.out.println("str:"+str+"|"+decodedStr+"|"+delimiters);
		StringTokenizer rawTokens = new StringTokenizer(decodedStr.trim(),
				delimiters);
//		System.out.println("rt:"+rawTokens);
		StringBuffer buffer = new StringBuffer();
		while (rawTokens.hasMoreTokens()) {
			String rawToken = rawTokens.nextToken();
//			System.out.println("t:"+rawToken);
			int start = 0;
			int end = 0;
			while (end < rawToken.length() - 1) {
				char curr = rawToken.charAt(end);
				char next = rawToken.charAt(end + 1);
				if (Character.isLowerCase(curr) && Character.isUpperCase(next)) {
					String token = rawToken.substring(start, end + 1);
					buffer.append(token);
					buffer.append(' ');
					start = end + 1;
				} else if (Character.isUpperCase(curr)
						&& Character.isLowerCase(next)) {
					String token = rawToken.substring(start, end);
					if (token.length() != 0) {
						buffer.append(token);
						buffer.append(' ');
						start = end;
					}
				}
				end++;
			}//end_while
			String token = rawToken.substring(start, end + 1);
			buffer.append(token);
			buffer.append(' ');
		}

		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer.toString();
	}
	/**
	 * 
	 * @param uri
	 * @return
	 * @author Qingxia Liu 2015-12-3 下午4:42:28
	 */
	public static String getNormalizedLocalName(String uri){
		String localName = URIHelper.getInstance().getLocalName(uri);
		localName = URIHelper.normalizeLocalName(localName);
		return localName;
	}
	/**
	 * modified to static
	 * @param rawURIStr
	 * @return
	 * @author Qingxia Liu 2015-10-3 下午3:13:14
	 */
	public static URI normalizeURI(String rawURIStr) {
		if ((rawURIStr == null) || rawURIStr.equals("")) {
			return null;
		}
		String trimmedRawURIStr = rawURIStr.trim();
		URI rawURI = null;
		try {
			rawURI = new URI(trimmedRawURIStr);
		} catch (URISyntaxException e) {
			return null;
		}

		String scheme = rawURI.getScheme();
		if (scheme != null) {
			scheme = scheme.toLowerCase();

			/* http */
			if (scheme.equals("http")) {
				String userInfo = rawURI.getUserInfo();
				String host = rawURI.getHost();
				if (host != null) {
					host = host.toLowerCase();
				}
				int port = rawURI.getPort();
				if (port == 80) {
					port = -1;
				}
				String path = rawURI.getPath();
				if ((path == null) || path.equals("")) {
					path = "/";
				}
				String query = rawURI.getQuery();
				String fragment = rawURI.getFragment();

				try {
					return new URI(scheme, userInfo, host, port, path, query,
							fragment);
				} catch (URISyntaxException e) {
					return null;
				}
			} else {
				try {
					return new URI(scheme, rawURI.getSchemeSpecificPart(),
							rawURI.getFragment());
				} catch (URISyntaxException e) {
					return null;
				}
			}
		} else {
			return rawURI;
		}
	}

	public static String removeFragment(String uri) {
		if (uri == null) {
			return null;
		} else {
			int hashPos = uri.indexOf('#');
			if (hashPos != -1) {
				return uri.substring(0, hashPos);
			} else {
				return uri;
			}
		}
	}
	public static void main(String[] args){
		String uri = "http://dbpedia.org/ontology/Non-ProfitOrganisation";
		System.out.println(getNormalizedLocalName(uri));
		String str = "Non-ProfitOrganisation";
		System.out.println(normalizeLocalName(str));
	}
}
