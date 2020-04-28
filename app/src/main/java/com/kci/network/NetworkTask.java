package com.kci.network;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;


import com.kci.dlg.LoadingDialog;

import java.util.Map;

public class NetworkTask extends AsyncTask<Map, Void, String> {

    private String strUrl, strMethod, strLoading;
    private Handler mHandler;
    private int nWhat, nStatusCode;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;

    private LoadingDialog mProgressDlg;

    public NetworkTask(String url, boolean isGet, int what, int loadTextId, Handler handler, Context context) {
        this.strUrl = url;
        this.strMethod = isGet ? "GET" : "POST";
        this.mHandler = handler;
        this.nWhat = what;
        this.strLoading = context.getString(loadTextId);
        this.mContext = context;

        mProgressDlg = new LoadingDialog(context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //mProgressDlg.show();
    }

    @Override
    protected String doInBackground(Map... maps) {
        HttpClient.Builder httpBuilder = new HttpClient.Builder(strMethod, strUrl);
        if (maps.length > 0) httpBuilder.addAllParameters(maps[0]);
        HttpClient httpClient = httpBuilder.getClient();
        httpClient.request();
        nStatusCode = httpClient.getHttpStatusCode();
        return httpClient.getBody();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if (mHandler != null) {
            Message message = mHandler.obtainMessage(nWhat);
            Bundle bundle = new Bundle();
            bundle.putString("data", result == null ? "" : result);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
        if (nStatusCode == ApiParameter.API_RESP_ERROR)
            Toast.makeText(mContext, "Server connection failed", Toast.LENGTH_SHORT).show();
        //mProgressDlg.dismiss();
    }
}
