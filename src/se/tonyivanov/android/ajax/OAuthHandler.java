package se.tonyivanov.android.ajax;



import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;
import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class OAuthHandler {
	private OAuthConsumer consumer;
	private OAuthProvider provider;	
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
	public void authenticate(Activity app) {
		if(app.getIntent().getData() != null && app.getIntent().getData().toString().startsWith(getAppCallback(app))){
			// Entry point of a successful redirect from the browser.
			loadToken(app);
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
	public OAuthConsumer getConsumer(){
		return consumer;
	}
	public String getAppCallback(Activity app){
		return "oauth://"+app.getClass().getName()+"/callback";
	}
	private void auth_request_token(Activity app) throws Exception{
		Log.i("Ajax.OAuth","Callback URI: "+  getAppCallback(app));
	
		String authUrl = provider.retrieveRequestToken(consumer, getAppCallback(app));
		Log.i("Ajax.OAuth","POMPULATED? = "+consumer.getToken()+" \n Secret: "+ consumer.getTokenSecret());
		Log.i("Ajax.OAuth", "REQ Token AQUIRED: " + authUrl);
		//Save current state
		saveToken(app);
		
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.setData(Uri.parse(authUrl));
		app.startActivity(i);
	
	}
	private void saveToken(Activity app){
		SharedPreferences prefs = app.getSharedPreferences("oauth", 0);
		Editor e = prefs.edit();
		e.putString("token", consumer.getToken());
		e.putString("secret", consumer.getTokenSecret());
		e.commit();
	}
	private void loadToken(Activity app){
		SharedPreferences prefs = app.getSharedPreferences("oauth", 0);
		consumer.setTokenWithSecret(prefs.getString("token", ""),prefs.getString("secret", ""));
	}
	
	
	private void fetch_access_token(Uri data){
		String verificationCode = data.getQueryParameter("oauth_verifier");
		Log.i("Ajax.OAuth", "Trying to verify vCode: " + verificationCode+"\n" +
				"token: "+consumer.getToken()+"\n" +
				"secret: "+consumer.getTokenSecret());
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




}
