package com.eiri.wifidb_uploader;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class ScanService extends Service {
	private static final String TAG = "WiFiDB_ScanService";
	private static Timer timer = new Timer();
	private Context ctx;
	static WifiManager wifi;
	BroadcastReceiver receiver;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onCreate() {
		ctx = this; 
		Toast.makeText(this, "My Service Created", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onCreate");
		
		// Setup WiFi
		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		// Register Broadcast Receiver
		//if (receiver == null)
		//	receiver = new WiFiScanReceiver(this);
		//registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
		
		//Setup GpS
		MyLocation my_location = new MyLocation();
		my_location.init(this, null);	   		
	
		timer.scheduleAtFixedRate(new mainTask(), 0, 5000);
	}
	
	private class mainTask extends TimerTask
    { 
        public void run() 
        {
        	//new Thread(new Runnable() {
    	        //public void run() {
    	        	// Get Prefs
    	        	SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
	    		    String WifiDb_ApiURL = sharedPrefs.getString("wifidb_upload_api_url", "https://api.wifidb.net/");
	    		    String WifiDb_Username = sharedPrefs.getString("wifidb_username", "Anonymous"); 
	    		    String WifiDb_ApiKey = sharedPrefs.getString("wifidb_upload_api_url", "");     	
	    		    String WifiDb_SID = "1";
	    		    
	    		    // Get Location
    	    		Location location = MyLocation.getLocation(ctx);
    	    	    final Double latitude = location.getLatitude();
    	    	    final Double longitude = location.getLongitude();
    	    	    Integer sats = MyLocation.getGpsStatus(ctx);      	
    	    	    Log.d(TAG, "LAT: " + latitude + "LONG: " + longitude + "SATS: " + sats);
    	    	    
    	    	    // Get Wifi Info
    	    	    List<ScanResult> results = ScanService.wifi.getScanResults();
    	    	    for (ScanResult result : results) {    	    	    
    	    	    	Log.d(TAG, "onReceive() http post");
    	    	    	WifiDB post = new WifiDB();
    	    		    String Label = "";
    	    	    	post.postLiveData(WifiDb_ApiURL, WifiDb_Username, WifiDb_ApiKey, WifiDb_SID, result.SSID, result.BSSID, result.capabilities, result.frequency, result.level, latitude, longitude, Label);
    	    	    }
    	        //}
  	      	//}).start();
        }
    }
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "My Service Stopped", Toast.LENGTH_LONG).show(); 	
		
		if(timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
	}
	
	@Override
	public void onStart(Intent intent, int startid) {
		Toast.makeText(this, "My Service Started", Toast.LENGTH_LONG).show();
		Log.d(TAG, "onStart");
		wifi.startScan();
		// List available networks
		List<WifiConfiguration> configs = wifi.getConfiguredNetworks();
		for (WifiConfiguration config : configs) {
			Log.d(TAG, "Network: " + config.toString());
		}
	}
}