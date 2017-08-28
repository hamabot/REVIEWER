package com.example.reviewer;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by takeshi on 2017/08/23.
 */

public class rakutenJSON {
  private int num;
  JSONObject rootObject;
  JSONArray resultsArray;

  public rakutenJSON(String data) {
    try {
      rootObject = new JSONObject(data);
      num = rootObject.getInt("hits");
      resultsArray = rootObject.getJSONArray("Products");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public String[] Name() {
    try {
      String[] name = new String[num];

      for (int i = 0; i < num; i++){
        JSONObject productsObject = resultsArray.getJSONObject(i);
        JSONObject productObject = productsObject.getJSONObject("Product");
        name[i] = productObject.getString("productName");
      }
      return name;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] ProURL(){
    try {
      String[] proUrls = new String[num];

      for (int i = 0; i < num; i++){
        JSONObject productsObject = resultsArray.getJSONObject(i);
        JSONObject productObject = productsObject.getJSONObject("Product");
        proUrls[i] = productObject.getString("productUrlPC");
      }
      return proUrls;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] Price(){
    try {
      String[] price = new String[num];

      for (int i = 0; i < num; i++){
        JSONObject productsObject = resultsArray.getJSONObject(i);
        JSONObject productObject = productsObject.getJSONObject("Product");
        price[i] = productObject.getString("averagePrice");
        price[i] += "円";
      }
      return price;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public String[] ImageURL(){
    try {
      String[] imageUrls = new String[num];

      for (int i = 0; i < num; i++){
        JSONObject productsObject = resultsArray.getJSONObject(i);
        JSONObject productObject = productsObject.getJSONObject("Product");
        imageUrls[i] = productObject.getString("smallImageUrl");
      }
      return imageUrls;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  public double[] Review(){
    try {
      double[] reviews = new double[num];

      for (int i = 0; i < num; i++){
        JSONObject productsObject = resultsArray.getJSONObject(i);
        JSONObject productObject = productsObject.getJSONObject("Product");
        reviews[i] = productObject.getDouble("reviewAverage");
      }
      return reviews;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return  null;
  }

  public String[] ReviewURL(){
    try {
      String[] reviewUrls = new String[num];

      for (int i = 0; i < num; i++){
        JSONObject productsObject = resultsArray.getJSONObject(i);
        JSONObject productObject = productsObject.getJSONObject("Product");
        reviewUrls[i] = productObject.getString("reviewUrlPC");
      }
      return reviewUrls;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
}
