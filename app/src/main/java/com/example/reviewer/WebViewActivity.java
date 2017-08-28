package com.example.reviewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.FileOutputStream;

public class WebViewActivity extends AppCompatActivity {
  private WebView myWebView;
  private DatabeseHelper db_helper = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view);

    ////////////////////////////////////////////////////////////////////////
    // ツールバーをアクションバーとしてセット
    Toolbar toolbar = (Toolbar) findViewById(R.id.reviewtool_bar);
    toolbar.setLogo(R.mipmap.ic_launcher);
    //toolbar.setTitle("レビュー");
    ////////////////////////////////////////////////////////////////////////

    Intent intent = getIntent();
    final String name = intent.getStringExtra("Name");
    final double review = intent.getDoubleExtra("Review", 0);
    final String price = intent.getStringExtra("Price");
    final String reviewUrl = intent.getStringExtra("reviewURL");
    final String proUrl = intent.getStringExtra("proURL");
    final byte[] image = intent.getByteArrayExtra("Image");
    final Drawable img = getDrawableFromByteArray(image);

    // toolbar（実際はmenu/webviewiew.xml）にセットされたアイテムがクリックされた時の処理
    toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        // Handle the menu item
        int id = item.getItemId();

        if (id == R.id.search) {
          Intent intent = new Intent(getApplicationContext(), MainActivity.class);
          startActivity(intent);
        } else if (id == R.id.shop) {
          Intent intent = new Intent(getApplicationContext(), ShopActivity.class);
          intent.putExtra("reviewURL", reviewUrl);
          intent.putExtra("proURL", proUrl);
          startActivity(intent);
        } else if (id == R.id.fav) {
          // お気に入りに登録する
          // データベースに商品名、平均レビュー、価格、レビューURL、購入ページURLを保存
          db_helper = new DatabeseHelper(WebViewActivity.this);
          SQLiteDatabase db = db_helper.getWritableDatabase();

          //db_helper.onCreate(db);
          // テーブルが作成されていないとき、テーブルを作成する
          db_helper.CreateTable(db);
          try {
            // 既にお気に入りに登録されているか確認
            String[] params = {reviewUrl};
            Cursor cs = db.query("favorite", null, "reviewUrl = ?", params, null, null, null);
            if (cs.moveToFirst()) {
              // 既にお気に入りに登録されているとき
              Toast.makeText(WebViewActivity.this, "既にお気に入りに登録されています", Toast.LENGTH_LONG).show();
            } else {
              // お気に入りに登録されてなかったとき
              try {
                // データをデータベースに挿入する
                db_helper.InsertData(db, name, proUrl, price, review, reviewUrl);
              } catch (SQLException e) {
                e.printStackTrace();
                Log.i("insert Error", "insert");
              } finally {
                db.close();
              }

              // データベースに保存したデータのIDを取得し、png形式のファイルでサムネイル画像を保存
              try {
                db = db_helper.getReadableDatabase();
                int Id = 0;
                String[] param = {reviewUrl};
                Cursor cus = db.query("favorite", null, "reviewUrl = ?", param, null, null, null, null);
                if (cus.moveToFirst()) {
                  // データベースに保存したデータのIDを取得
                  Id = cus.getInt(cus.getColumnIndex("id"));

                  // 画像をローカルにpng形式で保存する
                  FileOutputStream out = null;
                  String fileName = "image" + Id + ".png";
                  try {
                    out = WebViewActivity.this.openFileOutput(fileName, Context.MODE_PRIVATE);
                    Bitmap bitmap = ((BitmapDrawable) img).getBitmap();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                  } catch (Exception e) {
                    e.printStackTrace();
                  } finally {
                    if (out != null) {
                      out.close();
                      out = null;
                    }
                  }
                }
                Toast.makeText(WebViewActivity.this, "お気に入りに登録しました", Toast.LENGTH_LONG).show();
              } catch (Exception e) {
                Toast.makeText(WebViewActivity.this, "お気に入りに登録できませんでした", Toast.LENGTH_LONG).show();
              } finally {
                db.close();
              }
            }
          } catch (Exception e) {
            Toast.makeText(WebViewActivity.this, "エラー", Toast.LENGTH_LONG).show();
          } finally {
            // データベースとの接続を切断する
            db.close();
          }
        }
        return true;
      }
    });
    // ツールバーにメニューをインフレート
    toolbar.inflateMenu(R.menu.webview);
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
        progressDialog = new ProgressDialog(WebViewActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
    myWebView.loadUrl(reviewUrl);
  }

  // ByteArray を Drawable に変換
  public Drawable getDrawableFromByteArray(byte[] b) {
    return new BitmapDrawable(BitmapFactory.decodeByteArray(b, 0, b.length));
  }
}
