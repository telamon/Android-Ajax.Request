package se.tonyivanov.android.ajax;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
	public String getAppCallback(Context appContext){
		return "oauth://"+appContext.getClass().getName()+"/callback";
	}
	private void auth_request_token(Context appContext) throws Exception{
		Log.i("Ajax.OAuth","Callback URI: "+  getAppCallback(appContext));
	
		String authUrl = provider.retrieveRequestToken(consumer, getAppCallback(appContext));
		Log.i("Ajax.OAuth", "REQ Token AQUIRED: " + authUrl);
		Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.addCategory(Intent.CATEGORY_BROWSABLE);
		i.setData(Uri.parse(authUrl));
		appContext.startActivity(i);
	
	}
	private void fetch_access_token(Uri data){
		verificationCode = data.getQueryParameter("oauth_verifier");
		Log.i("Ajax.OAuth", "VERIFICATION AQUIRED:" + verificationCode);
		try {
			provider.retrieveAccessToken(consumer,verificationCode);
			Log.i("Ajax.OAuth", "ACCESS TOKEN AQUIRED!");
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
}
