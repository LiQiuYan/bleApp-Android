package com.healthme.app.ui;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Shader;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.FloatMath;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidplot.Plot.BorderStyle;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;
import com.healthme.app.AppContext;
import com.healthme.app.AppManager;
import com.healthme.app.R;
import com.healthme.app.adapter.FragmentTabAdapter;
import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.bean.HeartBeatData;
import com.healthme.app.common.StringUtils;
import com.healthme.common.nio.socket.WorkerPool;
import com.healthme.ecg.HMECGCODES;

public class ECGCodeDetail extends FragmentActivity implements OnTouchListener {

	public final static int MAX_TAB_COUNT = 3;

	AppContext appContext;

	private ImageView back;
	private TextView ecg_info_title;

	private ImageView refresh;
	private short code;
	private short[] subCode;
	private String[] subTitle;
	private int tabCount;
	private int current = 0;

	private EcgRecord record;
	private long startTime;

	private RadioGroup rgs;

	public List<Fragment> fragments = new ArrayList<Fragment>();

	private SampleDynamicSeries series;
	private XYPlot plot;

	private Handler mHandler;

	private boolean isLoading;
	private final static int DATA_LOAD_ING = 0x001;
	private final static int DATA_LOAD_COMPLETE = 0x002;
	
	//基线
	static final int BASE = 0;
	//放大倍数
	static final int ZOOM = 400;
	static final int SAMPLE_RATE = 128;
	static final int SAMPLES_XSERIES = SAMPLE_RATE * 2; // number of examples in
														// X series
	
	//X 大格数
	static final int XGRID = SAMPLES_XSERIES / SAMPLE_RATE * 5;
	//Y 大格数
	static final int YGRID = 5;
	
	private static int LAYOUT_PLOT_DOMAIN_HEIGHT = 25;
	
	static final int YSTEP = ZOOM / 10;	
	static final int YMIN = BASE - YSTEP * 5 * 2;
	static final int YMAX = BASE + YSTEP * 5 * 3;
	
	static final double XSTEP = SAMPLE_RATE / 25.0;
	static final int XTICKS = 5;
	static final int YTICKS = 5;
	
	private RelativeLayout totalLay;
	
	private TextView sample_hint;
	
	private WorkerPool frontThreadPool=new WorkerPool("front-worker",1,1,new LinkedBlockingQueue<Runnable>());
	private WorkerPool backThreadPool=new WorkerPool("back-worker",1,1,new LinkedBlockingQueue<Runnable>());

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppManager.getAppManager().addActivity(this);
		setContentView(R.layout.ecg_code_detail);

