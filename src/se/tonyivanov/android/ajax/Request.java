package se.tonyivanov.android.ajax;
/*
 * @author TonyIvanov (telamohn@gmail.com) 
 */
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.json.JSONObject;
import org.w3c.dom.Document;

import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.util.Log;

public abstract class Request extends AsyncTask<Object,Void,Transport> {
	// Redundant Constants probably already defined at other places.
	public final static String GET ="GET";
	public final static String POST ="POST";
	public final static String PUT ="PUT";
	public final static String DELETE ="DELETE";
	public final static String HEAD = "HEAD";
	public final static String OPTIONS = "OPTIONS";
	
	public final static String CTYPE_JSON = "application/json";
	public final static String CTYPE_FORM = "application/x-www-form-urlencoded";
	public final static String CTYPE_XML = "application/xml";	
	public final static String CTYPE_PLAIN = "text/plain";	
	
	private HashMap<String,String> headers;
	AndroidHttpClient client;
	BasicHttpContext context;
	private String method; 
	HttpRequestBase request;
	private Transport transport;
	
	private String url;
	private HttpEntity params;


	
	public Request(String url){
		context= new BasicHttpContext();		
		client = AndroidHttpClient.newInstance("se.tonyivanov.android.ajax.Request");
		this.url = url;
		headers = new HashMap<String,String>();
		headers.put("Accept",CTYPE_JSON);
	}
	
	@Override
	protected Transport doInBackground(Object... args){
		method = ((String)args[0]).toUpperCase();
		if(args.length > 1 && method.equalsIgnoreCase(POST) || method.equalsIgnoreCase(PUT)){
			setParams(args[1]);
		}
		
		if(method.equalsIgnoreCase(POST)){
			request = new HttpPost(url);		
			((HttpPost)request).setEntity(params);
			
		}else if(method.equalsIgnoreCase(DELETE)){
			request = new HttpDelete(url);
		}else if(method.equalsIgnoreCase(HEAD)){
			request = new HttpHead(url);
		}else if(method.equalsIgnoreCase(OPTIONS)){
			request = new HttpOptions(url);
		}else if(method.equalsIgnoreCase(PUT)){			
			request = new HttpPut(url);
			((HttpPut)request).setEntity(params);			
		}else { //Defaults to GET.
			request = new HttpGet(url);
		}
		
//		client.enableCurlLogging("Ajax.Request", Log.DEBUG);
		for(String k:headers.keySet()){
			request.setHeader(k,headers.get(k));
//			Log.d("Ajax.Request","Header: "+k+" : "+headers.get(k));
		}
		
		try {
			transport = new Transport(client.execute(request));
			
		} catch (IOException e) {
			e.printStackTrace();
		}	
		return transport;
	}
	protected void onPostExecute(Transport transport){
		onComplete(transport);
	}
	protected abstract void onComplete(Transport transport);

	
	/**
	 *  Auto-identifies parameters and serializes into compatible format.
	 *  Also automatically sets the 'Content-Type' and 'Accept' header to the format
	 *  detected. (Note: if you want to accept a different type of content than the
	 *  parameters. Please call accept(String mime) after the call to this function.
	 * @param parameters Can be a HashMap , JSONObject or XMLNode, everything else fallbacks to Object.toString();
	 */
	private void setParams(Object parameters) {
		if(parameters instanceof String[][]){
			setFormParams((String[][])parameters);
		}else if(parameters instanceof HashMap<?,?>){
			setFormParams((HashMap<?,?>)parameters);
		}else if(parameters instanceof JSONObject){
			setJsonParams((JSONObject)parameters);
		}else{			
			setStringParams(parameters.toString());
		}
	}
	
	/**
	 *  Sets the supplied String:text as post data.
	 *  Also checks if the string starts with an XML indentation
	 *  and in that case sets the ContentType header to 'application/xml' 
	 * @param text
	 */
	public void setStringParams(String text) {
		try {
			if(text.startsWith("<?xml")){
				setContentType(CTYPE_XML);
			}
			params = new StringEntity(text);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}	
	
	public Request setFormParams(String[][] stringsTable){
		setHeader("Content-Type",CTYPE_FORM);
		List<NameValuePair> list = new ArrayList<NameValuePair>(stringsTable.length);
		String[][] st = stringsTable;
		for(String[] e: st){
			list.add(new BasicNameValuePair(e[0],e[1]));					
			Log.d("Ajax.Request","Param: "+e[0]+" : "+e[1]);
		}
		try {
			params = new UrlEncodedFormEntity(list);			
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}		
		return this;
	}
	
	public Request setFormParams(HashMap<?,?> hashmap){
		setHeader("Content-Type",CTYPE_FORM);
		List<NameValuePair> list = new ArrayList<NameValuePair>(hashmap.size());
		
		Iterator<?> i = hashmap.keySet().iterator();
		while(i.hasNext()){
			Object key = i.next();
			list.add(new BasicNameValuePair(key.toString(),hashmap.get(key).toString()));
		}
		try {
			params = new UrlEncodedFormEntity(list);			
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}		
		return this;
	}
	
	public Request setJsonParams(JSONObject jsonObject){
		setContentType(CTYPE_JSON);
		accept(CTYPE_JSON);
		setStringParams(jsonObject.toString());
		return this;
	}
	
	public Request setXmlParams(Document doc){
        try {
    		TransformerFactory tf = TransformerFactory.newInstance();
			Transformer t = tf.newTransformer();
	        DOMSource src = new DOMSource(doc);
	        ByteArrayOutputStream target = new ByteArrayOutputStream();
	        StreamResult sr = new StreamResult(target);
	        t.transform(src, sr);
	        setStringParams(target.toString("UTF-8"));
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return this;
	}
	
	public Request setContentType(String ctype){
		setHeader("Content-Type",ctype);		
		return this;
	}
	public String getContentType(String ctype){
		return headers.get("Content-Type");
	}
	public Request setHeader(String key, String value){
		headers.put(key, value);
		return this;
	}
	/**
	 *  Modifies the Accept header field.
	 * @param contentType
	 * @return returns self for convenience.
	 */
	public Request accept(String contentType){
		setHeader("Accept",contentType);
		return this;
	}
	
	
}
