package shank.shank.com.audiofrequency;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * NetworkCaller helps in making the Network call in the APP All the activities
 * will use the NetworkCaller class for making Network call to get the results.
 * 
 */

public class CallNetwork {

	// To check if network call is successful
	public boolean responseResult = false;

	public int statusCode;

	// Map used For POST requests
	public Map<String, String> postFields;

	// To store the response obtained from NetworkCall
	public String responseString;

	public Bitmap bitmap_image;
	public InputStream inputStream;
	Context ctx;
//    Connection timeout - 10 secs
    final private int timeoutConnection = 10000;
//    Socket timeout - 10 secs
    final private int timeoutSocket = 10000;

    public CallNetwork() {

	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int s) {
		this.statusCode = s;
	}

	/**
	 * GET request
	 * 
	 * @param url
	 *            - URL to which HTTPGET request is made
	 * @return inputStream of response obtained
	 */
	public InputStream getInputStream(String url) {
		Log.v("GET URL", url);
		/**
		 * "is" the InputStream for storing the result obtained as the result of
		 * GET NetworkCall.
		 */

		InputStream is = null;
		// try {
		try {
			responseResult = false;

            HttpParams httpParameters = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);


            HttpClient httpClient = new DefaultHttpClient(httpParameters);

			// httpGet to initialize the GET request
			HttpGet httpGet = new HttpGet(url);

			Log.v("URL", url);

			HttpResponse response = httpClient.execute(httpGet);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			Log.e("Status Code CN", response.getStatusLine().getStatusCode()
                    + "");

			// Get the Status code of the N/W call made
			int responseCode = response.getStatusLine().getStatusCode();

			statusCode = responseCode;

			responseResult = true;

		} catch (Exception e) {
			responseResult = false;
			e.printStackTrace();
		}

		return is;
	}

	public InputStream postInputStream(String url) {

		InputStream is = null;

		try {

			Log.v("Post URL ", url);

			responseResult = false;

			HttpClient httpClient = new DefaultHttpClient();
			Log.v("Index 0", url.charAt(0) + "");
			HttpPost httpPost = new HttpPost(url);

			httpPost.setHeader("Content-Type", "application/json");

			// convert parameters into JSON object

			// passes the results to a string builder/entity

			if (postFields != null) {
				JSONObject holder = new JSONObject(postFields);
				String postJSON = holder.toString();
				StringEntity se = new StringEntity(postJSON);
				Log.v("Post Json", postJSON);
				httpPost.setEntity(se);
			} else {
				Log.v("Post Data", "Null");

			}
			HttpResponse response = httpClient.execute(httpPost);
			HttpEntity entity = response.getEntity();
			is = entity.getContent();

			int responseCode = response.getStatusLine().getStatusCode();

			statusCode = responseCode;

			responseResult = true;
		} catch (Exception e) {
			responseResult = false;
			e.printStackTrace();
		}
		return is;
	}

	public String makeString(InputStream is) {
		String result = "";
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,
					"UTF-8"), 1024);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			result = sb.toString();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return result;
	}

	// return response string after GET-request
	public String getString(String url) {
		return makeString(getInputStream(url));
	}

	// return response string after POST-request
	public String post(String url) {
		return makeString(postInputStream(url));
	}

	// returns the Bitmap of the image obtained from the URL
	public Bitmap getBitmap(String url) {

		Bitmap temp_bitmap = null;
		try {
			temp_bitmap = BitmapFactory.decodeStream(getInputStream(url));
			Bitmap bm = temp_bitmap;
			if (temp_bitmap != null) {
				return bm;

			} else {
				Log.e("Bitmap : ", "Downloaded Bitmap is null");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return temp_bitmap;
	}

	public String makeNetworkCall(String resource, String method) {

		try {
			if (method.equals("GET")) {
				String url = " ";
				try {
					if (!resource.contains("http")) {
						url = AppConstants.MAIN_URL + resource;
					} else {
						url = resource;
					}
					responseString = "";
					responseString = getString(url);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (method.equals("POST")) {
				try {
					String url;

					url = AppConstants.MAIN_URL + resource;
					if (resource.contains("http") || resource.contains("192")) {
						url = resource;
					}
					responseString = "";
					responseString = post(url);

				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (method.equals("Image")) {
				// Ad's Bitmap retrived here
				String url = "";
				if (!resource.contains("http")) {
					url = AppConstants.MAIN_URL + resource;
				} else {
					url = resource;
				}
				bitmap_image = getBitmap(url);

			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return method;
	}

}
