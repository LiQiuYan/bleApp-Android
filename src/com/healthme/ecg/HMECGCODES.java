package com.healthme.ecg;

import java.util.HashMap;
import java.util.Map;

public class HMECGCODES extends ECGCODES{

	public static final short PAUSE = 501;
	public static final short MINRATE=502;
	public static final short MAXRATE=503;
	public static final short MAXPVCNUM=504;
	public static final short MAXSVPBNUM=505;
	public static final short MINAVGRATE=506;
	public static final short MAXAVGRATE=507;
	public static final short MAXPAUSE=508;
	public static final short LONGRR=509;
	public static final short TPVC=510; //total pvc number
	public static final short TSVPB=511;//total svpb number
	public static final short TACHYCARDIA=512;
	public static final short BRADYCARDIA=513;
	
	/**
	 * 1xx 代表 xx的 1发
	 * 2xx 代表 xx的 二连发，二连律
	 * 3xx 代表 xx的 三连发，三连律
	 */
	public static final short PVC1=100+PVC;//
	public static final short PVC2=200+PVC;//
	public static final short PVC3=300+PVC;//
	public static final short SVPB1=100+SVPB;//
	public static final short SVPB2=200+SVPB;//
	public static final short SVPB3=300+SVPB;//
	public static final short APC1=100+APC;//
	public static final short APC2=200+APC;//
	public static final short APC3=300+APC;//
	
	public static final short DOUBLE=250;
	public static final short D_SVPB=250+SVPB;//double SVPB
	public static final short D_PVC=250+PVC;//double PVC
	public static final short SERIAL=350;
	public static final short S_SVPB=350+SVPB;//serial SVPB
	public static final short S_PVC=350+PVC;//serial PVC
	
	public static final short UVNT=400;//user event
	static HashMap<Short, String> decribeMap=new HashMap<Short,String>();
	static {
		decribeMap.put(NORMAL, "N");
		decribeMap.put(LBBB, "L");
		decribeMap.put(RBBB, "R");
		decribeMap.put(ABERR, "a");
		decribeMap.put(PVC, "V");
		decribeMap.put(NPC, "J");
		decribeMap.put(FUSION, "F");
		decribeMap.put(APC, "A");
		decribeMap.put(SVPB, "S");
		decribeMap.put(VESC, "E");
		decribeMap.put(NESC, "j");
		decribeMap.put(PACE, "/");
		decribeMap.put(UNKNOWN, "Q");
		decribeMap.put(NOISE, "~");
		decribeMap.put(ARFCT, "|");
		decribeMap.put(STCH, "s");
		decribeMap.put(TCH, "T");
		decribeMap.put(SYSTOLE, "*");
		decribeMap.put(DIASTOLE, "D");
		decribeMap.put(NOTE, "\"");
		decribeMap.put(MEASURE, "=");
		decribeMap.put(BBB, "B");
		decribeMap.put(PACESP, "^");
		decribeMap.put(RHYTHM, "+");
		decribeMap.put(LEARN, "?");
		decribeMap.put(FLWAV, "!");
		decribeMap.put(VFON, "[");
		decribeMap.put(VFOFF, "]");
		decribeMap.put(AESC, "e");
		decribeMap.put(SVESC, "n");
		decribeMap.put(NAPC, "x");
		decribeMap.put(PFUS, "f");
		decribeMap.put(PQ, ",");
		decribeMap.put(JPT, "`");
		decribeMap.put(RONT, "r");
		decribeMap.put(AB, "(AB");
		decribeMap.put(AFIB, "(AFIB");
		decribeMap.put(AFL, "(AFL");
		decribeMap.put(B, "(B");
		decribeMap.put(BII, "(BII");
		decribeMap.put(IVR, "(IVR");
		decribeMap.put(N, "(N");
		decribeMap.put(NOD, "(NOD");
		decribeMap.put(P, "(P");
		decribeMap.put(PREX, "(PREX");
		decribeMap.put(SBR, "(SBR");
		decribeMap.put(SVTA, "(SVTA");
		decribeMap.put(T, "(T");
		decribeMap.put(VFL, "(VFL");
		decribeMap.put(VT, "(VT");
	}
	
	static HashMap<Short, String> chnMap=new HashMap<Short,String>();
	static {
		chnMap.put(PVC, "室早");
		chnMap.put(PVC1, "单发");
		chnMap.put(PVC2, "二联律");
		chnMap.put(PVC3, "三联律");
		chnMap.put(SVPB, "室上早");
		chnMap.put(SVPB1, "单发");
		chnMap.put(SVPB2, "二联律");
		chnMap.put(SVPB3, "三联律");
		chnMap.put(PAUSE, "停博");
		chnMap.put(AFIB, "房颤");
		chnMap.put(NOISE, "噪音");
		chnMap.put(LONGRR, "长RR间隔");
		chnMap.put(UVNT, "事件");
	}
	
	public static String getChn(short code){
		String chn=chnMap.get(code);
		return chn!=null?chn:"未知";
	}
	
	static HashMap<Short,short[]> subGroupMap=new HashMap<Short,short[]>();
	static{
		subGroupMap.put(PVC, new short[]{PVC1,PVC2,PVC3});
		subGroupMap.put(SVPB, new short[]{SVPB1,SVPB2,SVPB3});
		subGroupMap.put(APC, new short[]{APC1,APC2,APC3});
	}
	
	public static short[] getSubCodes(short code){
		short[] sub=subGroupMap.get(code);
		if(sub!=null)return sub.clone();
		return null;
	}
	
	public static Short getCode(String decribe){		
		for(Map.Entry<Short,String> en:decribeMap.entrySet()){
			if(en.getValue().equals(decribe)){
				return en.getKey();
			}
		}
		return null;
	}
	
	
	public static String getAuxByCode(int code) {
		String aux="UNKNOWN";
		String v = decribeMap.get((short)code);
		return (v==null?aux:v);
	}
	
	public static boolean isQRS(int type) {
		if(type==HMECGCODES.NORMAL
				||type==HMECGCODES.LBBB
				||type==HMECGCODES.RBBB
				||type==HMECGCODES.BBB
				||type==HMECGCODES.APC
				||type==HMECGCODES.ABERR
				||type==HMECGCODES.NPC
				||type==HMECGCODES.SVPB
				||type==HMECGCODES.PVC
				||type==HMECGCODES.RONT
				||type==HMECGCODES.FUSION
				||type==HMECGCODES.AESC
				||type==HMECGCODES.NESC
				||type==HMECGCODES.SVESC
				||type==HMECGCODES.VESC
				||type==HMECGCODES.PACE
				||type==HMECGCODES.PFUS
				||type==HMECGCODES.UNKNOWN
				||type==HMECGCODES.LEARN)
			return true;
		return false;
	}
	
	public static boolean isNORMAL(int type) {
		return type==HMECGCODES.NORMAL;
	}
	public static boolean isRhythm(int type) {
		return type<0;
	}
	
	public static boolean isSpecType(int type) {
		return type>=SKIP&&type<AUX;
	}
	
	public static boolean isBasicType(int type) {
		return type>=0&&type<ACMAX;
	}
	
	public static String getDescribe(short code) {
		String d = decribeMap.get(code);
		return (d!=null?d:"UNKNOWN");
	}
	public static String getDescribe(int code) {
		String d = decribeMap.get((short)code);
		return (d!=null?d:"UNKNOWN");
	}
}