		appContext = (AppContext) getApplication();
		record = (EcgRecord) getIntent().getSerializableExtra("record");
		startTime = record.getStartTime().getTime();
		code = getIntent().getShortExtra("code", (short) 0);
		ecg_info_title = (TextView) findViewById(R.id.head_title);
		ecg_info_title.setText(HMECGCODES.getChn(code));
		this.findViewById(R.id.head_back).setOnClickListener(backListener);
		this.findViewById(R.id.head_home).setOnClickListener(homeListener);	
		// 初始化视图控件
		this.initView();

	}

	private void initView() {
//		back = (ImageView) findViewById(R.id.ecg_back);
//		back.setOnClickListener(UIHelper.finish(this));
//
//		refresh = (ImageView) findViewById(R.id.ecg_refresh);
//		refresh.setOnClickListener(refreshClickListener);
		
		sample_hint=(TextView)findViewById(R.id.sample_hint);

		short[] sub = HMECGCODES.getSubCodes(code);

		if (sub == null) {
			sub = new short[] { code };
		}

		subCode = new short[Math.min(sub.length, MAX_TAB_COUNT)];

		System.arraycopy(sub, 0, subCode, 0, subCode.length);

		tabCount = subCode.length;

		subTitle = new String[subCode.length];

		rgs = (RadioGroup) findViewById(R.id.tabs_rg);

		int count = rgs.getChildCount();
		for (int i = 0; i < tabCount; i++) {
			subTitle[i] = HMECGCODES.getChn(subCode[i]);
			fragments.add(new ECGCodeDetailList(this, record, subCode[i]) {
				@Override
				public void drawWave(int recordId, int start, int end) {
					start-=SAMPLE_RATE/2;
					if(start<0)start=0;
					ECGCodeDetail.this.drawWave(start, end);
				}
			});
			((RadioButton) rgs.getChildAt(i)).setText(subTitle[i]);
		}
		while (count > tabCount) {
			rgs.removeViewAt(--count);
		}

		if (tabCount == 1) {
			((View)rgs.getParent()).setVisibility(View.GONE);
		} else {
			((RadioButton) rgs.getChildAt(0)).setChecked(true);
		}

		FragmentTabAdapter tabAdapter = new FragmentTabAdapter(this, fragments,
				R.id.tab_content, rgs);
		tabAdapter
				.setOnRgsExtraCheckedChangedListener(new FragmentTabAdapter.OnRgsExtraCheckedChangedListener() {
					@Override
					public void OnRgsExtraCheckedChanged(RadioGroup radioGroup,
							int checkedId, int index) {

					}
				});

		mHandler = new Handler() {

			public void handleMessage(Message msg) {

				int start = msg.arg1;

				int size = (int) (plot.maxXY.x - plot.minXY.x);
				plot.minXY.x = start;
				plot.maxXY.x = plot.minXY.x + size;
				plot.setDomainBoundaries(plot.minXY.x, plot.maxXY.x,
						BoundaryMode.FIXED);
				series.setDomain((int) plot.minXY.x,
						(int) (plot.maxXY.x - plot.minXY.x));

				plot.redraw();

			}

		};

		initPlot();
	}
	@Override
	public void onWindowFocusChanged(boolean hasFocus){
		super.onWindowFocusChanged(hasFocus);
		if(tabCount>1 && totalLay==null){
			initStatShow();
		}		
	}
	
	private Integer getStatCount(short code){
		switch(code){
			case HMECGCODES.PVC1:return record.getPvc1Number();
			case HMECGCODES.PVC2:return record.getPvc2Number();
			case HMECGCODES.PVC3:return record.getPvc3Number();
			case HMECGCODES.SVPB1:return record.getSvpb1Number();
			case HMECGCODES.SVPB2:return record.getSvpb2Number();
			case HMECGCODES.SVPB3:return record.getSvpb3Number();
		}
		
		return null;
	}
	
	private void initStatShow(){
		totalLay=(RelativeLayout)findViewById(R.id.totalLay);		
		
		for(int i=0;i<tabCount;i++){
			Integer num=getStatCount(subCode[i]);
			if(num!=null){
				RadioButton rb=(RadioButton) rgs.getChildAt(i);
				int[] location = new int[2];
		        rb.getLocationInWindow(location);   
		        
		        TextView text=new TextView(this);
		        text.setBackgroundResource(R.drawable.oval_red);	        
		        text.setGravity(Gravity.CENTER);
		        text.setTextColor(Color.WHITE);
		        text.setText(String.valueOf(num));
		        text.setTextSize(10);	        
		        
		        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		        lp.setMargins(location[0]+rb.getWidth()-sample_hint.getWidth(), location[1]-50, 0, 0);
		        
		        text.setLayoutParams(lp);
		        
		        totalLay.addView(text);
			}
		}
        
	}

	private void httpGetData(int startPos, int endPos) {
		try {
			HeartBeatData data = ApiClient.getRawData(appContext,
					record.getId(), startPos, endPos);

			if (data != null) {
				Object[] arr = data.getData();
				if (arr != null) {
					int first = data.getFirstPos().intValue();
					for (int i = 0; i < arr.length; i++) {
						Number o = (Number) arr[i];
						series.indexMap.put(first + i,
								o.shortValue());
					}
				}
			}
		} catch (Exception e) {
			Log.v("ERROR", e.getMessage());
		}
	}

	private void preLoadData(final int startPos, final int endPos) {
		backThreadPool.execute(new Runnable(){
			public void run() {
				
				int start = startPos, end = endPos;
				while (start < end) {
					if (series.indexMap.get(start) == null) {
						break;
					}
					start++;
				}
				while (start < end) {
					if (series.indexMap.get(end) == null) {
						break;
					}
					end--;
				}
				if (start < end) {
					start -= SAMPLES_XSERIES;
					if (start < 0)
						start = 0;

					end += SAMPLES_XSERIES;

					httpGetData(start, end);
				}				
			}
		});
	}

	private void drawWave(final int startPos, final int endPos) {
		frontThreadPool.execute(new Runnable(){
			public void run() {
				try {
					boolean hasData = true;
					int newStart = startPos - SAMPLES_XSERIES;
					if (newStart < 0)
						newStart = 0;
					int newEnd = startPos + SAMPLES_XSERIES;					

					for (int i = startPos; i <= newEnd; i++) {
						if (series.indexMap.get(i) == null) {
							hasData = false;
							break;
						}
					}
					if (!hasData) {
						httpGetData(newStart, newEnd + SAMPLES_XSERIES);
					}
					
					Message msg = new Message();
					msg.arg1 = startPos;
					msg.arg2 = endPos;
					mHandler.sendMessage(msg);
					if (hasData) {
						preLoadData(newStart, newEnd + SAMPLES_XSERIES);
					}

				} catch (Exception e) {
					Log.v("ERROR", e.getMessage());
				}

			}
		});
	}

	private void initPlot() {

		// 自定义视图

		Shader WHITE_SHADER = new LinearGradient(1, 1, 1, 1, Color.WHITE,
				Color.WHITE, Shader.TileMode.REPEAT);

		plot = (XYPlot) findViewById(R.id.ecgPlot);
		
		WindowManager wm = this.getWindowManager();
		int width = wm.getDefaultDisplay().getWidth();
		LAYOUT_PLOT_DOMAIN_HEIGHT = width / XGRID *2 /5;
		int height = width * YGRID / XGRID + LAYOUT_PLOT_DOMAIN_HEIGHT;
		ViewGroup.LayoutParams lp=plot.getLayoutParams();
		lp.width=width;
		lp.height=height;
		plot.setLayoutParams(lp);		
		
		plot.setOnTouchListener(this);

		// PVCItem item = new PVCItem();

		LineAndPointFormatter formatter2 = new LineAndPointFormatter(Color.rgb(
				0, 0, 200), null, null, null);
		formatter2.getLinePaint().setStrokeWidth(3);
		formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
		series = new SampleDynamicSeries(new ArrayList<Short>(),
				SimpleXYSeries.ArrayFormat.Y_VALS_ONLY, record.getId() + "");

		plot.addSeries(series, formatter2);
		plot.setTitle(StringUtils.formatDate(record.getStartTime()));
		plot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
		plot.setDomainStepValue(XSTEP);
		Paint rangeGridLinePaint = new Paint();
		rangeGridLinePaint.setColor(Color.rgb(0x0, 0x0, 0x0));// 180, 0, 0));
		rangeGridLinePaint.setAntiAlias(true);
		rangeGridLinePaint.setStrokeWidth(2);
		Paint rangeSubGridLinePaint = new Paint();
		rangeSubGridLinePaint.setColor(Color.rgb(0xff, 0x14, 0x93));// 180, 0,
																	// 0));
		rangeSubGridLinePaint.setAntiAlias(true); // rangeGridLinePaint.setStyle(Paint.Style.FILL);
		rangeSubGridLinePaint.setStrokeWidth(1);
		plot.getGraphWidget().setDomainGridLinePaint(rangeGridLinePaint);
		plot.getGraphWidget().setRangeGridLinePaint(rangeGridLinePaint);
		plot.getGraphWidget().setDomainSubGridLinePaint(rangeSubGridLinePaint);
		
		Paint domainLabelPaint = plot.getGraphWidget().getDomainLabelPaint();
		domainLabelPaint.setColor(Color.rgb(0x70, 0x80, 0x90));		
		plot.getGraphWidget().setDomainLabelPaint(domainLabelPaint);
		
		plot.setBorderStyle(BorderStyle.NONE, 0f, 0f);
		
		plot.setTicksPerDomainLabel(XTICKS);
		plot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
		plot.setRangeStepValue(YSTEP);
		plot.setTicksPerRangeLabel(YTICKS);
		plot.getGraphWidget().setRangeSubGridLinePaint(rangeSubGridLinePaint);
		plot.setGridPadding(0, 0, 0, 0); // yl
		plot.setRangeValueFormat(new DecimalFormat("###.#"));
		// plot.setDomainValueFormat(new DecimalFormat("####.#"));
		plot.setDomainValueFormat(new Format() {

			// create a simple date format that draws on the year portion of our
			// timestamp.
			// see
			// http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
			// for a full description of SimpleDateFormat.
			private SimpleDateFormat dateFormat = new SimpleDateFormat(
					"HH:mm:ss");

			private double gridSize = SAMPLE_RATE / 5.0 - 0.01;

			@Override
			public StringBuffer format(Object obj, StringBuffer toAppendTo,
					FieldPosition pos) {

				// because our timestamps are in seconds and SimpleDateFormat
				// expects milliseconds
				// we multiply our timestamp by 1000:
				// long timestamp = ((Number) obj).longValue() * 1000;
				// Date date = new Date(timestamp);
				double domain = ((Number) obj).doubleValue();
				int idomain = (int) domain;

				if (domain - idomain / SAMPLE_RATE * SAMPLE_RATE < gridSize) {
					toAppendTo.append(dateFormat.format(new Date(startTime
							+ idomain * 1000L / SAMPLE_RATE)));
					// toAppendTo.append(position/SAMPLE_RATE);
				}

				return toAppendTo;
				// return dateFormat.format(date, toAppendTo, pos);
			}

			@Override
			public Object parseObject(String source, ParsePosition pos) {
				return null;

			}
		});

		plot.setRangeBoundaries(YMIN, YMAX, BoundaryMode.FIXED);
		plot.setDomainBoundaries(0, SAMPLES_XSERIES, BoundaryMode.FIXED);
		// series.refreshData(0, SAMPLES_XSERIES);
		// y dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);
		// create a dash effect for domain and range grid lines:
		DashPathEffect dashFx = new DashPathEffect(new float[] {
				PixelUtils.dpToPix(3), PixelUtils.dpToPix(3) }, 0);
		// Paint paint = new Paint(R.drawable.frame_button_n);
		plot.getGraphWidget().getDomainSubGridLinePaint().setPathEffect(dashFx);
		plot.getGraphWidget().getRangeSubGridLinePaint().setPathEffect(dashFx);
		plot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
		plot.getGraphWidget().getGridBackgroundPaint().setShader(WHITE_SHADER);
		plot.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().getBackgroundPaint().setColor(Color.WHITE);
		plot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);
		plot.getGraphWidget().setSize(
				new SizeMetrics(0, SizeLayoutType.FILL, -40,
				// 0,
						SizeLayoutType.FILL));
		plot.getGraphWidget().position(-40, XLayoutStyle.ABSOLUTE_FROM_LEFT, 0,
				YLayoutStyle.ABSOLUTE_FROM_TOP);
		// plot.getGraphWidget().setRangeOriginLinePaint(null);
		// plot.getGraphWidget().setDomainOriginLinePaint(null);
		// plot.getGraphWidget().setRangeLabelPaint(null);
		// plot.getGraphWidget().setDomainLabelPaint(null);
		// plot.setRangeLabel(null);
		plot.getDomainLabelWidget().setSize(
				new SizeMetrics(LAYOUT_PLOT_DOMAIN_HEIGHT, SizeLayoutType.FILL, 0,
				// 0,
						SizeLayoutType.FILL));
		// plot.getDomainLabelWidget().position(0,
		// XLayoutStyle.ABSOLUTE_FROM_LEFT, -20,
		// YLayoutStyle.ABSOLUTE_FROM_BOTTOM);
		plot.getLegendWidget().setVisible(false);
		// plot.getLayoutManager().remove(plot.getDomainLabelWidget());

		// plot.getGraphWidget().setDomainOriginLinePaint(null);

		// plot.getGraphWidget().setBackgroundPaint(null);
		plot.setPlotMargins(0, 0, 0, 0);
		plot.setPlotPadding(0, 0, 0, 0);

		plot.getGraphWidget().setMargins(0, 0, 0, LAYOUT_PLOT_DOMAIN_HEIGHT);
		// plot.getGraphWidget().setPadding(0, 0, 0, 0);
		// plot.setGridPadding(0, 0, 0, 0);
		// plot.setDomainLabelWidget(null);
		// plot.setRangeLabelWidget(null);
		plot.setLegendWidget(null);
		// plot.setTitleWidget(null);

		plot.calculateMinMaxVals();
		plot.minXY = new PointF(plot.getCalculatedMinX().floatValue(), plot
				.getCalculatedMinY().floatValue());
		plot.maxXY = new PointF(plot.getCalculatedMaxX().floatValue(), plot
				.getCalculatedMaxY().floatValue());
		this.drawWave((int) plot.minXY.x, (int) plot.maxXY.x);
		// plot.minXY = new PointF(0,0);
		// plot.maxXY = new PointF(400,2000);
		// }
		// plot.setTag(item);

	}

	// Definition of the touch states
	static final int NONE = 0;
	static final int ONE_FINGER_DRAG = 1;
	static final int TWO_FINGERS_DRAG = 2;
	int mode = NONE;

	PointF firstFinger;
	float distBetweenFingers;
	boolean stopThread = false;

	@Override
	public boolean onTouch(final View arg0, final MotionEvent event) {
		XYPlot plot = (XYPlot) arg0.findViewById(R.id.ecgPlot);
		// XYPlot plot = (XYPlot)arg0;
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: // Start gesture
			firstFinger = new PointF(event.getX(), event.getY());
			mode = ONE_FINGER_DRAG;
			stopThread = true;
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_DOWN: // second finger
			distBetweenFingers = spacing(event);
			// the distance check is done to avoid false alarms
			if (distBetweenFingers > 5f) {
				mode = TWO_FINGERS_DRAG;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if (isLoading) {
				Toast.makeText((AppContext) getApplication(), "请稍候，正在调用数据...",
						Toast.LENGTH_SHORT).show();

				break;
			}
			if (mode == ONE_FINGER_DRAG) {
				PointF oldFirstFinger = firstFinger;
				firstFinger = new PointF(event.getX(), event.getY());
				scroll(arg0, oldFirstFinger.x - firstFinger.x);
				plot.setDomainBoundaries(plot.minXY.x, plot.maxXY.x,
						BoundaryMode.FIXED);

				series.setDomain((int) plot.minXY.x,
						(int) (plot.maxXY.x - plot.minXY.x));

				this.drawWave((int) plot.minXY.x, (int) plot.maxXY.x);

			} else if (mode == TWO_FINGERS_DRAG) {
				// float oldDist = distBetweenFingers;
				// distBetweenFingers = spacing(event);
				// zoom(arg0,oldDist / distBetweenFingers);
				// plot.setDomainBoundaries(plot.minXY.x, plot.maxXY.x,
				// BoundaryMode.FIXED);
				// series.setDomain((int)plot.minXY.x,
				// (int)(plot.maxXY.x-plot.minXY.x));
				//
				// plot.redraw();
			}
			break;
		}
		return true;
	}

	private void scroll(View arg0, float pan) {
		XYPlot plot = (XYPlot) arg0.findViewById(R.id.ecgPlot);
		// XYPlot plot = (XYPlot)arg0;
		float domainSpan = plot.maxXY.x - plot.minXY.x;
		Log.d("DEBUG", "domainSpan " + domainSpan);
		float step = domainSpan / plot.getWidth();
		Log.d("DEBUG", "step " + step);
		float offset = pan * step;
		Log.d("DEBUG", "offset " + offset);
		plot.minXY.x = plot.minXY.x + offset;
		plot.maxXY.x = plot.maxXY.x + offset;
		clampToDomainBounds(arg0, domainSpan);
		Log.d("DEBUG", "after clampToDomainBounds MinX" + plot.minXY.x
				+ "MaxX " + plot.maxXY.x);
	}

	private void clampToDomainBounds(View arg0, float domainSpan) {
		XYPlot plot = (XYPlot) arg0.findViewById(R.id.ecgPlot);
		// XYPlot plot = (XYPlot)arg0;
		SampleDynamicSeries series;
		Set<XYSeries> se = plot.getSeriesSet();
		Iterator<XYSeries> it = se.iterator();
		series = (SampleDynamicSeries) it.next();

		float leftBoundary = 0;
		float rightBoundary = SAMPLES_XSERIES;
		// enforce left scroll boundary:
		if (plot.minXY.x < leftBoundary) {
			plot.minXY.x = leftBoundary;
			plot.maxXY.x = leftBoundary + domainSpan;
		} else if (plot.maxXY.x > rightBoundary) {
			// plot.maxXY.x = rightBoundary;
			// plot.minXY.x = rightBoundary - domainSpan;
		}
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	class SampleDynamicSeries extends SimpleXYSeries {
		private List<? extends Number> samples;
		private int seriesIndex;
		private String title;

		private int domainLeft = 0, domainRight = 0,
				domainSize = SAMPLES_XSERIES;
		private ConcurrentHashMap<Integer, Short> indexMap = new ConcurrentHashMap<Integer, Short>();

		public SampleDynamicSeries(List<? extends Number> model,
				ArrayFormat format, String title) {
			super(model, format, title);

		}

		void setDomain(int left, int size) {
			domainLeft = left;
			domainRight = left + size;
			domainSize = size;
		}

		synchronized void refreshData(final int startPos, final int length) {
			// AnimationDrawable startAnimation = (AnimationDrawable)
			// loadingSample
			// .getBackground();
			// startAnimation.start();
			// mViewSwitcher.showNext();

			isLoading = true;
			int newStart = startPos - startPos % 2;
			int newLength = length + length % 2;
			HeartBeatData data;
			try {
				// Thread.sleep(3000);

				data = ApiClient.getRawData(appContext, record.getId(),
						newStart,
						newStart + newLength);
			} catch (Exception e) {
				data = null;
			}

			if (data != null) {
				Object[] arr = data.getData();
				if (arr != null) {
					for (int i = 0; i < arr.length; i++) {
						Object[] o = (Object[]) arr[i];
						indexMap.put(((Number) o[0]).intValue(),
								((Number) o[0]).shortValue());
					}
				}
			}
			isLoading = false;

		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public Number getX(int index) {
			return index + domainLeft;
		}

		@Override
		public int size() {
			return domainSize;
		}

		@Override
		public Number getY(int index) {
			int newPos = index + domainLeft;
			// if (indexMap.get(newPos) == null) { // to use an even start and
			// // length
			// refreshData(newPos, SAMPLES_XSERIES * 2);
			// }
			Number ret = 0;
			if (indexMap.get(newPos) != null)
				ret = indexMap.get(newPos);
			return ret;

			// return samples.get(index);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		// 结束Activity&从堆栈中移除
		AppManager.getAppManager().finishActivity(this);
	}
	
	@Override
	public void finish(){
		super.finish();
		overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
	}

	private View.OnClickListener refreshClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			// loadRecordInfoThread(true);
		}
	};
	
	private View.OnClickListener backListener=new View.OnClickListener() {		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			finish();
		}
	};
	
	private View.OnClickListener homeListener=new View.OnClickListener() {
		
		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			while(!(AppManager.getAppManager().currentActivity() instanceof Main)){
				AppManager.getAppManager().finishActivity();
			}
		}
	};

}
