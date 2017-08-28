package com.example.reviewer;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by takeshi on 2017/08/22.
 */

public class yahooJSON {
  private String data;

  public yahooJSON(String data) {
    this.data = data;
  }

  /**
   * JSON形式のデータを解析する
   * 戻り値　解析成功：result, 解析失敗：null
   */
  public String[] Name() {
    int num;

    // JSON形式の結果をパースし、名前のみ取り出す
    try {
      JSONObject rootObject = new JSONObject(data);
      JSONObject resultsetObject = rootObject.getJSONObject("ResultSet");
      num = resultsetObject.getInt("totalResultsReturned");  // 総数

      String[] result = new String[num];
      JSONObject result0Object = resultsetObject.getJSONObject("0");
      JSONObject resultObject = result0Object.getJSONObject("Result");

      // 商品名の取得
      for (int i = 0; i < num; i++) {
        JSONObject item0Object = resultObject.getJSONObject(String.valueOf(i));
        result[i] = item0Object.getString("Name");
      }
      return result;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] ProURL() {
    int num;

    // JSON形式の結果をパースし、名前のみ取り出す
    try {
      JSONObject rootObject = new JSONObject(data);
      JSONObject resultsetObject = rootObject.getJSONObject("ResultSet");
      num = resultsetObject.getInt("totalResultsReturned");  // 総数

      String[] result = new String[num];
      JSONObject result0Object = resultsetObject.getJSONObject("0");
      JSONObject resultObject = result0Object.getJSONObject("Result");

      // 商品名の取得
      for (int i = 0; i < num; i++) {
        JSONObject item0Object = resultObject.getJSONObject(String.valueOf(i));
        result[i] = item0Object.getString("Url");
      }
      return result;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] Price() {
    int num;

    // JSON形式の結果をパース
    try {
      JSONObject rootObject = new JSONObject(data);
      JSONObject resultsetObject = rootObject.getJSONObject("ResultSet");
      num = resultsetObject.getInt("totalResultsReturned");  // 総数

      String[] result = new String[num];
      JSONObject result0Object = resultsetObject.getJSONObject("0");
      JSONObject resultObject = result0Object.getJSONObject("Result");

      // 価格の取得
      for (int i = 0; i < num; i++) {
        JSONObject item0Object = resultObject.getJSONObject(String.valueOf(i));
        result[i] = item0Object.getJSONObject("Price").getString("_value");
        result[i] += "円";
      }
      return result;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] ImageURL() {
    int num;

    // JSON形式の結果をパース
    try {
      JSONObject rootObject = new JSONObject(data);
      JSONObject resultsetObject = rootObject.getJSONObject("ResultSet");
      num = resultsetObject.getInt("totalResultsReturned");  // 総数

      String[] result = new String[num];
      JSONObject result0Object = resultsetObject.getJSONObject("0");
      JSONObject resultObject = result0Object.getJSONObject("Result");

      // 価格の取得
      for (int i = 0; i < num; i++) {
        JSONObject item0Object = resultObject.getJSONObject(String.valueOf(i));
        result[i] = item0Object.getJSONObject("Image").getString("Small");
      }
      return result;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }

  public double[] Review() {
    int num;

    // JSON形式の結果をパース
    try {
      JSONObject rootObject = new JSONObject(data);
      JSONObject resultsetObject = rootObject.getJSONObject("ResultSet");
      num = resultsetObject.getInt("totalResultsReturned");  // 総数

      double[] result = new double[num];
      JSONObject result0Object = resultsetObject.getJSONObject("0");
      JSONObject resultObject = result0Object.getJSONObject("Result");

      // レビュー平均の取得
      for (int i = 0; i < num; i++) {
        JSONObject item0Object = resultObject.getJSONObject(String.valueOf(i));
        JSONObject reviewObject = item0Object.getJSONObject("Review");
        result[i] = reviewObject.getDouble("Rate");
      }
      return result;
    } catch (JSONException e) {
      e.printStackTrace();
    }

    return null;
  }


  public String[] ReviewURL() {
    int num;

    // JSON形式の結果をパース
    try {
      JSONObject rootObject = new JSONObject(data);
      JSONObject resultsetObject = rootObject.getJSONObject("ResultSet");
      num = resultsetObject.getInt("totalResultsReturned");  // 総数

      String[] result = new String[num];
      JSONObject result0Object = resultsetObject.getJSONObject("0");
      JSONObject resultObject = result0Object.getJSONObject("Result");

      // レビューURLの取得
      for (int i = 0; i < num; i++) {
        JSONObject item0Object = resultObject.getJSONObject(String.valueOf(i));
        JSONObject reviewObject = item0Object.getJSONObject("Review");
        result[i] = reviewObject.getString("Url");
      }
      return result;
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return null;
  }
}
