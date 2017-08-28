package com.example.reviewer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by takeshi on 2017/08/11.
 * HTTP通信を行うクラス
 */

public class WebTask extends AsyncTask<String, Void, String[]> {
  private CallBackTask callBackTask;
  private ProgressDialog progressDialog;
  private Activity activity;

  // コンストラクタ
  public WebTask(ProgressDialog progressDialog, Activity activity) {
    super();
    this.progressDialog = progressDialog;
    this.activity = activity;
  }

  @Override
  // バックグラウンドで処理を始める前に行う処理
  protected void onPreExecute() {
    progressDialog = new ProgressDialog(this.activity);
    progressDialog.setMessage("検索中...");
    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    progressDialog.setCancelable(false);
    progressDialog.show();
  }

  @Override
  // バックグラウンドで行う処理
  protected String[] doInBackground(String... params) {
    String yahooUri = params[0]; // 引数の取得
    String rakutenUri = params[1];
    String[] response = new String[2];

    try {
      // URIをURLに変換
      URL yahooUrl = new URL(yahooUri);
      URL rakutenUrl = new URL(rakutenUri);

      // HttpURLConnection インスタンス生成
      HttpURLConnection yahooUrlConnection = (HttpURLConnection) yahooUrl.openConnection();
      HttpURLConnection rakutenUrlConnection = (HttpURLConnection) rakutenUrl.openConnection();

      // タイムアウト設定
      yahooUrlConnection.setReadTimeout(10000);
      yahooUrlConnection.setConnectTimeout(20000);
      rakutenUrlConnection.setReadTimeout(10000);
      rakutenUrlConnection.setConnectTimeout(20000);

      // リクエストメソッド
      yahooUrlConnection.setRequestMethod("GET");
      rakutenUrlConnection.setRequestMethod("GET");

      // リダイレクトを自動で許可しない設定
      yahooUrlConnection.setInstanceFollowRedirects(false);
      rakutenUrlConnection.setInstanceFollowRedirects(false);

      // 接続
      yahooUrlConnection.connect();
      rakutenUrlConnection.connect();

      // レスポンスコードの取得
      int yahooRes = yahooUrlConnection.getResponseCode();
      int rakutenRes = rakutenUrlConnection.getResponseCode();

      // レスポンスがOKの場合(Yahoo)
      if (yahooRes == HttpURLConnection.HTTP_OK) {
        // レスポンスボディ(レスポンスデータ)の読み出し
        String yahooResponseData = "";
        InputStream yahooStream = yahooUrlConnection.getInputStream();
        BufferedReader yahooBr = new BufferedReader(new InputStreamReader(yahooStream, "UTF-8"));
        try {
          StringBuffer yahooSb = new StringBuffer();
          String yahooLine = "";
          while ((yahooLine = yahooBr.readLine()) != null) {
            yahooSb.append(yahooLine);
          }
          yahooResponseData = yahooSb.toString();
          response[0] = yahooResponseData;
        } finally {
          yahooStream.close();
          yahooBr.close();
        }
        // HTTP接続の切断
        yahooUrlConnection.disconnect();
      } else {
        response[0] = "Error";
        // HTTP接続の切断
        yahooUrlConnection.disconnect();
      }

      // レスポンスがOKの場合(Yahoo)
      if (rakutenRes == HttpURLConnection.HTTP_OK){
        // レスポンスデータの読出し
        String rakutenResponseData = "";
        InputStream rakutenStream = rakutenUrlConnection.getInputStream();
        BufferedReader rakutenBr = new BufferedReader(new InputStreamReader(rakutenStream, "UTF-8"));
        try {
          StringBuffer rakutenSb = new StringBuffer();
          String rakutenLine;
          while ((rakutenLine = rakutenBr.readLine()) != null) {
            rakutenSb.append(rakutenLine);
          }
          rakutenResponseData = rakutenSb.toString();
          response[1] = rakutenResponseData;
        } finally {
          rakutenStream.close();
          rakutenBr.close();
        }
        // HTTPS接続切断
        rakutenUrlConnection.disconnect();
      } else {
        response[1] = "Error";
        rakutenUrlConnection.disconnect();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    return response; // レスポンスを返す
  }

  // キャンセル時の処理
  protected void onCancelled() {
    // プログレスダイアログを閉じる
    if (progressDialog != null && progressDialog.isShowing()) {
      progressDialog.dismiss();
    }
    activity.finish();
  }

  // 終了時の処理
  protected void onPostExecute(String[] responseData) {
    callBackTask.CallBack(responseData, progressDialog);
  }

  public void setOnCallBack(CallBackTask cbj) {
    callBackTask = cbj;
  }

  /**
   * コールバック用のクラス
   */
  public static class CallBackTask {
    public void CallBack(String[] result, ProgressDialog progressDialog){

    }
  }
}