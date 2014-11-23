package com.echo.block;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.util.ByteArrayBuffer;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;

public class App extends Application {

	public static boolean showInterstitialAd = true; // when it is xiaomi,
														// disable it;
	public static boolean autoDownloadAd = false;
	public static boolean newVersionAvailable = false;

	// ad related
	// TODO very important: update every version
	private static final String AUTO_DOWNLOAD_AD = "auto3.3";
	private static final String SHOW_INTERSTITIAL_AD = "3.3ad"; // only for xiaomi: xiaomi do not allow use interstitial ad

	private static final String SUBMIT_SCORE_URL = "http://littleappleapp.sinaapp.com/submit_score.php";
	
	// for preference
	private static final String TYPE_PREFIX = "type:";
	
	private static SharedPreferences sharedPreferences;

	private static final String LAST_ENDLESS_DATE = "LAST_ENDLESS_DATE";
	
	// TODO attention: this is the wandoujia ad info, please set change it
	public static final String AD_TAG = "";
	private static final String APPKEY_ID = "";
	private static final String SECRET_KEY = "";

	@Override
	public void onCreate() {
		super.onCreate();

		sharedPreferences = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
		asyncGetOnlineConfig();
		initAd();
	}

	private void asyncGetOnlineConfig() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				getOnlineConfig();
			}
		}).start();
	}

	private void getOnlineConfig() {
		try {
			URL url = new URL("http://littleappleapp.sinaapp.com/config.txt");
			HttpURLConnection urlConnection = (HttpURLConnection) url
					.openConnection();
			InputStream inputStream = urlConnection.getInputStream();
			byte[] bytes = new byte[1024];
			int count;
			ByteArrayBuffer byteArrayBuffer = new ByteArrayBuffer(1024);
			count = inputStream.read(bytes);
			while (count != -1) {
				byteArrayBuffer.append(bytes, 0, count);
				count = inputStream.read(bytes);
			}
			String config = new String(byteArrayBuffer.toByteArray());
			if (config.contains(SHOW_INTERSTITIAL_AD)) {
				showInterstitialAd = true;
			}

			if (config.contains(AUTO_DOWNLOAD_AD)) {
				autoDownloadAd = true;
			}

			String[] tmp = config.split("\\s+");

			for (String str : tmp) {
				if (str.startsWith("version")) {
					int versionCode = Integer.parseInt(str.substring("version"
							.length()));
					checkUpdate(versionCode);
					break;
				}
			}

			urlConnection.disconnect();
			inputStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkUpdate(int versionCode) {
		try {
			int currentVersionCode = getPackageManager().getPackageInfo(
					getPackageName(), 0).versionCode;
			if (versionCode > currentVersionCode) {
				newVersionAvailable = true;
			}

		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void initAd() {
		// TODO depend on different ad platform
	}
	

	/*
	public static void submitScore(final String nickyName,
			final String scoreString, final int type) {
		if (nickyName == null || nickyName.trim().equals("")
				|| scoreString == null || scoreString.trim().equals("")) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				// TODO the uri should base on the mode
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
				nameValuePairs.add(new BasicNameValuePair("nickyname",
						nickyName));
				nameValuePairs
						.add(new BasicNameValuePair("score", scoreString));
				nameValuePairs.add(new BasicNameValuePair("type", type + ""));
				Util.httpPost(SUBMIT_SCORE_URL, nameValuePairs, callBack);
			}
		}).start();
	}

	 */

	public static long getBestScore(int type){
		long score;
		score = sharedPreferences.getLong(TYPE_PREFIX + type, 0);
		return score;
	}
	
	// attention: is not backward compatible
	public static void updateBestScore(int type, long score){
		long bestScore = getBestScore(type);
		if (score < bestScore) {
			return;
		}
		sharedPreferences.edit().putLong(TYPE_PREFIX + type, score).commit();
	}
	
	public static void putString(String key, String value){
		sharedPreferences.edit().putString(key, value).commit();
	}
	
	public static String getString(String key){
		return sharedPreferences.getString(key, null);
	}
	
	public static void putInt(String key, int value) {
		sharedPreferences.edit().putInt(key, value).commit();
	}
	
	public static int getInt(String key){
		return sharedPreferences.getInt(key, 0);
	}
	
}
