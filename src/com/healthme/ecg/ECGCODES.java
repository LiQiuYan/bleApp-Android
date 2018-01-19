package com.healthme.ecg;

/* file: ecgcodes.h	T. Baker and G. Moody	  June 1981
Last revised:  19 March 1992		dblib 7.0
ECG annotation codes

Copyright (C) Massachusetts Institute of Technology 1992. All rights reserved.
*/

public class ECGCODES {

	public static final short NOTQRS   =  0 ; /* not-QRS (not a getann/putann code) */
	public static final short NORMAL   =  1 ; /* normal beat */
	public static final short LBBB     =  2 ; /* left bundle branch block beat */
	public static final short RBBB     =  3 ; /* right bundle branch block beat */
	public static final short ABERR    =  4 ; /* aberrated atrial premature beat */
	public static final short PVC      =  5 ; /* premature ventricular contraction */
	public static final short FUSION   =  6 ; /* fusion of ventricular and normal beat */
	public static final short NPC      =  7 ; /* nodal (junctional) premature beat */
	public static final short APC      =  8 ; /* atrial premature contraction */
	public static final short SVPB     =  9 ; /* premature or ectopic supraventricular beat */
	public static final short VESC     = 10 ; /* ventricular escape beat */
	public static final short NESC     = 11 ; /* nodal (junctional) escape beat */
	public static final short PACE     = 12 ; /* paced beat */
	public static final short UNKNOWN  = 13 ; /* unclassifiable beat */
	public static final short NOISE    = 14 ; /* signal quality change */
	public static final short ARFCT    = 16 ; /* isolated QRS-like artifact */
	public static final short STCH     = 18 ; /* ST change */
	public static final short TCH      = 19 ; /* T-wave change */
	public static final short SYSTOLE  = 20 ; /* systole */
	public static final short DIASTOLE = 21 ; /* diastole */
	public static final short NOTE     = 22 ; /* comment annotation */
	public static final short MEASURE  = 23 ; /* measurement annotation */
	public static final short BBB      = 25 ; /* left or right bundle branch block */
	public static final short PACESP   = 26 ; /* non-conducted pacer spike */
	public static final short RHYTHM   = 28 ; /* rhythm change */
	public static final short LEARN    = 30 ; /* learning */
	public static final short FLWAV    = 31 ; /* ventricular flutter wave */
	public static final short VFON     = 32 ; /* start of ventricular flutter/fibrillation */
	public static final short VFOFF    = 33 ; /* end of ventricular flutter/fibrillation */
	public static final short AESC     = 34 ; /* atrial escape beat */
	public static final short SVESC    = 35 ; /* supraventricular escape beat */
	public static final short NAPC     = 37 ; /* non-conducted P-wave (blocked APB) */
	public static final short PFUS     = 38 ; /* fusion of paced and normal beat */
	public static final short PQ       = 39 ; /* PQ junction (beginning of QRS) */
	public static final short JPT      = 40 ; /* J poshort (end of QRS) */
	public static final short RONT     = 41 ; /* R-on-T premature ventricular contraction */

	/* ... annotation codes between RONT+1 and ACMAX inclusive are user-defined */

	public static final short ACMAX    = 49 ; /* value of largest valid annot code (must be < 50) */
	
	
	public static final short SKIP	   =59;
	public static final short NUM	   =60;
	public static final short SUB	   =61;
	public static final short CHN	   =62;
	public static final short AUX	   =63;
	
	
	//minus type is to discribe the rhythm, ST segement, T-wave change.Extended by Healthme 
	public static final short AB		=-1;	//Atrial bigeminy
	public static final short AFIB		=-2;	//Atrial fibrillation
	public static final short AFL		=-3;	//Atrial flutter
	public static final short B			=-4;	//Ventricular bigeminy
	public static final short BII		=-5;	//2° heart block
	public static final short IVR		=-6;	//Idioventricular rhythm
	public static final short N			=-7;	//Normal sinus rhythm
	public static final short NOD		=-8;	//Nodal (A-V junctional) rhythm
	public static final short P			=-9;	//Paced rhythm
	public static final short PREX		=-10;	//Pre-excitation (WPW)
	public static final short SBR		=-11;	//Sinus bradycardia
	public static final short SVTA		=-12;	//Supraventricular tachyarrhythmia
	public static final short T			=-13;	//Ventricular trigeminy
	public static final short VFL		=-14;	//Ventricular flutter
	public static final short VT		=-15;	//Ventricular tachycardia
	
	
}
