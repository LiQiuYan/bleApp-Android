package com.healthme.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;

import com.healthme.app.R;

public class DatePickerActivity extends Activity {
	private Button button = null;
	private DatePicker datePicker = null;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.datepicker);
        button = (Button)findViewById(R.id.mybutton);
        datePicker  = (DatePicker)findViewById(R.id.myDatePicker);
        button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("type", 1);
				intent.putExtra("year",datePicker.getYear()+"");
				intent.putExtra("month",datePicker.getMonth()+"");
				intent.putExtra("day",datePicker.getDayOfMonth()+"");
				DatePickerActivity.this.setResult(1000, intent);
				finish();//必须手动finish
			}
		});
	}
}