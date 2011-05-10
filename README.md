Ajax.Request for Android
========================

I'm used to writing Webserver requests in JavaScript and when I arrived
to the android platform I realized that the HttpClient-request-way left much
to be desired compared to prototypejs' Ajax.Request with auto-serialization, content handling and
general lazyness.

Features
---------
* Automatic content handling for XML and JSON
* Built on top of AsyncTask to ensure that your callback code runs on the UI thread.
* Provides general lazyness.
* Failsafe? (No guarantees yet)
### TODO
* JSON Header support
* More ways to handle XML post data?
* Rails authenticity token handling for "application/x-www-form-urlencoded" postdata

Requesting Things (The Request class)
=================================


Example usage
--------------
Include the library in whatever fashion you desire

### Simple GET request

	Request r = new Request("http://mydomain.org/service/service.json"){
		@Override
		protected void onComplete(Transport transport) {
			// Your handling code goes here,
			// The 'transport' object holds all the desired response data.
						
			EditText et = (EditText) this.findViewById(R.id.editText1);
			et.getText().append( transport.getResponseJson().optString("foo") );
						
		}					
	}.execute("GET");	
					
### Simple POST request

	Request r = new Request("http://mydomain.org/service/highscore"){
		@Override
		protected void onComplete(Transport transport) {
			// Grab the desired nodes with the xpath helper			
			org.w3c.dom.NodeList users = transport.xpath('//users');
			
			// do something.
			for(org.w3c.dom.Node user:users){
				addUserToMyList(user);
			}
		}	
	}
	
	// Tell the webserver that we would like to have the response in XML
	r.accept(Request.CTYPE_XML); 
	
	// Tell the webserver that our post data is in JSON
	r.setContentType(Request.CTYPE_JSON);
		
	r.execute("POST","{ \"name\" : \"MrMan\", \"score\" : 9001 }");
	
	
Headers
-------
You can set whatever HTTPHeader you like but they must be set _before_ the execute() call.
	request.setHeader("If-Modified-Since", "Tue, 10 May 2011 11:31:56 GMT")

There are a few shortcuts like
	request.accept("text/plain"); // Modifies the 'Accept' header
	setContentType("application/json");	 // Modifies the 'Content-Type' header

Parameters
----------
You can easily set POST or PUT data by calling
	r.setParams(myBadAssObject)
or provide your data as the second parameter to execute, eg: 
	r.execute("POST",myKewlObject)

setParams(Object) method tries to automatically determine the type of content you're
trying to send, and automatically sets the Content-Type header accordingly.

The detected data is then delegated to one of the following methods:
	
### setFormParams(String[][])
Converts your data to an UrlEncodedFormEntity and calls setContent(CTYPE_FORM).
usage:
	setFormParams(new String[][]{
		{"foo","bar"},
		{"eat","bacon"}
	});
	
### setFormParams(HashMap<?, ?>)
Does the same as above except it takes an hashmap as input and ultimately calls
toString() on each key / value.

### setJsonParams(org.json.JSONObject)
Sets Content-Type header to application/json.
Converts your JSONObject to a String and then delegates your data to setStringParams()
	 	
###	setXmlParams(org.w3c.dom.Document)
Sets Content-Type header to application/xml,
Converts your Xml Document to a String and then delegates your data to setStringParams()
	
### setStringParams(String)
Creates a StringEntity from your data and sets it to be sent with the request.
	
_note_
As of this moment there's no sophisticated string content type detection.
The only case that setStringParams() modifies the "Content-Type" header
is when the string begins with '<?xml'.

Yes this might seem like a design flaw and might trigger the "setParams calls modified
my Content-Type header when I've already set it manually"-problem.
But also it's pretty stupid to be required to call setContent("application/json")
when passing a JSONObject as parameters.


Response Handling (a.k.a the Transport class)
=============================================

More on this later, I'm hungry, goin for lunch.  



  




 