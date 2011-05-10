package se.tonyivanov.android.ajax;
/*
 * @author TonyIvanov (telamohn@gmail.com) 
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Log;

public class Transport {
	private HttpResponse response;
	private String contentType;
	private String responseText="";
	private JSONObject responseJson=null;
	private Document responseXml=null;
	
	private String contentEncoding;
	private long contentLength;
	public Transport(HttpResponse resp) throws IllegalStateException, IOException{
		response = resp;
		if(response.getEntity().getContentType() !=null){
			contentType= response.getEntity().getContentType().getValue();
		}else{
			contentType = Request.CTYPE_PLAIN;
		}
		if(response.getEntity().getContentEncoding() !=null){
			contentEncoding = response.getEntity().getContentEncoding().getValue();
		}else{
			contentEncoding = "UTF-8";
		}
		
		contentLength = response.getEntity().getContentLength();		
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		/*InputStream content = response.getEntity().getContent();
		int b=0;
		while((b = content.read())!= -1){
			baos.write(b);
		}*/
		response.getEntity().writeTo(baos);
		
		responseText = baos.toString("UTF-8");
		
		response.getEntity().consumeContent();
		if(contentType.equalsIgnoreCase(Request.CTYPE_JSON)){
			try {
				responseJson = new JSONObject(responseText);
			} catch (JSONException e) {				
				e.printStackTrace();
			}
		}else
		if(contentType.equalsIgnoreCase(Request.CTYPE_XML)){
			
		}
	}
	public HttpResponse getResponse() {
		return response;
	}
	public String getContentType() {
		return contentType;
	}
	public String getContentEncoding() {
		return contentEncoding;
	}
	public long getContentLength() {
		return contentLength;
	}
	public void parseResponseXml(ContentHandler xmlPushHandler){
		if(!contentType.equalsIgnoreCase(Request.CTYPE_XML)){
			Log.w("Ajax.Request", "Trying to parse XML from response with type: "+contentType);
		}		
		try {
			android.util.Xml.parse(getResponseText(), xmlPushHandler);
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	public XmlPullParser getResponseXmlPullParser(){
		if(!contentType.equalsIgnoreCase(Request.CTYPE_XML)){
			Log.w("Ajax.Request", "Trying to parse XML from response with type: "+contentType);
		}
		try {
			XmlPullParser pp =android.util.Xml.newPullParser(); 
			pp.setInput(new ByteArrayInputStream(responseText.getBytes()), getContentEncoding());			
			return pp;
		} catch (XmlPullParserException e) {			
			e.printStackTrace();
			return null;
		}
	}
	public Document getResponseXml(){
		if(responseXml == null){
			if(!contentType.equalsIgnoreCase(Request.CTYPE_XML)){
				Log.w("Ajax.Request", "Trying to parse XML from response with type: "+contentType);
			}
	
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder builder;
				builder = factory.newDocumentBuilder();
				responseXml= builder.parse(new ByteArrayInputStream(responseText.getBytes()), getContentEncoding());
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}       
		}
		return responseXml;
	}
	/**
	 * Helper method to find nodes by XPath in XML responses.
	 * 
	 * WARNING: Use this method with care! First off, it calls getResponseXml
	 * which in effect builds a searchable Document that can hog up ALOT of memory
	 * if used carelessly. 
	 * Second, this method fails if getResponseXml fails to parse responseText as XML.
	 * 
	 * @param expression A valid XPath expression see http://developer.android.com/reference/javax/xml/xpath/package-summary.html for more info.
	 * @return NodeList An Array containing nodes that match your expression.
	 * @throws XPathExpressionException 
	 */
	
	public NodeList findByXpath(String expression) throws XPathExpressionException{
		if(getResponseXml() == null){
			// let's do something forbidden.
			throw new XPathExpressionException("There is no parsable XML in the response.");
		}
		XPath xpath = XPathFactory.newInstance().newXPath();
		return (NodeList) xpath.evaluate(expression, getResponseXml(), XPathConstants.NODESET);
	}
	
	public JSONObject getResponseJson(){
		return responseJson;	
	}
	public int getStatus(){
		return response.getStatusLine().getStatusCode();
	}
	public String getResponseText(){
		return responseText;
	}
}
