package com.example.reviewer;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
  private EditText editText;
  WebTask task;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // ボタンやテキストビューなどの設定
    ImageButton button = (ImageButton) findViewById(R.id.btn);
    editText = (EditText) findViewById(R.id.et);

    ////////////////////////////////////////////////////////////////////////
    // ツールバーをアクションバーとしてセット
    Toolbar toolbar = (Toolbar) findViewById(R.id.maintool_bar);
    //setSupportActionBar(toolbar);
    toolbar.setLogo(R.mipmap.ic_launcher);
    toolbar.setTitle("REVIEWER");

    ////////////////////////////////////////////////////////////////////////
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        // Handle the menu item
        int id = item.getItemId();

        if (id == R.id.fav) {
          Intent favorite = new Intent(getApplicationContext(), FavoriteActivity.class);
          startActivity(favorite);
        }
        return true;
      }
    });
    // ツールバーにメニューをインフレート
    toolbar.inflateMenu(R.menu.main);
    ////////////////////////////////////////////////////////////////////////

    // 商品検索ボタンが押されたときの動作
    button.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        ConnectivityManager connMgr = (ConnectivityManager) MainActivity.this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null) {

          try {
            // EditText内の文字列を読み出す
            String keyword = editText.getText().toString();

            // EditText内の文字列が空でないとき
            if (!keyword.equals("")) {
            /* ----------------- yahooURIの作成開始 ----------------- */
              String yahooScheme = "http";
              String yahooAuthority = "shopping.yahooapis.jp";
              String yahooPath = "/ShoppingWebService/V1/json/itemSearch";
              Uri.Builder yahooUriBuilder = new Uri.Builder();
              yahooUriBuilder.scheme(yahooScheme);
              yahooUriBuilder.authority(yahooAuthority);
              yahooUriBuilder.path(yahooPath);
              yahooUriBuilder.appendQueryParameter("appid", "dj00aiZpPUFnaW43cXo3bmZNRyZzPWNvbnN1bWVyc2VjcmV0Jng9M2M-");
              yahooUriBuilder.appendQueryParameter("query", keyword);
              final String yahooUri = yahooUriBuilder.toString();
            /* ----------------- yahooURIの作成終了 ----------------- */

            /* ----------------- 楽天URIの作成開始 ----------------- */
              String rakutenScheme = "https";
              String rakutenAuthority = "app.rakuten.co.jp";
              String rakutenPath = "/services/api/Product/Search/20170426";
              Uri.Builder rakutenUriBuilder = new Uri.Builder();
              rakutenUriBuilder.scheme(rakutenScheme);
              rakutenUriBuilder.authority(rakutenAuthority);
              rakutenUriBuilder.path(rakutenPath);
              rakutenUriBuilder.appendQueryParameter("format", "json");
              rakutenUriBuilder.appendQueryParameter("applicationId", "1040443002527967418");
              rakutenUriBuilder.appendQueryParameter("keyword", keyword);
              final String rakutenUri = rakutenUriBuilder.toString();
            /* ----------------- 楽天URIの作成終了 ----------------- */

              // Search_Result_Activity(検索結果画面)に遷移
              Intent intent = new Intent(getApplicationContext(), SearchResultActivity.class);
              intent.putExtra("yahooURI", yahooUri);
              intent.putExtra("rakutenURI", rakutenUri);
              startActivity(intent);
            } else {
              // 文字列が空の時
              Toast toast = Toast.makeText(MainActivity.this, "商品名を入力してください", Toast.LENGTH_LONG);
              toast.setGravity(Gravity.TOP, 0, 300);
              toast.show();
            }
          } catch (Exception e) {
            // エラー発生時
            Toast toast = Toast.makeText(MainActivity.this, "取得エラー", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.TOP, 0, 300);
            toast.show();
          }
        } else {
          // エラー発生時
          Toast toast = Toast.makeText(MainActivity.this, "ネットワークエラー", Toast.LENGTH_LONG);
          toast.setGravity(Gravity.TOP, 0, 300);
          toast.show();
        }
      }
    });
  }
}
