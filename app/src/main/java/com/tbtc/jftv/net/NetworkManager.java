package com.tbtc.jftv.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

import com.tbtc.jftv.common.Global;

public class NetworkManager {
	public final static int RESULT_OK = 0;
	public final static int RESULT_NOCARD = 1;
	public final static int RESULT_CANTUSE = 2;
	public final static int RESULT_FIRSTMUST12 = 3;
	public final static int RESULT_EXISTS = 4;	
	public final static int RESULT_TIMEOUTCARD = 5;
	public final static int RESULT_STOPCARD = 6;
	public final static int RESULT_ERROR = 7;
	
	
	private static NetworkManager s_instance = null;
	public static NetworkManager getManager()	{
		if(s_instance == null)
			s_instance = new NetworkManager();
		return s_instance;
	}
	protected Map<String, Object> _reqParams = null;
	public NetworkManager() {		
		_reqParams = new LinkedHashMap<String, Object>();
	}
	
	protected final static String URL_CHKPERIOD = "checkPeriod.aspx";
	protected final static String URL_REGPHONE = "regPhone.aspx";
	
	public int parseResult(String strResult) {
		if(strResult == null)
			return RESULT_ERROR;
		
		if(strResult.equals("OK")) {
			return RESULT_OK;
		} else if(strResult.equals("TimeoutCard")) {
			return RESULT_TIMEOUTCARD;
		} else if(strResult.equals("NoCard")) {
			return RESULT_NOCARD;			
		} else if(strResult.equals("CantUse")) {
			return RESULT_CANTUSE;
		} else if(strResult.equals("FirstMust12")) {
			return RESULT_FIRSTMUST12;
		} else if(strResult.equals("Exists")) {
			return RESULT_EXISTS;
		} else if(strResult.equals("StopCard")) {
			return RESULT_STOPCARD;
		}
		
		return RESULT_ERROR;
	}	
	
	protected String getServerResponse(String strUrl, Map<String, Object> params) {
		try {
			URL url = new URL(strUrl);
			
			StringBuilder postData = new StringBuilder();
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	        
	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);

	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        
	        String inputLine;
	        String repString = "";
	        
	        while ((inputLine = in.readLine()) != null) { 
	            repString = repString + inputLine;
	        }
	        in.close();
	        return repString;	        
		} catch (Exception e) {
			e.printStackTrace();
		}		
		return null;
	}
}
