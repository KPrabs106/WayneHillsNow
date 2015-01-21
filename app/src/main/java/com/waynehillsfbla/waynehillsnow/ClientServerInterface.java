package com.waynehillsfbla.waynehillsnow;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Created by Kartik on 1/17/2015.
 */
public class ClientServerInterface {

    static InputStream is = null;
    static String json = "";
    static JSONArray jarr = null;


    public ClientServerInterface(){
    }

    public JSONArray makeHttpRequest(String url){
        DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Content-type", "application/json");
        try{
            HttpResponse httpresponse = httpclient.execute(httppost);
            HttpEntity httpentity = httpresponse.getEntity();
            is = httpentity.getContent();
        }catch (ClientProtocolException e){
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            try{
                while((line = reader.readLine())!= null){
                    sb.append(line+"\n");
                }
                is.close();
                json = sb.toString();
                try{
                    jarr = new JSONArray(json);
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        return jarr;
    }

    public JSONArray postData(String url, JSONObject jobj){
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 100000);
        HttpPost httppost = new HttpPost(url);

        JSONArray postjson = new JSONArray();
        postjson.put(jobj);

        try{
            httppost.setHeader("json", jobj.toString());
            httppost.getParams().setParameter("jsonpost", postjson);

            HttpResponse httpResponse = httpClient.execute(httppost);

            HttpEntity httpEntity = httpResponse.getEntity();
            is = httpEntity.getContent();

            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                try{
                    while((line = reader.readLine())!= null){
                        sb.append(line+"\n");
                    }
                    is.close();
                    json = sb.toString();
                    try{
                        jarr = new JSONArray(json);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }catch(IOException e){
                    e.printStackTrace();
                }
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return jarr;
    }
}

