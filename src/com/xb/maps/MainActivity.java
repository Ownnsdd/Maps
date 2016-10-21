package com.xb.maps;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender.SendIntentException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.service.carrier.CarrierMessagingService.SendMmsResult;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	// 地图相关
	private MapView mMapView;
	private BaiduMap mBitMap;

	// 数据库相关
	private static SQLiteDatabase db;
	private dbTools tool;
	private Cursor dbCursor;

	// 短信相关
	private String sendms = "Where are you now?";

	private String SMS_SEND_ACTION = "SMS_SEND";
	private String SMS_DELIVERED_ACTION = "SMS_DELIVERED";
	
	private IntentFilter receiveFilter;
    private MessageReceiver messageReceiver;
	
	private Double latitude;
	private Double longitude;

	// 定位相关
	private LocationClient mLocationClient;
	private MyLocationListener mLocationListener;
	private boolean isFirstIn = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		SDKInitializer.initialize(getApplicationContext());
		setContentView(R.layout.activity_main);
		db = DBconnection.open(this);
		tool = new dbTools(db);
		initView();
		dbCursor = tool.findAll();

		initLocation();

		LatLng point = new LatLng(39.963175, 116.400244);
		makeMarker(point);

		Button fl = (Button) findViewById(R.id.btn_fl);
		Button fmf = (Button) findViewById(R.id.btn_fmf);

		fl.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
				startActivity(intent);
			}
		});

		fmf.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				Sendmstof();
				/*
				PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 1,
						new Intent(SMS_SEND_ACTION), 0);
				PendingIntent deliveryIntent = PendingIntent.getBroadcast(MainActivity.this, 2,
						new Intent(SMS_DELIVERED_ACTION), 0);
				SmsManager manager = SmsManager.getDefault();
				manager.sendTextMessage("15820591042", null, sendms, sentIntent,
						deliveryIntent);
						*/
			}
		});
		
		receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        messageReceiver = new MessageReceiver();
        registerReceiver(messageReceiver,receiveFilter);

	}

	private void initLocation() {
		mLocationClient = new LocationClient(this);
		mLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mLocationListener);

		LocationClientOption option = new LocationClientOption();
		option.setCoorType("bd09ll");
		option.setIsNeedAddress(true);
		option.setOpenGps(true);
		option.setScanSpan(1000);
		mLocationClient.setLocOption(option);
	}

	private void initView() {
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBitMap = mMapView.getMap();
		MapStatusUpdate msu = MapStatusUpdateFactory.zoomTo(15.0f);
		mBitMap.setMapStatus(msu);
	}

	private void makeMarker(LatLng point) { // 构建Marker图标 BitmapDescriptor
		BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.friends);
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(point).icon(bitmap); // 在地图上添加Marker，并显示
		mBitMap.addOverlay(option);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mBitMap.setMyLocationEnabled(true);
		if (!mLocationClient.isStarted())
			mLocationClient.start();
	}

	@Override
	protected void onStop() {
		super.onStop();
		mBitMap.setMyLocationEnabled(false);
		mLocationClient.stop();
	}

	@Override
	protected void onDestroy() {
		dbCursor.close();
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		mMapView.onDestroy();
		unregisterReceiver(messageReceiver);
	}

	@Override
	protected void onResume() {
		dbCursor = tool.findAll();
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.map_common:
			mBitMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
			break;
		case R.id.map_site:
			mBitMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private class MyLocationListener implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			MyLocationData data = new MyLocationData.Builder()//
					.accuracy(location.getRadius())//
					.latitude(location.getLatitude())//
					.longitude(location.getLongitude())//
					.build();
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			mBitMap.setMyLocationData(data);
			// MyLocationConfiguration config = new
			// MyLocationConfiguration(com.baidu.mapapi.map.MyLocationConfiguration.LocationMode.NORMAL,
			// arg1, arg2)

			if (isFirstIn) {
				LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
				MapStatusUpdate msu = MapStatusUpdateFactory.newLatLng(latlng);
				mBitMap.animateMapStatus(msu);
				isFirstIn = false;
			}
		}

	}

	class MessageReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String msg = "";
			Bundle bundle = intent.getExtras();
			Object[] pdus = (Object[]) bundle.get("pdus");
			for (Object pdu : pdus) {
				SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
				String address = smsMessage.getDisplayOriginatingAddress();
				String fullMessage = smsMessage.getMessageBody();

				
				// content.setText(fullMessage);
				msg += fullMessage;
				if(msg.equals(sendms)){
					String jwd = latitude.toString()+"/"+longitude.toString();
					sendmsg(address, jwd);
				}else{
					Friend friend = tool.findByPhone(address);
					if(friend!= null){
						String[] strs = msg.split("/");
						LatLng latLng = new LatLng(Double.valueOf(strs[0]), Double.valueOf(strs[1]));
						makeMarker(latLng);
					}
				}				 
			}
			
		}
	}
	
	public void Sendmstof(){
		Cursor cursor = tool.findAll();
		int n=1;
		while(cursor.moveToNext()){
			Friend friend = new Friend();
			friend = tool.findById(n);
			String numb = friend.getPhone();
			sendmsg(numb, sendms);
			n++;
		};
		/*
		while (cursor.moveToNext()) {
			String numb = cursor.getString(cursor.getColumnIndex("phone"));
			// 这个意图包装了对短信发送状态回调的处理逻辑
			
		}*/
		cursor.close();
	}
	
	public void sendmsg(String phone, String msg){
		PendingIntent sentIntent = PendingIntent.getBroadcast(MainActivity.this, 1,
				new Intent(SMS_SEND_ACTION), 0);
		// 这个意图包装了对短信接受状态回调的处理逻辑
		PendingIntent deliveryIntent = PendingIntent.getBroadcast(MainActivity.this, 2,
				new Intent(SMS_DELIVERED_ACTION), 0);
		SmsManager manager = SmsManager.getDefault();
		manager.sendTextMessage(phone, null, msg, sentIntent,
				deliveryIntent);
	}
}
