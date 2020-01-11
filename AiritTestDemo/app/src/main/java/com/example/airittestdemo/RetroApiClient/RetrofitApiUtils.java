package com.example.airittestdemo.RetroApiClient;

import android.app.ProgressDialog;
import android.content.Context;
import com.example.airittestdemo.RetroClient.ApiClient;
import com.example.airittestdemo.SubUrlInterface.ApiInterface;
import com.example.airittestdemo.Utilities.URLs;


public class RetrofitApiUtils {
    public ProgressDialog pDialog;
    Context activity;

    public RetrofitApiUtils(Context activity) {
        this.activity = activity;
        this.pDialog = new ProgressDialog(activity);
    }

    public static ApiInterface getAPIService() {
            return ApiClient.getClient(URLs.BaseUrl).create(ApiInterface.class);
    }
}
