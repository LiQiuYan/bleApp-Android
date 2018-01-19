package com.healthme.app.bean;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import android.app.TabActivity;

import com.healthme.app.AppException;


public class EcgRecord extends Entity{

	public static enum STATUS{INIT,MONITOR,UPLOADING,ANALYZING,FINISH};
	
	private int sampleRate=128;

	private Date startTime;
	private Date endTime;
	private String groupId;
	private String status;
	
	private Integer averageHeartbeat;
	private Integer minHeartbeat;
	private Integer maxHeartbeat;	
	private Integer totalBeatNumber;
	private Integer totalPvcNumber;
	private Integer totalSvpbNumber;
	private Integer pauseNumber;
	private Integer afibNumber;	
	private Integer longRRNumber;
	private Integer curRRPieceInterval;
	
	private Integer pvc1Number;
	private Integer pvc2Number;
	private Integer pvc3Number;
	
	private Integer svpb1Number; //室上性早博
	private Integer svpb2Number;
	private Integer svpb3Number;

	public Date getStartTime() {
		return startTime;
	}



	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}



	public Date getEndTime() {
		return endTime;
	}



	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}



	public String getGroupId() {
		return groupId;
	}



	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}



	public String getStatus() {
		return status;
	}



	public void setStatus(String status) {
		this.status = status;
	}



	public Integer getAverageHeartbeat() {
		return averageHeartbeat;
	}



	public void setAverageHeartbeat(Integer averageHeartbeat) {
		this.averageHeartbeat = averageHeartbeat;
	}



	public Integer getMinHeartbeat() {
		return minHeartbeat;
	}



	public void setMinHeartbeat(Integer minHeartbeat) {
		this.minHeartbeat = minHeartbeat;
	}



	public Integer getMaxHeartbeat() {
		return maxHeartbeat;
	}



	public void setMaxHeartbeat(Integer maxHeartbeat) {
		this.maxHeartbeat = maxHeartbeat;
	}



	public Integer getTotalBeatNumber() {
		return totalBeatNumber;
	}



	public void setTotalBeatNumber(Integer totalBeatNumber) {
		this.totalBeatNumber = totalBeatNumber;
	}



	public Integer getTotalPvcNumber() {
		return totalPvcNumber;
	}



	public void setTotalPvcNumber(Integer totalPvcNumber) {
		this.totalPvcNumber = totalPvcNumber;
	}



	public Integer getTotalSvpbNumber() {
		return totalSvpbNumber;
	}



	public void setTotalSvpbNumber(Integer totalSvpbNumber) {
		this.totalSvpbNumber = totalSvpbNumber;
	}



	public Integer getPauseNumber() {
		return pauseNumber;
	}



	public void setPauseNumber(Integer pauseNumber) {
		this.pauseNumber = pauseNumber;
	}



	public Integer getAfibNumber() {
		return afibNumber;
	}



	public void setAfibNumber(Integer afibNumber) {
		this.afibNumber = afibNumber;
	}
	
	


	public int getSampleRate() {
		return sampleRate;
	}



	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}


	public Integer getLongRRNumber() {
		return longRRNumber;
	}



	public void setLongRRNumber(Integer longRRNumber) {
		this.longRRNumber = longRRNumber;
	}


	public Integer getCurRRPieceInterval() {
		return curRRPieceInterval;
	}



	public void setCurRRPieceInterval(Integer curRRPieceInterval) {
		this.curRRPieceInterval = curRRPieceInterval;
	}
	
	



	public Integer getPvc1Number() {
		return pvc1Number;
	}



	public void setPvc1Number(Integer pvc1Number) {
		this.pvc1Number = pvc1Number;
	}



	public Integer getPvc2Number() {
		return pvc2Number;
	}



	public void setPvc2Number(Integer pvc2Number) {
		this.pvc2Number = pvc2Number;
	}



	public Integer getPvc3Number() {
		return pvc3Number;
	}



	public void setPvc3Number(Integer pvc3Number) {
		this.pvc3Number = pvc3Number;
	}



	public Integer getSvpb1Number() {
		return svpb1Number;
	}



	public void setSvpb1Number(Integer svpb1Number) {
		this.svpb1Number = svpb1Number;
	}



	public Integer getSvpb2Number() {
		return svpb2Number;
	}



	public void setSvpb2Number(Integer svpb2Number) {
		this.svpb2Number = svpb2Number;
	}



	public Integer getSvpb3Number() {
		return svpb3Number;
	}



	public void setSvpb3Number(Integer svpb3Number) {
		this.svpb3Number = svpb3Number;
	}



	@Deprecated
	public static EcgRecord parse(InputStream inputStream) throws IOException, AppException {
		EcgRecord record = null;
		//TODO parse object here
        return record;       
	}



	@Override
	public String toString() {
		return "EcgRecord [sampleRate=" + sampleRate + ", startTime="
				+ startTime + ", endTime=" + endTime + ", groupId=" + groupId
				+ ", status=" + status + ", averageHeartbeat="
				+ averageHeartbeat + ", minHeartbeat=" + minHeartbeat
				+ ", maxHeartbeat=" + maxHeartbeat + ", totalBeatNumber="
				+ totalBeatNumber + ", totalPvcNumber=" + totalPvcNumber
				+ ", totalSvpbNumber=" + totalSvpbNumber + ", pauseNumber="
				+ pauseNumber + ", afibNumber=" + afibNumber
				+ ", longRRNumber=" + longRRNumber + ", curRRPieceInterval="
				+ curRRPieceInterval + ", pvc1Number=" + pvc1Number
				+ ", pvc2Number=" + pvc2Number + ", pvc3Number=" + pvc3Number
				+ ", svpb1Number=" + svpb1Number + ", svpb2Number="
				+ svpb2Number + ", svpb3Number=" + svpb3Number + "]";
	}
}
