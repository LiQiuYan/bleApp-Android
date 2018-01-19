package com.healthme.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.healthme.app.api.ApiClient;
import com.healthme.app.bean.BluetoothDeviceList;
import com.healthme.app.bean.CustomizedBluetoothDevice;
import com.healthme.app.bean.ECGClassificationList;
import com.healthme.app.bean.EcgRecord;
import com.healthme.app.bean.EcgRecordList;
import com.healthme.app.bean.Hmessage;
import com.healthme.app.bean.Notice;
import com.healthme.app.bean.Relative;
import com.healthme.app.bean.Result;
import com.healthme.app.bean.User;
import com.healthme.app.common.GpsTask.GpsData;
import com.healthme.app.common.ImageUtils;
import com.healthme.app.common.MethodsCompat;
import com.healthme.app.common.StringUtils;
import com.healthme.app.common.UIHelper;
import com.healthme.app.service.StompClient;
import com.mycompany.common.util.ShortOutput;

import de.mindpipe.android.logging.log4j.LogConfigurator;

//import android.webkit.CacheManager;

/**
 * 全局应用程序类：用于保存和调用全�?��用配置及访问网络数据
 * 
 * @author ecg team (http://healthme.com.cn)
 * @version 1.0
 * @created 2012-3-21
 */
public class AppContext extends Application {

	public static final int NETTYPE_WIFI = 0x01;
	public static final int NETTYPE_CMWAP = 0x02;
	public static final int NETTYPE_CMNET = 0x03;

	public static final int PAGE_SIZE = 10;// 默认分页大小
	private static final int CACHE_TIME = 60 * 60000;// 缓存失效时间
	
//	private static int SCAN_TIME = 10000; // 扫描的时间为10秒

	private boolean login = false; // 登录状�?
	private int loginUid = 0; // 登录用户的id
	private GpsData curGpsData=null;
	private Hashtable<String, Object> memCacheRegion = new Hashtable<String, Object>();
	private SimpleDateFormat formater = new SimpleDateFormat("yyyyMM-dd");
	private String saveImagePath;// 保存图片路径
	private static String ECG_RAW_PATH="/ecgRaw";
//	private CustomizedBluetoothDevice mblueDevice;
	
	private final static String KEY_BLUETOOTH_LAST = "bluetooth4.0_last_0";
	private final static String KEY_HMESSAGE_NEW = "stomp_message_0";
	
	private final String TAG = getClass().getSimpleName();
	
//	private Handler handler = null;
//	private boolean isScanning = false; // 是否正在扫描

	public static BluetoothAdapter bleAdapter = null;
//	private RFStarManageListener bleScanListener = null;

//	private ArrayList<BluetoothDevice> scanBlueDeviceArray = new ArrayList<BluetoothDevice>(); // 扫描到的数据

	private CustomizedBluetoothDevice btDevice;
//	private CubicBLEDevice cubicBLEDevice = null; // 选中的cubicBLEDevice
	
//	private boolean autoConnectBle;
	

	private Handler unLoginHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				UIHelper.ToastMessage(AppContext.this,
						getString(R.string.msg_login_error));
				UIHelper.showLoginDialog(AppContext.this);
			}
		}
	};
	
	private Handler networkErrHandler = new Handler() {
		public void handleMessage(Message msg) {
				UIHelper.ToastMessage(AppContext.this,
						getString(R.string.http_exception_error));
		}
	};

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate");
		super.onCreate();
		// 注册App异常崩溃处理�?
		Thread.setDefaultUncaughtExceptionHandler(AppException
				.getAppExceptionHandler());

		init();
