/*
 * Copyright 2013 AndroidPlot.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.androidplot.demos;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Bundle;
import com.androidplot.Plot;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.*;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import com.healthme.app.R;

public class DynamicXYPlotActivity extends Activity {

    // redraws a plot whenever an update is received:
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

    private XYPlot dynamicPlot;
    private MyPlotUpdater plotUpdater;
    public SampleDynamicXYDatasource data;
    private Thread myThread;
    int FRESH_RATE=200;//ms
    int SAMPLE_RATE=200;
	private Shader WHITE_SHADER = new LinearGradient(1, 1, 1, 1, Color.WHITE, Color.WHITE, Shader.TileMode.REPEAT);
    

    @Override
    public void onCreate(Bundle savedInstanceState) {

        // android boilerplate stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ecg_main);

        // get handles to our View defined in layout.xml:
//        dynamicPlot = (XYPlot) findViewById(R.id.ecgSimpleXYPlot);

        plotUpdater = new MyPlotUpdater(dynamicPlot);

        // only display whole numbers in domain labels
        dynamicPlot.getGraphWidget().setDomainValueFormat(new DecimalFormat("0"));

        // getInstance and position datasets:
        data = new SampleDynamicXYDatasource();
//        SampleDynamicSeries sine1Series = new SampleDynamicSeries(data, 0, "Sine 1");
        SampleDynamicSeries sine2Series = new SampleDynamicSeries(data, 1, "Sine 2");

//        LineAndPointFormatter formatter1 = new LineAndPointFormatter(
//                                Color.rgb(0, 0, 0), null, null, null);
//        formatter1.getLinePaint().setStrokeJoin(Paint.Join.ROUND);
//        formatter1.getLinePaint().setStrokeWidth(10);
//        dynamicPlot.addSeries(sine1Series,
//                formatter1);

        LineAndPointFormatter formatter2 =
                new LineAndPointFormatter(Color.rgb(0, 0, 200), null, null, null);
        formatter2.getLinePaint().setStrokeWidth(2);
        formatter2.getLinePaint().setStrokeJoin(Paint.Join.ROUND);

        //formatter2.getFillPaint().setAlpha(220);
        dynamicPlot.addSeries(sine2Series, formatter2);

        // hook up the plotUpdater to the data model:
        data.addObserver(plotUpdater);

        // thin out domain tick labels so they dont overlap each other:
        dynamicPlot.setDomainStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setDomainStepValue(SAMPLE_RATE/(5*5));
        Paint rangeGridLinePaint = new Paint();
        //FF1493
        rangeGridLinePaint.setColor(Color.rgb(0x0,0x0,0x0));//180, 0, 0));
        rangeGridLinePaint.setAntiAlias(true);
        rangeGridLinePaint.setStrokeWidth(2);        
        Paint rangeSubGridLinePaint = new Paint();
        //FF1493
        rangeSubGridLinePaint.setColor(Color.rgb(0xff,0x14,0x93));//180, 0, 0));
        rangeSubGridLinePaint.setAntiAlias(true);        //rangeGridLinePaint.setStyle(Paint.Style.FILL);     
        rangeSubGridLinePaint.setStrokeWidth(1);
        dynamicPlot.getGraphWidget().setDomainGridLinePaint(rangeGridLinePaint);        
        dynamicPlot.getGraphWidget().setRangeGridLinePaint(rangeGridLinePaint);        
        dynamicPlot.getGraphWidget().setDomainSubGridLinePaint(rangeSubGridLinePaint);        
        dynamicPlot.setTicksPerDomainLabel(5);
        dynamicPlot.setRangeStepMode(XYStepMode.INCREMENT_BY_VAL);
        dynamicPlot.setRangeStepValue(10);
        dynamicPlot.setTicksPerRangeLabel(5);
        dynamicPlot.getGraphWidget().setRangeSubGridLinePaint(rangeSubGridLinePaint);        
        
        dynamicPlot.setRangeValueFormat(new DecimalFormat("###.#"));
        dynamicPlot.setDomainValueFormat(new DecimalFormat("####.#"));
        dynamicPlot.setDomainValueFormat(new Format() {

            // create a simple date format that draws on the year portion of our timestamp.
            // see http://download.oracle.com/javase/1.4.2/docs/api/java/text/SimpleDateFormat.html
            // for a full description of SimpleDateFormat.
            private SimpleDateFormat dateFormat = new SimpleDateFormat("dd HH:mm:ss");

            @Override
            public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {

                // because our timestamps are in seconds and SimpleDateFormat expects milliseconds
                // we multiply our timestamp by 1000:
//                long timestamp = ((Number) obj).longValue() * 1000;
//                Date date = new Date(timestamp);
                toAppendTo.append(((Number) obj).doubleValue()/(SAMPLE_RATE));
                return toAppendTo;
                //return dateFormat.format(date, toAppendTo, pos);
            }

            @Override
            public Object parseObject(String source, ParsePosition pos) {
                return null;

            }
        });
        // uncomment this line to freeze the range boundaries:
        dynamicPlot.setRangeBoundaries(-100, 100, BoundaryMode.FIXED);

        // create a dash effect for domain and range grid lines:
        DashPathEffect dashFx = new DashPathEffect(
                new float[] {PixelUtils.dpToPix(3), PixelUtils.dpToPix(3)}, 0);
        dynamicPlot.getGraphWidget().getDomainSubGridLinePaint().setPathEffect(dashFx);
        dynamicPlot.getGraphWidget().getRangeSubGridLinePaint().setPathEffect(dashFx);
        
        dynamicPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setShader(WHITE_SHADER);
        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.BLACK);
//        dynamicPlot.getGraphWidget().getDomainGridLinePaint().setPathEffect(new DashPathEffect(new float[]{3, 3}, 1));
        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setColor(Color.BLACK);
//        dynamicPlot.getGraphWidget().getRangeGridLinePaint().setPathEffect(new DashPathEffect(new float[]{3, 3}, 1));
        dynamicPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.BLACK);
        dynamicPlot.getGraphWidget().getRangeOriginLinePaint().setColor(Color.BLACK);        
//        RectF rect = dynamicPlot.getGraphWidget().getGridRect();
//        BitmapShader myShader = new BitmapShader(
//                Bitmap.createScaledBitmap(
//                        BitmapFactory.decodeResource(
//                                getResources(),
//                                R.drawable.graph_background),
//                        1,
//                        (int) rect.height(),
//                        false),
//                Shader.TileMode.REPEAT,
//                Shader.TileMode.REPEAT);
//        Matrix m = new Matrix();
//        m.setTranslate(rect.left, rect.top);
//        myShader.setLocalMatrix(m);
//
//        dynamicPlot.getGraphWidget().getGridBackgroundPaint().setShader(
//	        		myShader);


    }

    @Override
    public void onResume() {
        // kick off the data generating thread:
        myThread = new Thread(data);
        myThread.start();
        super.onResume();
    }

    @Override
    public void onPause() {
        data.stopThread();
        super.onPause();
    }

    public class SampleDynamicXYDatasource implements Runnable {

        // encapsulates management of the observers watching this datasource for update events:
        class MyObservable extends Observable {
            @Override
            public void notifyObservers() {
                setChanged();
                super.notifyObservers();
            }
        }

        private static final double FREQUENCY = 60; // larger is lower frequency
        private static final int MAX_AMP_SEED = 100;
        private static final int MIN_AMP_SEED = 10;
        private static final int AMP_STEP = 1;
        public static final int SINE1 = 0;
        public static final int SINE2 = 1;
        private static final int SAMPLE_SIZE = 400;
        private int phase = 0;
        private int sinAmp = 1;
        private MyObservable notifier;
        private boolean keepRunning = false;
  //      private Number internalData[];
        private List<Short> internalData = new ArrayList<Short>();
        
        {
            notifier = new MyObservable();

//            internalData = new Number[SAMPLE_SIZE];
//            Random r = new Random();
//            for (int i=0;i<SAMPLE_SIZE;i++)
//            {
//            	internalData[i]=r.nextInt(50)-25;
//// if there is no data, we can set  the value to zero.            	
////            	if (i>100)
////                	internalData[i]=0;
//            		
//            }
                        
        }

        public void updateData( List<Short> dataList) {
            notifier = new MyObservable();

            internalData.addAll(dataList);	
          	
        }
        
        public void stopThread() {
            keepRunning = false;
        }

        //@Override
        public void run() {
            try {
                keepRunning = true;
                boolean isRising = true;
                while (keepRunning) {

                    Thread.sleep(FRESH_RATE); // decrease or remove to speed up the refresh rate.
                    phase++;
                    if (sinAmp >= MAX_AMP_SEED) {
                        isRising = false;
                    } else if (sinAmp <= MIN_AMP_SEED) {
                        isRising = true;
                    }

                    if (isRising) {
                        sinAmp += AMP_STEP;
                    } else {
                        sinAmp -= AMP_STEP;
                    }
                    notifier.notifyObservers();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public int getItemCount(int series) {
            return SAMPLE_SIZE;
        }

        public Number getX(int series, int index) {
            if (index >= SAMPLE_SIZE) {
                throw new IllegalArgumentException();
            }
            //return 1;
            return index+phase*SAMPLE_RATE*FRESH_RATE/1000;
           // return index;
        }

        public Number getY(int series, int index) {
            if (index >= SAMPLE_SIZE) {
                throw new IllegalArgumentException();
            }
          double angle = (index + (phase*SAMPLE_RATE))/FREQUENCY;
 //           double angle = (index)/FREQUENCY;
            double amp = sinAmp * Math.sin(angle);
            switch (series) {
//                case SINE1:
//                    return amp;
//                case SINE2:
//                    return -amp;
          case SINE1:
          return amp;
          case SINE2:
//          return internalData[(index+phase*SAMPLE_RATE*FRESH_RATE/1000)%SAMPLE_SIZE];  
          return internalData.get((index+phase*SAMPLE_RATE*FRESH_RATE/1000)%SAMPLE_SIZE);
                default:
                    throw new IllegalArgumentException();
            }
        }

        public void addObserver(Observer observer) {
            notifier.addObserver(observer);
        }

        public void removeObserver(Observer observer) {
            notifier.deleteObserver(observer);
        }

    }

    class SampleDynamicSeries implements XYSeries {
        private SampleDynamicXYDatasource datasource;
        private int seriesIndex;
        private String title;

        public SampleDynamicSeries(SampleDynamicXYDatasource datasource, int seriesIndex, String title) {
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
}
