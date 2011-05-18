package se.tonyivanov.android.ajax;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class OAuthHandler implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 609663171140088815L;
	private OAuthConsumer consumer;
	private OAuthProvider provider;
	private String verificationCode=null;
	public OAuthHandler(String app_id,String app_key,String apiBase){
		this(app_id,app_key,
				apiBase+"/oauth/request_token",
				apiBase+"/oauth/access_token",
				apiBase+"/oauth/authorize"
				);		
	}
	public OAuthHandler(String app_id, String app_key, String requestPath,
			String accessPath,String authorizePath) {
		consumer = new CommonsHttpOAuthConsumer(app_id,app_key);
		provider = new CommonsHttpOAuthProvider(requestPath,accessPath,authorizePath);
		
	}
	public OAuthConsumer getConsumer(){
		return consumer;
	}
	public String getAppCallback(Activity app){
		return "oauth://"+app.getClass().getName()+"/callback";
	}
	private void auth_request_token(Activity app) throws Exception{
		Log.i("Ajax.OAuth","Callback URI: "+  getAppCallback(app));
	
		String authUrl = provider.retrieveRequestToken(consumer, getAppCallback(app));
		Log.i("Ajax.OAuth", "REQ Token AQUIRED: " + authUrl);
// Webviews don't seem to work very well.
//		WebView webview = new WebView(app);
//		app.setContentView(webview);
////		CookieSyncManager.createInstance(app);
////		CookieSyncManager.getInstance().sync();
//		webview.loadUrl(authUrl);
		
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.setData(Uri.parse(authUrl));
		app.startActivity(i);
	
	}
	private void fetch_access_token(Uri data){
		verificationCode = data.getQueryParameter("oauth_verifier");
		Log.i("Ajax.OAuth", "VERIFICATION AQUIRED:" + verificationCode);
		try {			
			provider.retrieveAccessToken(consumer,verificationCode);
			
			Log.i("Ajax.OAuth", "ACCESS TOKEN AQUIRED!");
			Log.i("Ajax.OAuth", "saveToken " + consumer.getToken() + " secret: "+ consumer.getTokenSecret());
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthNotAuthorizedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	public void authenticate(Activity app) {
		restore(app);
		if(app.getIntent().getData() != null && app.getIntent().getData().toString().startsWith(getAppCallback(app))){
			fetch_access_token(app.getIntent().getData());
        }else{		
        	try {
				auth_request_token(app);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	public void sign(Request request) {
		try {
			consumer.sign(request.getHttpRequest());
		} catch (OAuthMessageSignerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthExpectationFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OAuthCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private final static String cacheFile = "oauth_cache";
	
	private static String getCacheFile(Activity app){
		return cacheFile;
	}
	private void restore(Activity app){
		Log.i("Ajax.OAuth", "restoring token");
		try {
			java.io.FileInputStream fis = app.openFileInput(getCacheFile(app));
			java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
			OAuthConsumer fossil = (OAuthConsumer) ois.readObject();
			OAuthProvider fosp = (OAuthProvider) ois.readObject(); 
			ois.close();
			consumer = fossil;
			provider = fosp;
			Log.i("Ajax.OAuth", "Token restored");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public void save(Activity app) {
		Log.i("Ajax.OAuth", "Saving onPause()");
		try {			
			FileOutputStream fos = app.openFileOutput(getCacheFile(app),Context.MODE_PRIVATE);
			java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(fos);
			oos.writeObject(consumer);
			oos.writeObject(provider);
			oos.close();
			Log.i("Ajax.OAuth", "Save succeded");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
