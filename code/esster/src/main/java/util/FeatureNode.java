//package cn.edu.nju.ws.es.web.algo.info2_phd_eyre2020.util;
package main.java.util;

public class FeatureNode{
	private String subject; //un-bracked
	private String property;
	private String object;
	private String slabel;
	private String olabel;
	private String plabel;
	private String line;
	boolean inverse;
	Double selfinfoCharacteristic=null;
	
	//add by qxliu
	Integer id = null;
	Double selfInfo = null;//for xls methods
	
	public FeatureNode(String subject, String property, String object, 
			String slabel, String plabel, String olabel, boolean inverse
//			, String line
			){
		this.subject = subject;
		this.property = property;
		this.object = object;
		this.slabel = slabel;
		this.plabel = plabel;
		this.olabel = olabel;
		this.inverse = inverse;
//		this.line = line;
	}
	/**
	 * for test outside entity from nt-file
	 * @param subject
	 * @param property
	 * @param object
	 * @param slabel
	 * @param plabel
	 * @param olabel
	 * @param inverse
	 * @param line
	 * @author qingxialiu Dec 28, 2016 4:02:09 PM
	 */
	public FeatureNode(int id, String subject, String property, String object, 
			String slabel, String plabel, String olabel, boolean inverse
			, String line
			){
		this(subject, property, object, slabel, plabel, olabel, inverse);
		this.id = id;
		this.line = line;
	}
	public FeatureNode(int id, String subject, String property, String object, 
			String slabel, String plabel, String olabel, boolean inverse
//			, String line
			){
//		this(subject, property, object, slabel, plabel, olabel, inverse, line);
		this(subject, property, object, slabel, plabel, olabel, inverse);
		this.id = id;
	}
	public Integer getId(){
		return id;
	}
	
	public String getSub(){
		return this.subject;
	}
	
	public String getPro(){
		return this.property;
	}
	
	public String getObj(){
		return this.object;
	}
	
	public boolean getInv(){
		return this.inverse;
	}
	
	public String getSLabel(){
		return this.slabel;
	}
	
	public String getPLabel(){
		return this.plabel;
	}
	public String getOLabel(){
		return this.olabel;
	}
//	public String getLine(){
//	return this.line;
//}

	public double getSelfChab(){
		return this.selfinfoCharacteristic;
	}
	public void setSelfChab(double selfChab){
		this.selfinfoCharacteristic = selfChab;
	}
	public double getSelfInfo(){
		return this.selfInfo;
	}
	public void setSelf(double si){
		this.selfInfo = si;
	}
	public void setSLabel(String slabel){
		this.slabel = slabel;
	}
	
	public void setPLabel(String plabel){
		this.plabel = plabel;
	}
	
	public void setOLabel(String olabel){
		this.olabel = olabel;
	}
	
	public void setPro(String property){
		this.property = property;
	}
	public void setObj(String object){
		this.object = object;
	}
	
	public String toString(){
//		return "("+getSLabel()+", "+getPLabel()+", "+getOLabel()+")";
		return "("+id+"\t"+getSelfChab()+")";
	}
	
}
