package com.healthme.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.healthme.app.AppException;


public class PVCInfoList extends Entity {

	public final static int CATALOG_LASTEST = 0;
	public final static int CATALOG_HOT = -1;

	public int pageSize;
	public int recordCount;
	public List<PVCItem> defectList = new ArrayList<PVCItem>();

	public int getPageSize() {
		return pageSize;
	}

	public int getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(int recordCount) {
		this.recordCount = recordCount;
	}

	public List<PVCItem> getDefectList() {
		return defectList;
	}

	public void setDefectList(List<PVCItem> defectList) {
		this.defectList = defectList;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public static PVCInfoList parse(String body)
			throws IOException, AppException {
		Gson gson = new GsonBuilder()
		.registerTypeAdapter(java.util.Date.class,new UtilDateDeserializer())
		.setDateFormat(DateFormat.LONG).create();
		PVCInfoList recordList1 = gson.fromJson(body, PVCInfoList.class);
		return recordList1;
	}
}
