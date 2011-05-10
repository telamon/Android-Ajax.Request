Ajax.Request for Android
========================
A sweet way of writing your everyday http requests.


I'm used to writing webservice requests in JavaScript and when I arrived
to the android platform I realized that the HttpClient-request-way left much
to be desired compared to prototypejs' Ajax.Request with auto-serialization, content handling and
general lazyness.

Features
---------
* Automatic content handling for XML and JSON
* Built on top of AsyncTask to ensure that your callback code runs on the UI thread.
* Provides general lazyness.
* Failsafe? (No guarantees yet)
* Better resource handling to be able to receive binary data responses?
### TODO

* JSON Header support
* More ways to handle XML post data?
* Rails authenticity token handling for "application/x-www-form-urlencoded" postdata

Requesting Things (The Request class)
-------------------------------------


### Example usage
Include the library in whatever fashion you desire

#### Simple GET request

	Request r = new Request("http://mydomain.org/service/service.json"){
		
		// Optional callback override.
		@Override
		protected void onSuccess(Transport transport) {
			// Your handling code goes here,
			// The 'transport' object holds all the desired response data.
						
			EditText et = (EditText) this.findViewById(R.id.editText1);
			et.getText().append( transport.getResponseJson().optString("foo") );
						
		}					
	}.execute("GET");	
					
#### Simple POST request

	Context context = getApplicationContext();	
	startMyProgressIndicator();
	
	Request r = new Request("http://mydomain.org/service/highscore"){
		
		// Optional callback override.
		@Override
		protected void onSuccess(Transport transport) {
			// Grab the desired nodes with the xpath helper			
			org.w3c.dom.NodeList users = transport.findByXpath('//users');
			
			// do something.
			for(org.w3c.dom.Node user:users){
				addUserToMyList(user);
			}
		}
		
		
		// Optional callback override.
		@Override
		protected void onComplete(Transport transport) {
			stopMyProgressIndicator();
		}
		
		
		// Optional callback override.
		@Override
		protected void onError(IOException ex) {
			Toast.makeText(context, "Error occured: " + ex.getMessage(), Toast.LENGTH_SHORT);
		}
		
		
		// Optional callback override.
		@Override
		protected void onFailure(Transport transport) {
			Toast.makeText(context, "Something went wrong. code: " + transport.getStatus(),Toast.LENGTH_SHORT);								
		}
			
		
	}
	
	// Tell the webserver that we would like to have the response in XML
	r.accept(Request.CTYPE_XML); 
	
	// Tell the webserver that our post data is in JSON
	r.setContentType(Request.CTYPE_JSON);
		
	r.execute("POST","{ \"name\" : \"MrMan\", \"score\" : 9001 }");
	
	
### Headers
You can set whatever HTTPHeader you like but they must be set _before_ the execute() call.

	request.setHeader("If-Modified-Since", "Tue, 10 May 2011 11:31:56 GMT")

There are a few shortcuts like

	request.accept("text/plain"); // Modifies the 'Accept' header
	setContentType("application/json");	 // Modifies the 'Content-Type' header

### Parameters

You can easily set POST or PUT data by calling

	r.setParams(myBadAssObject)

or provide your data as the second parameter to execute, eg: 

	r.execute("POST",myKewlObject)

setParams(Object) method tries to automatically determine the type of content you're
trying to send, and automatically sets the Content-Type header accordingly.

The detected data is then delegated to one of the following methods:

#### Urlencoded form 

	setFormParams(new String[][]{
		{"foo","bar"},
		{"eat","bacon"}
	});
	
Converts your data to an UrlEncodedFormEntity and calls setContent(CTYPE_FORM).
	
	setFormParams(HashMap<?, ?>)
Does the same as above except it takes an hashmap as input and ultimately calls
toString() on each key / value.

#### JSON

	setJsonParams(org.json.JSONObject)
Sets Content-Type header to application/json.
Converts your JSONObject to a String and then delegates your data to setStringParams()

#### XML
	 	
	setXmlParams(org.w3c.dom.Document)
Sets Content-Type header to application/xml,
Converts your Xml Document to a String and then delegates your data to setStringParams()

#### Plain text
	
	setStringParams(String)
Creates a StringEntity from your data and sets it to be sent with the request.
	
#### note
As of this moment there's no sophisticated string content type detection.
The only case that setStringParams() modifies the "Content-Type" header
is when the string begins with __'<?xml'__.

Yes this might seem like a design flaw and might trigger the "setParams calls modified
my Content-Type header when I've already set it manually"-problem.
But also it's pretty stupid to be required to call setContent("application/json")
when passing a JSONObject as parameters.


Response Handling (a.k.a the Transport class)
=============================================


### Callbacks

This is pretty straightforward if you've used prototypejs' Ajax.Request before.

There are 4 overrideable callbacks.

	// Called when a request completes without any IOExceptions thrown.
	onComplete(Transport)
	
	// Called when an IOException occurs and our request gets borked.
	onError(IOException)
	
	// Called when a request completes with an http status code less than 400
	onSuccess(Transport)
	
	// Called when a request completes with an http status code greater than or equal to 400
	onFailure(Transport)

by default these callbacks do nothing so they're completely optional in case
you simply want to touch an url and don't care about the response. 

### Transport

If a request completes without any IOExceptions you will be provided with a Transport
object in your callback.

This object holds the Response data and all the information you normally receive from
a webserver.

Depending on the response Content-Type header the Transport object will automatically try to
deserialize the response data into JSON or XML if possible.
As of now it is not recommended to use this Library to fetch binary data as it expects
data in string format.

#### Plaintext
To get the raw text response content  use
	
	String data = transport.getResponseText();

This method is always available disregarding Content-Type.

#### JSON
To get the response as an JSONObject you can use
	
	JSONObject data = transport.getResponseJson();
 
This method is available when Content-Type returned by the webserver is "application/json"
This method returns __null__ when the response was in another format or the response is malformed.

#### XML
Due to the fact that XML documents can be rather memory consuming - there are
3 ways consume XML-data.
	
Push parse the XML-data by providing a content handler.
 
	parseResponseXml(ContentHandler);

	
Pull parse the XML-data

	XmlPullParser myPullParser = getResponseXmlPullParser();
	
Build a DOM

	org.w3c.dom.Document doc = transport.getResponseXml();
	// Or cut right to the cheese and grab the desired nodes with:
	org.w3c.dom.NodeList fantasyBooks = transport.findByXpath("/bookstore/books[@category='fantasy']");

This is the most memory consuming way, getResponseXml() holds a local cache to the Document
once built, so the node tree is only built once.
If the response data is malformed or fails to be parsed this method returns __null__
  

For more info on working with Push/Pull/DOM parsers see: 
 http://developer.android.com/reference/org/xmlpull/v1/XmlPullParser.html

	
 


  




 