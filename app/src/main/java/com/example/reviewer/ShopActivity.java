package com.example.reviewer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ShopActivity extends AppCompatActivity {
  private WebView myWebView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_shop);

    Intent intent = getIntent();
    final String reviewURL = intent.getStringExtra("reviewURL");
    final String proURL = intent.getStringExtra("proURL");

    ////////////////////////////////////////////////////////////////////////
    // ツールバーをアクションバーとしてセット
    Toolbar toolbar = (Toolbar) findViewById(R.id.shoptool_bar);
    toolbar.setLogo(R.mipmap.ic_launcher);
    //toolbar.setTitle("This is title");

    // toolbar（実際はmenu/webviewiew.xml）にセットされたアイテムがクリックされた時の処理
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        // Handle the menu item
        int id = item.getItemId();

        if (id == R.id.search) {
          Intent intent = new Intent(getApplicationContext(), MainActivity.class);
          startActivity(intent);
        } else if (id == R.id.review) {
          finish();
        }
        return true;
      }
    });
    // ツールバーにメニューをインフレート
    toolbar.inflateMenu(R.menu.shop);
    ////////////////////////////////////////////////////////////////////////

    // WebViewの表示
    myWebView = (WebView) findViewById(R.id.webView);
    // JavaScriptを有効にする
    myWebView.getSettings().setJavaScriptEnabled(true);

    myWebView.setWebViewClient(new WebViewClient() {
      private ProgressDialog progressDialog;

      @Override
      public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        progressDialog = new ProgressDialog(ShopActivity.this);
        progressDialog.setMessage("読み込み中...");
        progressDialog.show();
      }

      @Override
      public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        if (progressDialog != null && progressDialog.isShowing()) {
          progressDialog.dismiss();
        }
      }
    });
    // 通信開始
    myWebView.loadUrl(proURL);
  }
}
