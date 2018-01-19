package com.healthme.app.ui;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.androidplot.Plot;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.healthme.app.AppContext;
import com.healthme.app.R;
import com.healthme.app.common.HandlerEvent;

public class DynamicPlot implements HandlerEvent{
	
	private MyPlotUpdater plotUpdater;
	private SampleDynamicXYDatasource data;
	private AppContext appContext;
	private FixedLoopList internalData=new FixedLoopList(SAMPLE_SIZE/2);
	private boolean keepRunning;
	private boolean pause = false;
	public DynamicPlot(AppContext appContext,XYPlot dynamicPlot) {
		this.appContext=appContext;
		plotUpdater = new MyPlotUpdater(dynamicPlot);

		// only display whole numbers in domain labels
		dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

		// getInstance and position datasets:
		data = new SampleDynamicXYDatasource();
		// SampleDynamicSeries sine1Series = new SampleDynamicSeries(data, 0,
		// "Sine 1");
		SampleDynamicSeries sine2Series = new SampleDynamicSeries(data, 1, "");
		LineAndPointFormatter lineFormatter = new LineAndPointFormatter(Color.RED, null, null, null);
		lineFormatter.getLinePaint().setStrokeWidth(4);
		lineFormatter.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

		// formatter2.getFillPaint().setAlpha(220);
		dynamicPlot.addSeries(sine2Series, lineFormatter);

		// hook up the plotUpdater to the data model:
		data.addObserver(plotUpdater);

		// thin out domain tick labels so they dont overlap each other:
		dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
		dynamicPlot.setDomainStepValue(SAMPLE_RATE / (5 * 5));
		Paint rangeGridLinePaint = new Paint();
		// FF1493
//		rangeGridLinePaint.setColor(Color.rgb(0x0, 0x0, 0x0));// 180, 0, 0));
		rangeGridLinePaint.setAntiAlias(true);
		rangeGridLinePaint.setStrokeWidth(2);
		Paint rangeSubGridLinePaint = new Paint();
		// FF1493
		rangeSubGridLinePaint.setColor(Color.TRANSPARENT);// 180, 0, 0));
		rangeSubGridLinePaint.setAntiAlias(true); // rangeGridLinePaint.setStyle(Paint.Style.FILL);
		rangeSubGridLinePaint.setStrokeWidth(1);
		dynamicPlot.getGraphWidget().setDomainGridLinePaint(rangeGridLinePaint);
		dynamicPlot.getGraphWidget().setRangeGridLinePaint(rangeGridLinePaint);
		dynamicPlot.getGraphWidget().setDomainSubGridLinePaint(rangeSubGridLinePaint);
		dynamicPlot.setTicksPerDomainLabel(5);
		dynamicPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
		dynamicPlot.setRangeStepValue(100);
		dynamicPlot.setTicksPerRangeLabel(5);
		dynamicPlot.getGraphWidget().setRangeSubGridLinePaint(rangeSubGridLinePaint);
        dynamicPlot.setGridPadding(0, 0, 0, 0); // yl

		final int vcc = 3300;
		final int multiplier = 4096 * 400 / vcc;
		// uncomment this line to freeze the range boundaries:
//		dynamicPlot.setRangeBoundaries(((-1.5)*multiplier), (multiplier *5 /2), BoundaryMode.FIXED);
		dynamicPlot.setRangeBoundaries(-512, 512, BoundaryMode.FIXED);
		// create a dash effect for domain and range grid lines:
		DashPathEffect dashFx = new DashPathEffect(new float[] {
				PixelUtils.dpToPix(3), PixelUtils.dpToPix(3) }, 0);
		dynamicPlot.getGraphWidget().getDomainSubGridLinePaint().setPathEffect(dashFx);
		dynamicPlot.getGraphWidget().getRangeSubGridLinePaint().setPathEffect(dashFx);
		dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
//		dynamicPlot.getGraphWidget().getGridBackgroundPaint().setShader(new LinearGradient(1, 1, 1, 1, Color.WHITE,
//				Color.WHITE, Shader.TileMode.REPEAT));
		dynamicPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
		dynamicPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.TRANSPARENT);
		dynamicPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
		dynamicPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.TRANSPARENT);

		dynamicPlot.getGraphWidget().setSize( new SizeMetrics(
                -15,
                SizeLayoutType.FILL,
                -40,
                SizeLayoutType.FILL));
		dynamicPlot.getGraphWidget().position(-40, XLayoutStyle.ABSOLUTE_FROM_LEFT, 0, YLayoutStyle.ABSOLUTE_FROM_TOP);
        
        dynamicPlot.getLegendWidget().setVisible(false);
        dynamicPlot.getLayoutManager().remove(dynamicPlot.getDomainLabelWidget());
        
        
        dynamicPlot.setPlotMargins(0, 0, 0, 0);
        dynamicPlot.setPlotPadding(0, 0, 0, 0);
        
        dynamicPlot.getGraphWidget().setMargins(0, 0, 0, 0);
        dynamicPlot.getGraphWidget().setPadding(0, 0, 0, 0);
        dynamicPlot.setGridPadding(0, 0, 0, 0);
        
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(appContext.getResources().getColor(R.color.title_blue));
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setColor(appContext.getResources().getColor(R.color.title_blue));
        
        new Thread(data).start();
        //TODO fake code
 //       new NumberGenerator().start();
	}
	
	public void pause(){
		pause=true;
	}
	
	public void resume(){
		pause=false;
	}
	
	public void addSerialData(Number sample){
		if(!pause)
			internalData.add(sample);
	}
	private class MyPlotUpdater implements Observer {
		Plot plot;

		public MyPlotUpdater(Plot plot) {
			this.plot = plot;
		}

		@Override
		public void update(Observable o, Object arg) {
			plot.redraw();
		}
	}
	class FixedLoopList{
		private Number[] array;
		private int limit;
		private int pos;
		
		public FixedLoopList(int limit){
			this.limit=limit;
			array=new Number[limit];
			Arrays.fill(array, null);
		}
		
		public synchronized void add(Number number){
			if(pos>=limit)pos=0;
			if(pos==0){
				Arrays.fill(array, null);
			}
			array[pos]=number;
			pos++;
		}
		
		public int size(){
			return limit;
		}
		
		public Number get(int index){
			return array[index%limit];
		}
	}
	
	class SampleDynamicSeries implements XYSeries {
		private SampleDynamicXYDatasource datasource;
		private int seriesIndex;
		private String title;

		public SampleDynamicSeries(SampleDynamicXYDatasource datasource,
				int seriesIndex, String title) {
			this.datasource = datasource;
			this.seriesIndex = seriesIndex;
			this.title = title;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public int size() {
			return datasource.getItemCount(seriesIndex);
		}

		@Override
		public Number getX(int index) {
			return datasource.getX(seriesIndex, index);
		}

		@Override
		public Number getY(int index) {
			return datasource.getY(seriesIndex, index);
		}
	}
	public class SampleDynamicXYDatasource implements Runnable {
		// encapsulates management of the observers watching this datasource for
		// update events:
		class MyObservable extends Observable {
			@Override
			public void notifyObservers() {
				setChanged();
				super.notifyObservers();
			}
		}

//		private int phase = 0;
		private MyObservable notifier;

		{
			notifier = new MyObservable();
//			 for (int i=0;i<SAMPLE_SIZE;i++)
//			 {
//			     internalData.add(0); // 500; // 0;
//			 }
		}

		public void stopThread() {
			keepRunning = false;
		}

		// @Override
		public void run() {
			try {
				keepRunning = true;
				
				int i=0;
				while (keepRunning) {
					Thread.sleep(REFRESH_RATE); // decrease or remove to speed up
					if(internalData.size()<1)
						continue;
					notifier.notifyObservers();
					
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public int getItemCount(int series) {
			return internalData.size();
		}

		public Number getX(int series, int index) {
			if (index >= SAMPLE_SIZE/2) {
				throw new IllegalArgumentException();
			}
			int x = index++;
			return x;
		}

		public Number getY(int series, int index) {
			if (index >= SAMPLE_SIZE/2) {
				throw new IllegalArgumentException();
			}

			Number y = internalData.get(index);
//			if (index == (SAMPLE_SIZE / 2 - 1)) {
////				phase++;
//				int dropNum = internalData.size() - SAMPLE_SIZE;
//				for (int i = 0; i < dropNum; i++) {
//					internalData.remove(0);
//				}
//			}
			return y;
		}

		public void addObserver(Observer observer) {
			notifier.addObserver(observer);
		}

		public void removeObserver(Observer observer) {
			notifier.deleteObserver(observer);
		}
	}
	
	

//	Integer[] s = new Integer[]{995, 995, 995, 995, 1000, 997, 993, 993, 989, 987, 990,
//			989, 987, 994, 992, 986, 978, 975, 971, 969, 969, 972, 968,
//			966, 966, 967, 966, 969, 968, 963, 966, 967, 963, 959, 951,
//			947, 935, 927, 946, 986, 1010, 1138, 1187, 1158, 1045, 930,
//			938, 954, 959, 955, 958, 961, 959, 957, 955, 957, 957, 959,
//			957, 953, 957, 961, 959, 956, 956, 955, 955, 958, 958, 955,
//			959, 958, 957, 956, 956, 954, 954, 959, 957, 955, 958, 956,
//			954, 956, 957, 958, 954, 957, 956, 953, 953, 956, 953, 952,
//			951, 952, 949, 952, 951, 949, 951, 952, 951, 955, 959, 959,
//			960, 964, 966, 966, 967, 973, 973, 973, 978, 975, 973, 975,
//			973, 973, 974, 971, 972, 970, 972, 969, 968, 971, 968, 967,
//			969, 971, 967, 968, 967, 968, 964, 965, 962, 963, 965, 967,
//			964, 966, 964, 963, 961, 965, 961, 958, 961, 963, 959, 959,
//			962, 963, 963, 964, 964, 963, 963, 966, 962, 963, 966, 968,
//			969, 972, 974, 979, 980, 984, 980, 980, 979, 979, 977, 978,
//			982, 974, 967, 963, 958, 959, 960, 961, 956, 956, 954, 954,
//			958, 959, 954, 956, 959, 958, 951, 943, 933, 921, 917, 959,
//			1009, 1091, 1174, 1199, 1181, 1083, 990, 944, 934, 937, 946,
//			947, 948, 948, 946, 947, 948, 942, 942, 945, 944, 947, 946,
//			946, 942, 946, 945, 941, 944, 943, 941, 944, 945, 946, 945,
//			946, 945, 943, 945, 947, 944, 944, 946, 947, 941, 944, 943,
//			940, 940, 940, 939, 938, 940, 938, 933, 936, 934, 930, 932,
//			934, 930, 932, 935, 936, 940, 948, 951, 952, 956, 961, 958,
//			960, 964, 964, 960, 963, 963, 959, 963, 964, 960, 962, 961,
//			963, 961, 965, 963, 960, 961, 961, 960, 961, 961, 959, 956,
//			957, 958, 954, 955, 958, 955, 957, 958, 957, 956, 958, 956,
//			952, 953, 956, 955, 956, 958, 957, 954, 957, 955, 953, 954,
//			957, 956, 958, 958, 957, 955, 959, 965, 965, 967, 971, 972,
//			974, 979, 981, 975, 976, 975, 969, 969, 978, 979, 971, 968,
//			967, 957, 958, 956, 950, 952, 954, 950, 948, 951, 951, 947,
//			951, 949, 943, 942, 927, 916, 913, 949, 974, 1094, 1185, 1208,
//			1110, 1034, 930, 938, 945, 945, 946, 944, 944, 945, 942, 943,
//			942, 942, 941, 943, 940, 939, 942, 942, 938, 940, 942, 939,
//			944, 944, 944, 942, 945, 943, 939, 939, 941, 939, 940, 944,
//			944, 941, 944, 944, 943, 947, 946, 943, 943, 942, 941, 940,
//			941, 940, 938, 937, 940, 940, 939, 945, 945, 951, 956, 958,
//			957, 959, 963, 961, 963, 964, 964, 962, 962, 963, 960, 959,
//			963, 960, 963, 965, 962, 959, 960, 957, 954, 956, 958, 954,
//			954, 953, 952, 952, 955, 953, 951, 951, 953, 949, 950, 951,
//			952, 949, 952, 954, 950, 951, 955, 952, 952, 953, 953, 951,
//			952, 952, 950, 952, 954, 955, 958, 963, 963, 965, 968, 968,
//			969, 971, 973, 970, 969, 970, 969, 969, 970, 974, 972, 968,
//			957, 950, 947, 950, 949, 946, 947, 947, 943, 944, 947, 943,
//			943, 946, 943, 930, 923, 918, 903, 895, 921, 960, 1017, 1108,
//			1151, 1182, 1070, 948, 921, 929, 930, 943, 944, 946, 945, 942,
//			945, 944, 941, 942, 943, 943, 943, 947, 946, 944, 944, 945,
//			943, 944, 948, 946, 946, 949, 947, 946, 949, 947, 946, 946,
//			948, 946, 947, 948, 946, 949, 949, 948, 947, 951, 948, 946,
//			949, 947, 948, 949, 951, 947, 947, 947, 951, 952, 958, 961,
//			960, 964, 966, 965, 967, 968, 967, 965, 966, 966, 965, 966,
//			968, 965, 964, 965, 965, 963, 965, 963, 961, 964, 963, 958,
//			958, 960, 960, 957, 961, 960, 958, 956, 958, 955, 955, 956,
//			957, 957, 958, 956, 956, 951, 955, 954, 954, 956, 955, 950,
//			953, 955, 954, 957, 958, 957, 958, 960, 965, 965, 968, 968,
//			969, 973, 977, 975, 973, 975, 972, 972, 970, 969, 973, 972,
//			966, 961, 957, 955, 956, 953, 956, 955, 952, 949, 951, 948,
//			949, 951, 951, 949, 952, 953, 950, 940, 931, 917, 916, 929,
//			1006, 1095, 1174, 1171, 1131, 949, 926, 937, 944, 946, 944,
//			939, 943, 943, 943, 946, 946, 945, 945, 947, 945, 940, 944,
//			944, 943, 942, 943, 942, 943, 940, 942, 940, 943, 945, 945,
//			945, 943, 939, 941, 943, 942, 943, 944, 945, 943, 943, 945,
//			942, 939, 939, 935, 935, 937, 930, 933, 936, 939, 935, 935,
//			938, 940, 941, 948, 951, 951, 957, 962, 962, 962, 962, 962,
//			960, 963, 965, 962, 962, 965, 963, 967, 968, 964, 961, 964,
//			961, 959, 960, 959, 958, 959, 959, 958, 956, 957, 958, 961,
//			958, 958, 957, 958, 960, 958, 954, 956, 960, 959, 960, 960,
//			958, 957, 958, 957, 956, 959, 959, 955, 957, 961, 963, 969,
//			970, 972, 973, 977, 980, 977, 979, 978, 975, 976, 978, 974,
//			976, 983, 982, 978, 968, 964, 958, 956, 955, 955, 954, 956,
//			955, 953, 951, 955, 951, 952, 955, 954, 947, 943, 933, 929,
//			915, 935, 975, 1043, 1087, 1197, 1150, 1023, 936, 924, 942,
//			946, 945, 946, 948, 945, 942, 930, 943, 943, 944, 945, 941,
//			941, 944, 943, 942, 945, 945, 943, 943, 946, 941, 941, 943,
//			942, 939, 943, 946, 944, 943, 944, 940, 941, 941, 940, 940,
//			944, 944, 944, 944, 944, 941, 939, 942, 939, 935, 937, 938,
//			934, 936, 936, 936, 938, 938, 944, 949, 957, 959, 958, 964,
//			967, 965, 965, 967, 965, 965, 968, 969, 966, 969, 970, 968,
//			967, 969, 966, 964, 965, 966, 964, 964, 964, 961, 959, 961,
//			961, 959, 962, 961, 958, 961, 962, 958, 958, 960, 959, 957,
//			959, 960, 959, 960, 961, 960, 958, 962, 958, 957, 958, 960,
//			958, 960, 961, 958, 960, 961, 964, 962, 970, 973, 972, 976,
//			978, 980, 981, 984, 980, 977, 979, 976, 976, 981, 982, 972,
//			967, 964, 959, 959, 958, 957, 956, 956, 960, 957, 955, 955,
//			956, 954, 956, 956, 955, 951, 939, 927, 920, 917, 962, 1021,
//			1106, 1191, 1213, 1127, 991, 933, 940, 948, 951, 948, 949, 951,
//			950, 948, 950, 947, 947, 947, 946, 943, 946, 945, 945, 944,
//			946, 945, 946, 947, 949, 946, 950, 950, 949, 947, 949, 948,
//			946, 947, 948, 945, 947, 948, 947, 948, 950, 949, 951, 951,
//			949, 945, 949, 950, 947, 946, 947, 947, 948, 950, 947, 946,
//			950, 949, 947, 951, 955, 958, 961, 962, 967, 966, 970, 968,
//			968, 967, 969, 967, 966, 968, 967, 964, 967, 967, 967, 967,
//			969, 965, 963, 967, 970, 967, 970, 971, 971, 974, 977, 974,
//			976, 981, 974, 981, 982, 978, 976, 975, 969, 961, 960, 962,
//			956, 952, 952, 953, 949, 949, 951, 950, 952, 954, 953, 952,
//			954, 953, 954, 950, 941, 926, 919, 914, 931, 982, 1047, 1135,
//			1171, 1177, 1053, 946, 932, 941, 948, 946, 947, 947, 945, 947,
//			947, 945, 943, 944, 946, 945, 930, 946, 942, 943, 946, 946,
//			944, 946, 946, 945, 944, 945, 942, 946, 947, 944, 945, 946,
//			946, 946, 948, 947, 946, 946, 947, 946, 945, 947, 946, 943,
//			946, 948, 947, 945, 945, 942, 940, 943, 943, 942, 946, 947,
//			945, 949, 950, 948, 953, 955, 957, 955};
//	public List<Integer> ls = Arrays.asList(s);
//class NumberGenerator extends Thread {
//		
//		public void run() {
//			while(true){
//				for (int i=0;i<ls.size();i++) {
//					try {
//						Thread.sleep(3);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
////					dataBuff.add(ls.get(i));
//					internalData.add(ls.get(i));
//				}
//			}
//		}
//		
//	}
	
}