//		System.out.println("INIT APP CONTEXT!");
	}
	@Override
	public void onTerminate(){
		Log.i(TAG, "onTerminate");
		super.onTerminate();
	}

	/**
	 * 初始�?
	 */
	private void init() {
		
//		handler = new Handler();
		BluetoothManager manager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
		bleAdapter = manager.getAdapter();
		// 设置保存图片的路�?
		saveImagePath = getProperty(AppConfig.SAVE_IMAGE_PATH);
		if (StringUtils.isEmpty(saveImagePath)) {
			setProperty(AppConfig.SAVE_IMAGE_PATH,
					AppConfig.DEFAULT_SAVE_IMAGE_PATH);
			saveImagePath = AppConfig.DEFAULT_SAVE_IMAGE_PATH;
		}
		
		LogConfigurator logConfigurator = new LogConfigurator();
        logConfigurator.setFileName(Environment.getExternalStorageDirectory()
                        + File.separator + "MyApp" + File.separator + "logs"
                        + File.separator + "log4j.txt");
        logConfigurator.setRootLevel(Level.DEBUG);
        logConfigurator.setLevel("org.apache", Level.ERROR);
        logConfigurator.setLevel("net.oschina", Level.DEBUG);
        logConfigurator.setFilePattern("%d %-5p [%c{2}]-[%L] %m%n");
        logConfigurator.setMaxFileSize(1024 * 1024 * 5);
        logConfigurator.setImmediateFlush(true);
        logConfigurator.configure();
        Logger log = Logger.getLogger(AppContext.class);
        log.info("My Application Created");
		
		btDevice = null;
	}

	/**
	 * �?��当前系统声音是否为正常模�?
	 * 
	 * @return
	 */
	public boolean isAudioNormal() {
		AudioManager mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		return mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
	}

	/**
	 * 应用程序是否发出提示�?
	 * 
	 * @return
	 */
	public boolean isAppSound() {
		return isAudioNormal() && isVoice();
	}

	/**
	 * �?��网络是否可用
	 * 
	 * @return
	 */
	public boolean isNetworkConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = cm.getActiveNetworkInfo();
		return ni != null && ni.isConnectedOrConnecting();
	}

	/**
	 * 获取当前网络类型
	 * 
	 * @return 0：没有网�?1：WIFI网络 2：WAP网络 3：NET网络
	 */
	public int getNetworkType() {
		int netType = 0;
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		if (networkInfo == null) {
			return netType;
		}
		int nType = networkInfo.getType();
		if (nType == ConnectivityManager.TYPE_MOBILE) {
			String extraInfo = networkInfo.getExtraInfo();
			if (!StringUtils.isEmpty(extraInfo)) {
				if (extraInfo.toLowerCase().equals("cmnet")) {
					netType = NETTYPE_CMNET;
				} else {
					netType = NETTYPE_CMWAP;
				}
			}
		} else if (nType == ConnectivityManager.TYPE_WIFI) {
			netType = NETTYPE_WIFI;
		}
		return netType;
	}

	/**
	 * 判断当前版本是否兼容目标版本的方�?
	 * 
	 * @param VersionCode
	 * @return
	 */
	public static boolean isMethodsCompat(int VersionCode) {
		int currentVersion = android.os.Build.VERSION.SDK_INT;
		return currentVersion >= VersionCode;
	}

	/**
	 * 获取App安装包信�?
	 * 
	 * @return
	 */
	public PackageInfo getPackageInfo() {
		PackageInfo info = null;
		try {
			info = getPackageManager().getPackageInfo(getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace(System.err);
		}
		if (info == null)
			info = new PackageInfo();
		return info;
	}

	/**
	 * 获取App唯一标识
	 * 
	 * @return
	 */
	public String getAppId() {
		String uniqueID = getProperty(AppConfig.CONF_APP_UNIQUEID);
		if (StringUtils.isEmpty(uniqueID)) {
			uniqueID = UUID.randomUUID().toString();
			setProperty(AppConfig.CONF_APP_UNIQUEID, uniqueID);
		}
		return uniqueID;
	}
	
//	private static Lock scanLock=new ReentrantLock();
//	/**
//	 * 扫描蓝牙设备
//	 */
//	public boolean startScanBluetoothDevice(final RFStarManageListener mlistener,boolean force) {
//		if(force){
//			stopScanBluetoothDevice();
//		}
//		
//		scanLock.lock();
//		if(isScanning){
//			scanLock.unlock();
//			return false;
//		}
//		isScanning = true;
//		scanLock.unlock();
//		Log.i(TAG,"startScanBluetoothDevice");
//		this.bleScanListener=mlistener;
//		scanBlueDeviceArray.clear();
//		handler.postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				// TODO Auto-generated method stub
//				stopScanBluetoothDevice();
//			}
//		}, SCAN_TIME); // 10秒后停止扫描
//		bleAdapter.startLeScan(bleScanCallback);
//		bleScanListener.RFstarBLEManageStartScan();
//		return true;
//	}
//	
//	/**
//	 * 停止扫描蓝牙设备
//	 */
//	public void stopScanBluetoothDevice() {
//		if (isScanning) {
//			isScanning = false;
//			bleAdapter.stopLeScan(bleScanCallback);
//			bleScanListener.RFstarBLEManageStopScan();
//		}
//		isScanning = false;
//	}
//	
////	/**
////	 * 每扫描到一个蓝牙设备调用一次
////	 * 
////	 * @param listener
////	 */
////	public void setRFstarBLEManagerListener(RFStarManageListener listener) {
////		this.listener = listener;
////	}
//	
//	// Device scan callback.
//	private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
//		@Override
//		public void onLeScan(final BluetoothDevice device, final int rssi,
//				final byte[] scanRecord) {
//			// TODO 添加扫描到的device，并刷新数据
//			handler.post(new Runnable() {
//				@Override
//				public void run() {
//					// TODO Auto-generated method stub
//
//					if (!scanBlueDeviceArray.contains(device)) {
//						scanBlueDeviceArray.add(device);
//
//						bleScanListener.RFstarBLEManageListener(device, rssi,
//								scanRecord);
//					}
//				}
//			});
//		}
//	};

	/**
	 * 用户是否登录
	 * 
	 * @return
	 */
	public boolean isLogin() {
		return login;
	}

	/**
	 * 获取登录用户id
	 * 
	 * @return
	 */
	public int getLoginUid() {
		return this.loginUid;
	}

	/**
	 * 用户注销
	 */
	public void Logout() {
		ApiClient.cleanCookie(this);
		this.cleanCookie();
		this.login = false;
//		System.out.println(new Exception());
		this.loginUid = 0;
	}

	/**
	 * 未登录或修改密码后的处理
	 */
	public Handler getUnLoginHandler() {
		return this.unLoginHandler;
	}

	/**
	 * 初始化用户登录信�?
	 */
	public void initLoginInfo() {
		User loginUser = getLoginInfo();
		if (loginUser != null && loginUser.getUid() > 0
				&& loginUser.isRememberMe()) {
			this.loginUid = loginUser.getUid();
			this.login = true;
		} else {
			this.Logout();
		}
	}

	/**
	 * 用户登录验证
	 * 
	 * @param account
	 * @param pwd
	 * @return
	 * @throws AppException
	 */
	public User loginVerify(String account, String pwd) throws AppException {
		return ApiClient.login(this, account, pwd);
	}

	
	public Map<String, String> checkRegisterParam(Map<String, Object> params) throws AppException{
		return ApiClient.checkParam(params, this);
	}
	
	public User register(Map<String, Object> params) throws AppException{
		return ApiClient.register(params, this);
	}
	
	public User changePassword(Map<String, Object> params) throws AppException{
		return ApiClient.changePassword(params, this);
	}	




//	/**
//	 * 更新用户头像
//	 * 
//	 * @param portrait
//	 *            新上传的头像
//	 * @return
//	 * @throws AppException
//	 */
//	public Result updatePortrait(File portrait) throws AppException {
//		return ApiClient.updatePortrait(this, loginUid, portrait);
//	}

	/**
	 * 清空通知消息
	 * 
	 * @param uid
	 * @param type
	 *            1:@我的信息 2:未读消息 3:评论个数 4:新粉丝个�?
	 * @return
	 * @throws AppException
	 */
	public Result noticeClear(int uid, int type) throws AppException {
		return ApiClient.noticeClear(this, uid, type);
	}

	/**
	 * 获取用户通知信息
	 * 
	 * @param uid
	 * @return
	 * @throws AppException
	 */
	public Notice getUserNotice(int uid) throws AppException {
		return ApiClient.getUserNotice(this, uid);
	}

//	/**
//	 * 用户收藏列表
//	 * 
//	 * @param type
//	 *            0:全部收藏 1:软件 2:话题 3:博客 4:新闻 5:代码
//	 * @param pageIndex
//	 *            页面索引 0表示第一�?
//	 * @return
//	 * @throws AppException
//	 */
//	public PVCInfoList getPVCInfoList(int pageIndex,
//			boolean isRefresh) throws AppException {
//		PVCInfoList list = null;
//		String key = "favoritelist_" + loginUid + "_" + type + "_" + pageIndex
//				+ "_" + PAGE_SIZE;
//		if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
//			try {
//				list = ApiClient.getPvcInfoList(this, loginUid, 
//						pageIndex, PAGE_SIZE);
//				if (list != null && pageIndex == 0) {
//					Notice notice = list.getNotice();
//					list.setNotice(null);
//					list.setCacheKey(key);
//					saveObject(list, key);
//					list.setNotice(notice);
//				}
//			} catch (AppException e) {
//				list = (PVCInfoList) readObject(key);
//				if (list == null)
//					throw e;
//			}
//		} else {
//			list = (FavoriteList) readObject(key);
//			if (list == null)
//				list = new FavoriteList();
//		}
//		return list;
//	}


	/**
	 * 帖子列表
	 * 
	 * @param catalog
	 * @param pageIndex
	 * @return
	 * @throws ApiException
	 */
	public BluetoothDeviceList getBluetoothDeviceList(int catalog, int pageIndex, boolean isRefresh)
			throws AppException {
		BluetoothDeviceList list = null;
		String key = "BluetoothDevicelist_" +  pageIndex + "_" + PAGE_SIZE;
		if ( isRefresh) {
			try {
				list = ApiClient.getBluetoothDeviceList(this, catalog, pageIndex,
						PAGE_SIZE);
				if (list != null && pageIndex == 0) {
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
//					saveObject(list, key);
					list.setNotice(notice);
				}
			} catch (AppException e) {
				list = (BluetoothDeviceList) readObject(key);
				if (list == null)
					throw e;
			}
		} else {
			list = (BluetoothDeviceList) readObject(key);
			if (list == null)
				list = new BluetoothDeviceList();
		}
		return list;
	}





	/**
	 * 动弹列表
	 * 
	 * @param catalog
	 *            -1 热门�? �?��，大�? 某用户的动弹(uid)
	 * @param pageIndex
	 * @return
	 * @throws AppException
	 */
	public EcgRecordList getRecordList(int pageIndex)
			throws AppException {
		if(!this.isLogin()){
			return new EcgRecordList();
		}
		return ApiClient.getRecordList(this, pageIndex,
						PAGE_SIZE);			
	}
	
	public ECGClassificationList getECGClassificationList(int recordId,short code,int pageIndex, boolean isRefresh)
			throws AppException {
		ECGClassificationList list = null;
		String key = "ecgclassificationlist_" + recordId + "_" + code + "_" + pageIndex + "_" + PAGE_SIZE;
		if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try {
				list = ApiClient.getECGClassificationList(this, recordId,code, pageIndex,
						PAGE_SIZE);
				if (list != null && pageIndex == 0) {
					Notice notice = list.getNotice();
					list.setNotice(null);
					list.setCacheKey(key);
					saveObject(list, key);
					list.setNotice(notice);
				}
			} catch (AppException e) {
				list = (ECGClassificationList) readObject(key);
				if (list == null)
					throw e;
			}
		} else {
			list = (ECGClassificationList) readObject(key);
			if (list == null)
				list = new ECGClassificationList();
		}
		return list;
	}

	/**
	 * 获取动弹详情
	 * 
	 * @param tweet_id
	 * @return
	 * @throws AppException
	 */
	public EcgRecord getRecordDetail(int id, boolean isRefresh) throws AppException {
		EcgRecord record = null;
		String key = "medicalrecord_" + id;
		if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
			try {
				record = ApiClient.getRecordDetail(this, id);
				if (record != null) {
					Notice notice = record.getNotice();
					record.setNotice(null);
					record.setCacheKey(key);
					saveObject(record, key);
					record.setNotice(notice);
				}
			} catch (AppException e) {
				record = (EcgRecord) readObject(key);
				if (record == null)
					throw e;
			}
		} else {
			record = (EcgRecord) readObject(key);
			if (record == null)
				record = new EcgRecord();
		}
		return record;
	}
	
	/**
	 * 保存登录信息
	 * 
	 * @param username
	 * @param pwd
	 */
	public void saveLoginInfo(final User user) {
		this.loginUid = user.getUid();
		this.login = true;
		setProperties(new Properties() {
			{
				setProperty("user.uid", String.valueOf(user.getUid()));
				setProperty("user.name", user.getName());
				setProperty("user.account", user.getAccount());
				setProperty("user.gender", user.getGender());
				setProperty("user.pwd",
						user.getPwd());
				setProperty("user.address", user.getAddress());
				setProperty("user.city", user.getCity());
				setProperty("user.province", user.getProvince());
				setProperty("user.country", user.getCountry());
				setProperty("user.postalCode", user.getPostalCode());
				setProperty("user.phoneNumber", user.getPhoneNumber());
				setProperty("user.birthday",
						formater.format(user.getBirthday()));
				setProperty("user.email", user.getEmail());
				setProperty("user.isRememberMe", String.valueOf(user.isRememberMe()));//是否记住我的信息
//				setProperty("relatives", XMLEnCodeder)
			}
		});
		saveRelatives(user.getRelatives());
	}
	

	/**
	 * 清除登录信息
	 */
	public void cleanLoginInfo() {
		this.loginUid = 0;
		this.login = false;
		removeProperty("user.uid", "user.name", "user.address", "user.city",
                "user.pwd", "user.province", "user.country", "user.postalCode",
                "user.phoneNumber", "user.birthday", "user.isRememberMe");
	}

	/**
	 * 获取登录信息
	 * 
	 * @return
	 */
	public User getLoginInfo() {
		User lu = new User();
		
		if (!isLogin()) {
			lu.setAccount(null);
			lu.setPwd(null);
			lu.setUid(0);
			lu.setName(null);
		} else {
			lu.setAccount(getProperty("user.account"));
			lu.setUid(StringUtils.toInt(getProperty("user.uid"), 0));
			lu.setName(getProperty("user.name"));
			lu.setPwd(getProperty("user.pwd"));
			lu.setAddress(getProperty("user.address"));
			lu.setCity(getProperty("user.city"));
			lu.setProvince(getProperty("user.province"));
			lu.setCountry(getProperty("user.country"));
			lu.setPostalCode(getProperty("user.postalCode"));
			lu.setGender(getProperty("user.gender"));
			lu.setPhoneNumber(getProperty("user.phoneNumber"));
			lu.setEmail(getProperty("user.email"));
			try {
				lu.setBirthday(formater.parse(getProperty("user.birthday")));
			} catch (Exception e) {
				Log.e(getClass().getName(), "failed to parse the date type");
				lu.setBirthday(new Date());
			}
		}
		lu.setRememberMe(StringUtils.toBool(getProperty("user.isRememberMe")));
		return lu;
	}
	/**
	 * 获取登录信息
	 * 
	 * @return
	 */
	public User getLastLoginInfo() {
		User lu = new User();
		

		lu.setAccount(getProperty("user.account"));
		lu.setUid(StringUtils.toInt(getProperty("user.uid"), 0));
		lu.setName(getProperty("user.name"));
		lu.setPwd(getProperty("user.pwd"));
		lu.setAddress(getProperty("user.address"));
		lu.setCity(getProperty("user.city"));
		lu.setProvince(getProperty("user.province"));
		lu.setCountry(getProperty("user.country"));
		lu.setPostalCode(getProperty("user.postalCode"));
		lu.setGender(getProperty("user.gender"));
		lu.setPhoneNumber(getProperty("user.phoneNumber"));
		lu.setEmail(getProperty("user.email"));
		try {
			lu.setBirthday(formater.parse(getProperty("user.birthday")));
		} catch (Exception e) {
			Log.e(getClass().getName(), "failed to parse the date type");
			lu.setBirthday(new Date());
		}

		lu.setRememberMe(StringUtils.toBool(getProperty("user.isRememberMe")));
		return lu;
	}

	/**
	 * 保存用户头像
	 * 
	 * @param fileName
	 * @param bitmap
	 */
	public void saveUserFace(String fileName, Bitmap bitmap) {
		try {
			ImageUtils.saveImage(this, fileName, bitmap);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 获取用户头像
	 * 
	 * @param key
	 * @return
	 * @throws AppException
	 */
	public Bitmap getUserFace(String key) throws AppException {
		FileInputStream fis = null;
		try {
			fis = openFileInput(key);
			return BitmapFactory.decodeStream(fis);
		} catch (Exception e) {
			throw AppException.run(e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 是否加载显示文章图片
	 * 
	 * @return
	 */
	public boolean isLoadImage() {
		String perf_loadimage = getProperty(AppConfig.CONF_LOAD_IMAGE);
		// 默认是加载的
		if (StringUtils.isEmpty(perf_loadimage))
			return true;
		else
			return StringUtils.toBool(perf_loadimage);
	}

	/**
	 * 设置是否加载文章图片
	 * 
	 * @param b
	 */
	public void setConfigLoadimage(boolean b) {
		setProperty(AppConfig.CONF_LOAD_IMAGE, String.valueOf(b));
	}

	/**
	 * 是否发出提示�?
	 * 
	 * @return
	 */
	public boolean isVoice() {
		String perf_voice = getProperty(AppConfig.CONF_VOICE);
		// 默认是开启提示声�?
		if (StringUtils.isEmpty(perf_voice))
			return true;
		else
			return StringUtils.toBool(perf_voice);
	}	
	/**
	 * 是否追踪?
	 * 
	 * @return
	 */
	public boolean isTrace() {
		String perf_trace = getProperty(AppConfig.CONF_TRACE);
		// 默认是开启提示声�?
		if ((StringUtils.isEmpty(perf_trace) || StringUtils.toBool(perf_trace)) && isGPSUp())
			return true;
		else
		return false;
	}
	
	/**
	 * 是否追踪?
	 * 
	 * @return
	 */
	public boolean isECGCache() {
		String perf_ecg = getProperty(AppConfig.CONF_ECG_CACHE);
		// 默认是开启提示声�?
		if ((StringUtils.isEmpty(perf_ecg) || StringUtils.toBool(perf_ecg)))
			return true;
		else
		return false;
	}
	/**
	 * 是否开启ＧＰＳ?
	 * 
	 * @return
	 */
	public boolean isGPSUp() {
		LocationManager loc = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (loc.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) 
			return true;
		 else
			return false;


	}
	/**
	 * 设置是否发出提示�?
	 * 
	 * @param b
	 */
	public void setConfigVoice(boolean b) {
		setProperty(AppConfig.CONF_VOICE, String.valueOf(b));
	}

	/**
	 * 设置是否发出提示�?
	 * 
	 * @param b
	 */
	public void setConfigECGCache(boolean b) {
		setProperty(AppConfig.CONF_ECG_CACHE, String.valueOf(b));
	}
	
	/**
	 * 设置是否启动追踪?
	 * 
	 * @param b
	 */
	public void setConfigTrace(boolean b) {
		setProperty(AppConfig.CONF_TRACE, String.valueOf(b));
	}
	/**
	 * 是否启动�?��更新
	 * 
	 * @return
	 */
	public boolean isCheckUp() {
		String perf_checkup = getProperty(AppConfig.CONF_CHECKUP);
		// 默认是开�?
		if (StringUtils.isEmpty(perf_checkup))
			return true;
		else
			return StringUtils.toBool(perf_checkup);
	}

	/**
	 * 设置启动�?��更新
	 * 
	 * @param b
	 */
	public void setConfigCheckUp(boolean b) {
		setProperty(AppConfig.CONF_CHECKUP, String.valueOf(b));
	}

	/**
	 * 是否左右滑动
	 * 
	 * @return
	 */
	public boolean isScroll() {
		String perf_scroll = getProperty(AppConfig.CONF_SCROLL);
		// 默认是关闭左右滑�?
		if (StringUtils.isEmpty(perf_scroll))
			return false;
		else
			return StringUtils.toBool(perf_scroll);
	}

	/**
	 * 设置是否左右滑动
	 * 
	 * @param b
	 */
	public void setConfigScroll(boolean b) {
		setProperty(AppConfig.CONF_SCROLL, String.valueOf(b));
	}

	/**
	 * 是否Https登录
	 * 
	 * @return
	 */
	public boolean isHttpsLogin() {
		String perf_httpslogin = getProperty(AppConfig.CONF_HTTPS_LOGIN);
		// 默认是http
		if (StringUtils.isEmpty(perf_httpslogin))
			return false;
		else
			return StringUtils.toBool(perf_httpslogin);
	}

	/**
	 * 设置是是否Https登录
	 * 
	 * @param b
	 */
	public void setConfigHttpsLogin(boolean b) {
		setProperty(AppConfig.CONF_HTTPS_LOGIN, String.valueOf(b));
	}

	/**
	 * 清除保存的缓�?
	 */
	public void cleanCookie() {
		removeProperty(AppConfig.CONF_COOKIE);
	}

	/**
	 * 判断缓存数据是否可读
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isReadDataCache(String cachefile) {
		return readObject(cachefile) != null;
	}

	/**
	 * 判断缓存是否存在
	 * 
	 * @param cachefile
	 * @return
	 */
	private boolean isExistDataCache(String cachefile) {
		boolean exist = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists())
			exist = true;
		return exist;
	}

	/**
	 * 判断缓存是否失效
	 * 
	 * @param cachefile
	 * @return
	 */
	public boolean isCacheDataFailure(String cachefile) {
		boolean failure = false;
		File data = getFileStreamPath(cachefile);
		if (data.exists()
				&& (System.currentTimeMillis() - data.lastModified()) > CACHE_TIME)
			failure = true;
		else if (!data.exists())
			failure = true;
		return failure;
	}

	/**
	 * 清除app缓存
	 */
	public void clearAppCache() {
		// 清除webview缓存
		// File file = CacheManager.getCacheFileBaseDir();
		// if (file != null && file.exists() && file.isDirectory()) {
		// for (File item : file.listFiles()) {
		// item.delete();
		// }
		// file.delete();
		// }
		deleteDatabase("webview.db");
		deleteDatabase("webview.db-shm");
		deleteDatabase("webview.db-wal");
		deleteDatabase("webviewCache.db");
		deleteDatabase("webviewCache.db-shm");
		deleteDatabase("webviewCache.db-wal");
		// 清除数据缓存
		clearCacheFolder(getFilesDir(), System.currentTimeMillis());
		clearCacheFolder(getCacheDir(), System.currentTimeMillis());
		// 2.2版本才有将应用缓存转移到sd卡的功能
		if (isMethodsCompat(android.os.Build.VERSION_CODES.FROYO)) {
			clearCacheFolder(MethodsCompat.getExternalCacheDir(this),
					System.currentTimeMillis());
		}
		// 清除编辑器保存的临时内容
		Properties props = getProperties();
		for (Object key : props.keySet()) {
			String _key = key.toString();
			if (_key.startsWith("temp"))
				removeProperty(_key);
		}
	}

	/**
	 * 清除缓存目录
	 * 
	 * @param dir
	 *            目录
	 * @param numDays
	 *            当前系统时间
	 * @return
	 */
	private int clearCacheFolder(File dir, long curTime) {
		int deletedFiles = 0;
		if (dir != null && dir.isDirectory()) {
			try {
				for (File child : dir.listFiles()) {
					if (child.isDirectory()) {
						deletedFiles += clearCacheFolder(child, curTime);
					}
					if (child.lastModified() < curTime) {
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}

	/**
	 * 将对象保存到内存缓存�?
	 * 
	 * @param key
	 * @param value
	 */
	public void setMemCache(String key, Object value) {
		memCacheRegion.put(key, value);
	}

	/**
	 * 从内存缓存中获取对象
	 * 
	 * @param key
	 * @return
	 */
	public Object getMemCache(String key) {
		return memCacheRegion.get(key);
	}

	/**
	 * 保存磁盘缓存
	 * 
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	public void setDiskCache(String key, String value) throws IOException {
		FileOutputStream fos = null;
		try {
			fos = openFileOutput("cache_" + key + ".data", Context.MODE_PRIVATE);
			fos.write(value.getBytes());
			fos.flush();
		} finally {
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 获取磁盘缓存数据
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	public String getDiskCache(String key) throws IOException {
		FileInputStream fis = null;
		try {
			fis = openFileInput("cache_" + key + ".data");
			byte[] datas = new byte[fis.available()];
			fis.read(datas);
			return new String(datas);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * 保存对象
	 * 
	 * @param ser
	 * @param file
	 * @throws IOException
	 */
	public boolean saveObject(Serializable ser, String file) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		try {
			fos = openFileOutput(file, MODE_PRIVATE);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(ser);
			oos.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				fos.close();
			} catch (Exception e) {
			}
		}
	}

	public ShortOutput openRawSampleFile(String fileName){
		
		FileOutputStream fos = null;
		try {
					 //获取SD卡的目录
			File sdCardDir = Environment.getExternalStorageDirectory();
					 
			File dir = new File(sdCardDir.getAbsolutePath()+ ECG_RAW_PATH);
			if(!dir.exists()||!dir.isDirectory())
				dir.mkdir();
			fos = new FileOutputStream(dir.getAbsolutePath()+fileName);
			ShortOutput so=new ShortOutput(fos);
			return so;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void writeRawSample(List<Short> data,ShortOutput so){
		Short[] samples = data.toArray(new Short[data.size()]);
			try {
				so.writeShorts(samples);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	/**
	 * 读取对象
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public Serializable readObject(String file) {
		if (!isExistDataCache(file))
			return null;
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		try {
			fis = openFileInput(file);
			ois = new ObjectInputStream(fis);
			return (Serializable) ois.readObject();
		} catch (FileNotFoundException e) {
		} catch (Exception e) {
			e.printStackTrace();
			// 反序列化失败 - 删除缓存文件
			if (e instanceof InvalidClassException) {
				File data = getFileStreamPath(file);
				data.delete();
			}
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return null;
	}

	public boolean containsProperty(String key) {
		Properties props = getProperties();
		return props.containsKey(key);
	}

	public void setProperties(Properties ps) {
		AppConfig.getAppConfig(this).set(ps);
	}

	public Properties getProperties() {
		return AppConfig.getAppConfig(this).get();
	}

	public void setProperty(String key, String value) {
		AppConfig.getAppConfig(this).set(key, value);
	}

	public String getProperty(String key) {
		return AppConfig.getAppConfig(this).get(key);
	}

	public void removeProperty(String... key) {
		AppConfig.getAppConfig(this).remove(key);
	}

	
	public ArrayList<Relative> getRelatives(){
//		return AppConfig.getRelatives();
		@SuppressWarnings("unchecked")
		ArrayList<Relative> l=(ArrayList<Relative>) readObject("relatives");
		return l;
	}
	
	public void saveRelatives(ArrayList<Relative> relativeList){
		saveObject(relativeList, "relatives");
//		AppConfig.setRelatives(relativeList);
	}
	/**
	 * 获取内存中保存图片的路径
	 * 
	 * @return
	 */
	public String getSaveImagePath() {
		return saveImagePath;
	}

	/**
	 * 设置内存中保存图片的路径
	 * 
	 * @return
	 */
	public void setSaveImagePath(String saveImagePath) {
		this.saveImagePath = saveImagePath;
	}

	public void setBluetoothDevice(CustomizedBluetoothDevice Cstmdevice){
		btDevice = Cstmdevice;
//		if(Cstmdevice.device!=null){
//			if(this.cubicBLEDevice!=null&&(!this.cubicBLEDevice.deviceMac.equals(btDevice.getAddress())||!this.cubicBLEDevice.isAlive()))
//				disconnectBluetoothDevice();
//			if(this.cubicBLEDevice==null)
//				this.cubicBLEDevice=new CubicBLEDevice(this.getApplicationContext(),Cstmdevice.device);
//			//sendBroadcast(new Intent(UIHelper.ACTION_REFRESHDEVICE));
//		}
		if(Cstmdevice!=null){			
			saveObject(btDevice,KEY_BLUETOOTH_LAST);
		}
	}
	
	public CustomizedBluetoothDevice getBluetoothDevice() {
		if(btDevice==null){
			CustomizedBluetoothDevice preDev=(CustomizedBluetoothDevice)readObject(KEY_BLUETOOTH_LAST);
			if(preDev!=null){
				btDevice=preDev;
			}
		}
		return btDevice;
	}
	
//	public CubicBLEDevice getBLEDevice(){
//		return this.cubicBLEDevice;
//	}
//
//    /**
//     * send message the BLE device
//     * @param message
//     */
//    public void sendBLEMessage(byte[] message){
//        cubicBLEDevice.sendBLEMessage(message);
//    }
//	public void disconnectBluetoothDevice(){
//		if(this.cubicBLEDevice!=null){
//			this.cubicBLEDevice.disconnectedDevice();
//		}
//		this.cubicBLEDevice=null;
//	}

//	public PVCInfoList getPVCInfoList(int recordId, int pageIndex, boolean isRefresh) throws AppException {
//		PVCInfoList list = null;
//		String key = "pvclist_" + recordId+ "_" + pageIndex + "_" + PAGE_SIZE;
//		if (isNetworkConnected() && (!isReadDataCache(key) || isRefresh)) {
//			try {
////				list = ApiClient.getRecordList(this, pageIndex,
////						PAGE_SIZE);
//				Log.i("INFO","freshed! getPVCInfoList");
//				list=ApiClient.getPVCInfoList(this, recordId, pageIndex, PAGE_SIZE);
//				if (list != null && pageIndex == 0) {
//					Notice notice = list.getNotice();
//					list.setNotice(null);
//					list.setCacheKey(key);
//					saveObject(list, key);
//					list.setNotice(notice);
//				}
//			} catch (AppException e) {
//				list = (PVCInfoList) readObject(key);
//				if (list == null)
//					throw e;
//			}
//		} else {
//			list = (PVCInfoList) readObject(key);
//			if (list == null)
//				list = new PVCInfoList();
//		}
//		return list;
//	}
//	
//	public PVCItem getPVCSamples(int recordId, int fromPos, int toPos) throws AppException {
//		String key = "pvcsamples_" + recordId + "_" + fromPos+"_"+toPos;
//		PVCItem samples;
//		if (isNetworkConnected() && (!isReadDataCache(key))) {
//			try {
//				Log.i("INFO","freshed! getPVCSamples");
//				samples=ApiClient.getPVCSamples(this, recordId, fromPos, toPos);
//				if (samples != null && samples.samples!=null&&samples.samples.size()>0) {
//					Notice notice = samples.getNotice();
//					samples.setNotice(null);
//					samples.setCacheKey(key);
//					saveObject(samples, key);
//					samples.setNotice(notice);
//				}
//			} catch (AppException e) {
//				samples = (PVCItem) readObject(key);
//				if (samples == null)
//					throw e;
//			}
//		} else {
//			samples = (PVCItem) readObject(key);
//			if (samples == null)
//				samples = new PVCItem();
//		}
//		return samples;
//	}
	
	/**
	 * 
	 * @param act; 1 is add and change, 2 is delete
	 * @param params
	 * @param appContext 
	 * @return
	 * @throws AppException 
	 */
	public Relative actRelative(int act,Map<String,Object> params){
//		try {
//		if(act==1){
//				return ApiClient.addRelative(this, params);
//		}else if(act==2){
//			ApiClient.delRelative(this, params);
//		}
//		} catch (AppException e) {
//			networkErrHandler.sendEmptyMessage(1);
//		}
		Relative r=new Relative();
		r.setId(new Integer(params.get("id").toString()));
		if(act==1)
			r.setId(10);
		r.setFullName(params.get("fullName").toString());
		r.setPhoneNumber(params.get("phoneNumber").toString());
		r.setPatientId(new Long(params.get("patientId").toString()));
		return r;
	}
	
	public void setCurGPSData(GpsData gpsData){
		this.curGpsData=gpsData;
	}
	
	public GpsData getCurGPSData(){
		if(isTrace()){
			return curGpsData;
		}
		return null;
	}
	
	//供测试使用
	public EcgRecord sendUserData(Map<String, Object> params){
//		Date startTime=new Date();
//		EcgRecord record=new EcgRecord();
//		long curTime = System.currentTimeMillis();
//		record.setAfibNumber(123*(int)(System.nanoTime()%5+1));
//		record.setPauseNumber(2234*(int)(System.nanoTime()%5+1));
//		record.setTotalPvcNumber(23332*(int)(System.nanoTime()%5+1));
//		record.setTotalSvpbNumber(133*(int)(System.nanoTime()%5+1) );
//		record.setLongRRNumber(44);
//		record.setStartTime(startTime);
//		record.setEndTime(new Date(curTime));
////		//
//		record.setCurRRPieceInterval(5*(int)(HandlerEvent.SAMPLE_RATE/2+System.currentTimeMillis()%HandlerEvent.SAMPLE_RATE));
		try {
//			Thread.sleep(400);
//			Map<String, Object> params=new HashMap<String, Object>();
			return ApiClient.sendEcgData(this, params);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new EcgRecord();
	}

	public void updateCurUser(Map<String,String> data) {
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public EcgRecord createTransferSession(Map<String, Object> params) throws AppException{
		// TODO Auto-generated method stub
			return ApiClient.createTransferSession(this,params);
	}
	
	public int receiveNewHmessage(Hmessage msg){
		ArrayList<Hmessage> msgs=(ArrayList<Hmessage>)this.readObject(KEY_HMESSAGE_NEW);
		if(msgs==null){
			msgs=new ArrayList<Hmessage>();
		}		
		msgs.add(msg);
		this.saveObject(msgs, KEY_HMESSAGE_NEW);
		int count=0;
		for(int i=msgs.size()-1;i>=0;i--){
			if(msgs.get(i).getViewTime()==null){
				count++;
			}
		}
		return count;
	}
	
	public void removeHmessage(Hmessage msg){
		ArrayList<Hmessage> msgs=(ArrayList<Hmessage>)this.readObject(KEY_HMESSAGE_NEW);
		if(msgs==null){
			msgs=new ArrayList<Hmessage>();
		}	
		msgs.remove(msg);
		this.saveObject(msgs, KEY_HMESSAGE_NEW);
	}
	
	public void markHmessageAsViewed(Hmessage msg){
		boolean rewrite=false;
		ArrayList<Hmessage> all=(ArrayList<Hmessage>)this.readObject(KEY_HMESSAGE_NEW);
		if(all==null){
			all=new ArrayList<Hmessage>();
			rewrite=true;
		}
		Date now=new Date();
		msg.setViewTime(now);
		for(Hmessage m:all){
			if(m.getMessageId().equals(msg.getMessageId())){
				m.setViewTime(now);
				rewrite=true;
				break;
			}
		}
		if(rewrite){		
			this.saveObject(all, KEY_HMESSAGE_NEW);
		}
	}
	
	public ArrayList<Hmessage> getHmessages(int fromIndex,int num){
		ArrayList<Hmessage> all=(ArrayList<Hmessage>)this.readObject(KEY_HMESSAGE_NEW);
		Date now=new Date();
		long lnow=now.getTime();
		boolean rewrite=false;
		if(all==null){
			all=new ArrayList<Hmessage>();
			rewrite=true;
		}		
		
		int size=all.size();
		
		for(int i=size-1;i>=0;i--){
			Hmessage m=all.get(i);
			if(lnow-m.getSendTime().getTime()>=AppConfig.CONF_MESSAGE_SAVINGTIME_IN_MS){
				rewrite=true;
				all.remove(i);
			}
		}
		ArrayList<Hmessage> ret=new ArrayList<Hmessage>();
		size=all.size();		
		if(rewrite){		
			this.saveObject(all, KEY_HMESSAGE_NEW);
		}
		if(size>0){
			int index=0;
			int from=fromIndex;
			int end=from+num;
			Date newDate=all.get(size-1).getViewTime();
			for(int i=size-1;i>=0;i--){
				Hmessage m=all.get(i);			
				if(index>=end)break;
				if(index>=from)ret.add(m);
				index++;							
			}		
		}
		return ret;
	}
	
	public void triggerStompAction(String cmd,Map<String,String> params){
		Intent intent=new Intent(this,StompClient.class);
		intent.putExtra("cmd", cmd);
		if(params!=null){
			for(Map.Entry<String, String> e:params.entrySet()){
				intent.putExtra(e.getKey(), e.getValue());
			}
		}
		this.startService(intent);
	}
	
//	/**
//	 * 用于处理，刷新到设备时更新界面
//	 * 
//	 * @author Kevin.wu
//	 * 
//	 */
//	public interface RFStarManageListener {
//		public void RFstarBLEManageListener(BluetoothDevice device, int rssi,
//				byte[] scanRecord);
//
//		public void RFstarBLEManageStartScan();
//
//		public void RFstarBLEManageStopScan();
//	}
//	
//	
}
