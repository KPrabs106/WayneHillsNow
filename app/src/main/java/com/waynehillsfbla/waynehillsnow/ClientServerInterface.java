package com.waynehillsfbla.waynehillsnow;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * *********************************************
 * This class is the link between the Android   *
 * device and the web server. It has methods to *
 * send and receive data.                       *
 * ********************************************
 */

public class ClientServerInterface {
    private static final String BASE_URL = "http://52.4.177.235/";
    private static AsyncHttpClient asyncHttpClient = new AsyncHttpClient();

    public static void get(String url, RequestParams requestParams, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        asyncHttpClient.get(getAbsoluteUrl(url), requestParams, asyncHttpResponseHandler);
    }

    public static void post(String url, RequestParams requestParams, AsyncHttpResponseHandler asyncHttpResponseHandler) {
        asyncHttpClient.post(getAbsoluteUrl(url), requestParams, asyncHttpResponseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}