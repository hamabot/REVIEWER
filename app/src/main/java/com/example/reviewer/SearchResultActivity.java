package com.example.reviewer;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;

public class SearchResultActivity extends AppCompatActivity {
  // 画像表示用 MyArratAdapter
  MyArrayAdapter adapter = null;
  // ListView
  ListView listView = null;
  private ProgressDialog progressDialog;
  private WebTask task;
  private DatabeseHelper db_helper = null;

  // MyArratAdapter 用プライベートクラス
  private class MyLinkData {
    String name;
    String proUrl;
    String price;
    double review;
    String reviewUrl;
    String imageUrl;
    Drawable myDrawable = null;

    MyLinkData(String name, String proUrl, String price, double review, String reviewUrl, String imageUrl) {
      this.name = name;
      this.proUrl = proUrl;
      this.price = price;
      this.review = review;
      this.reviewUrl = reviewUrl;
      this.imageUrl = imageUrl;
    }
  }

  // R.layout.list を使った MyLinkData 専用 ArrayAdapter
  private class MyArrayAdapter extends ArrayAdapter<MyLinkData> {

    public MyArrayAdapter() {
      // MyArrayAdapter に必要なのは SearchViewActivity のインスタンスのみです
      // ( 実際はそれも必要ありませんが、内部の初期化の都合で渡さないとエラーになります )
      super(SearchResultActivity.this, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // 継承元の ArrayAdapter の内部とほぼ同等の行の view の処理
      View rowView = convertView;
      if (rowView == null) {
        LayoutInflater inflater = SearchResultActivity.this.getLayoutInflater();
        rowView = inflater.inflate(R.layout.list, null);
      }

      // R.layout.list 用のデータ
      MyLinkData myLinkData = MyArrayAdapter.this.getItem(position);

      TextView textView = (TextView) rowView.findViewById(R.id.mark);
      if (myLinkData.proUrl.matches(".*.rakuten.*")) {
        textView.setText("楽天市場");
      } else {
        textView.setText("Yahoo!");
      }

      // R.layout.list 用のコンテンツへデータをセット
      // 1) 名前
      TextView textView1 = (TextView) rowView.findViewById(R.id.name);
      textView1.setText(myLinkData.name);
      // 2) アイコン画像
      ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
      imageView.setImageDrawable(myLinkData.myDrawable);
      // 3) レビュー
      TextView textView2 = (TextView) rowView.findViewById(R.id.review);
      textView2.setText(String.valueOf(myLinkData.review));
      // 4) 価格
      TextView textView3 = (TextView) rowView.findViewById(R.id.price);
      textView3.setText(myLinkData.price);

      return rowView;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search_result);

    ////////////////////////////////////////////////////////////////////////
    // ツールバーをアクションバーとしてセット
    Toolbar toolbar = (Toolbar) findViewById(R.id.searchtool_bar);
    //setSupportActionBar(toolbar);
    toolbar.setLogo(R.mipmap.ic_launcher);
    toolbar.setTitle("検索結果");
    setSupportActionBar(toolbar);
    ////////////////////////////////////////////////////////////////////////

    listView = (ListView) SearchResultActivity.this.findViewById(R.id.listView);

    final Intent intent = getIntent();
    String yahooUri = intent.getStringExtra("yahooURI");
    String rakutenUri = intent.getStringExtra("rakutenURI");

    task = new WebTask(progressDialog, SearchResultActivity.this);
    task.setOnCallBack(new WebTask.CallBackTask() {
      @Override
      public void CallBack(String[] str, ProgressDialog pDialog) {
        // strにはonPostExecuteで設定した戻り値が入る
        super.CallBack(str, pDialog);
        progressDialog = pDialog;

        if (str[0] == "Error" && str[1] == "Error") {
          Toast toast = Toast.makeText(SearchResultActivity.this, "エラー", Toast.LENGTH_LONG);
          toast.setGravity(Gravity.TOP, 0, 300);
          toast.show();
          Intent intent = new Intent(getApplicationContext(), MainActivity.class);
          startActivity(intent);
        } else {
          /* ------------------- YahooAPIのJson解析開始 ------------------- */
          String[] yahooNames, yahooProUrls, yahooPrices, yahooImgUrls, yahooReviewUrls;
          double[] yahooReviews;
          yahooJSON yahoo_json = new yahooJSON(str[0]);

          // 各種情報の抽出
          yahooNames = yahoo_json.Name();
          yahooProUrls = yahoo_json.ProURL();
          yahooPrices = yahoo_json.Price();
          yahooReviews = yahoo_json.Review();
          yahooImgUrls = yahoo_json.ImageURL();
          yahooReviewUrls = yahoo_json.ReviewURL();
          /* ------------------- YahooAPIのJson解析終了 ------------------- */

          /* ------------------- 楽天APIのJson解析開始 ------------------- */
          String[] rakutenNames, rakutenProUrls, rakutenPrices, rakutenImgUrls, rakutenReviewUrls;
          double[] rakutenReviews;
          rakutenJSON rakuten_json = new rakutenJSON(str[1]);
          rakutenNames = rakuten_json.Name();
          rakutenProUrls = rakuten_json.ProURL();
          rakutenPrices = rakuten_json.Price();
          rakutenImgUrls = rakuten_json.ImageURL();
          rakutenReviewUrls = rakuten_json.ReviewURL();
          rakutenReviews = rakuten_json.Review();
          /* ------------------- 楽天APIのJson解析終了 ------------------- */

          //* ------------------- Yahooと楽天の結果をまとめる ------------------- */
          int length = yahooNames.length + rakutenNames.length;
          String[] names = new String[length], proUrls = new String[length], prices = new String[length];
          String[] imgUrls = new String[length], reviewUrls = new String[length];
          double[] reviews = new double[length];
          System.arraycopy(yahooNames, 0, names, 0, yahooNames.length);
          System.arraycopy(rakutenNames, 0, names, yahooNames.length, rakutenNames.length);
          System.arraycopy(yahooProUrls, 0, proUrls, 0, yahooNames.length);
          System.arraycopy(rakutenProUrls, 0, proUrls, yahooNames.length, rakutenNames.length);
          System.arraycopy(yahooPrices, 0, prices, 0, yahooNames.length);
          System.arraycopy(rakutenPrices, 0, prices, yahooNames.length, rakutenNames.length);
          System.arraycopy(yahooImgUrls, 0, imgUrls, 0, yahooNames.length);
          System.arraycopy(rakutenImgUrls, 0, imgUrls, yahooNames.length, rakutenNames.length);
          System.arraycopy(yahooReviews, 0, reviews, 0, yahooNames.length);
          System.arraycopy(rakutenReviews, 0, reviews, yahooNames.length, rakutenNames.length);
          System.arraycopy(yahooReviewUrls, 0, reviewUrls, 0, yahooNames.length);
          System.arraycopy(rakutenReviewUrls, 0, reviewUrls, yahooNames.length, rakutenNames.length);

          // 平均レビューの高い順に表示
          int keyIndex;
          for (int i = 0; i < length - 1; i++) {
            keyIndex = i;
            for (int j = i + 1; j < length; j++) {
              if (reviews[keyIndex] < reviews[j]) {
                keyIndex = j;
              }
            }
            if (i == keyIndex) {
              continue;
            } else {
              double reviewTemp = reviews[i];
              reviews[i] = reviews[keyIndex];
              reviews[keyIndex] = reviewTemp;

              String temp = names[i];
              names[i] = names[keyIndex];
              names[keyIndex] = temp;

              temp = proUrls[i];
              proUrls[i] = proUrls[keyIndex];
              proUrls[keyIndex] = temp;

              temp = prices[i];
              prices[i] = prices[keyIndex];
              prices[keyIndex] = temp;

              temp = imgUrls[i];
              imgUrls[i] = imgUrls[keyIndex];
              imgUrls[keyIndex] = temp;

              temp = reviewUrls[i];
              reviewUrls[i] = reviewUrls[keyIndex];
              reviewUrls[keyIndex] = temp;
            }
          }


          MyLinkData[] myLinks = new MyLinkData[length];
          for (int i = 0; i < length; i++) {
            myLinks[i] = new MyLinkData(names[i], proUrls[i], prices[i], reviews[i], reviewUrls[i], imgUrls[i]);
          }

          // リストビュー用のアダプターを作成
          adapter = new MyArrayAdapter();
          adapter.addAll(myLinks);

          // 非同期インターネットアクセス
          // 画像はあらかじめ全て取得
          new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
              int count = adapter.getCount();
              for (int i = 0; i < count; i++) {
                try {
                  URL url = new URL(adapter.getItem(i).imageUrl);
                  InputStream is = (InputStream) url.getContent();
                  adapter.getItem(i).myDrawable = Drawable.createFromStream(is, "");
                } catch (Exception e) {
                  Log.i("lightbox", "error:" + adapter.getItem(i).imageUrl);
                }
              }
              return null;
            }

            protected void onCancelled() {
              // プログレスダイアログを閉じる
              if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
              }
              finish();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
              listView.setEmptyView(findViewById(R.id.empty));
              // 画像を全て取得してからリストビューにデータ(アダプター) をセット
              listView.setAdapter(adapter);
              // プログレスダイアログを閉じる
              if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
              }
            }
          }.execute();
        }

        // リストビューのアイテムがクリックされた時の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            // クリックされたアイテムを取得
            // MyLinkData をセットしているので、MyLinkData として取り出す
            MyLinkData item = (MyLinkData) listView.getItemAtPosition(position);
            // LogCatに表示
            Log.i("lightbox", item.reviewUrl);
            Log.i("lightbox", item.name);
            Log.i("lightbox", Integer.toString(position));

            // WebView を開く
            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
            intent.putExtra("Name", item.name);
            intent.putExtra("Review", item.review);
            intent.putExtra("Price", item.price);
            intent.putExtra("Image", getByteArrayFromDrawable(item.myDrawable));
            intent.putExtra("reviewURL", item.reviewUrl);
            intent.putExtra("proURL", item.proUrl);
            startActivity(intent);
          }
        });

        //リストの項目が長押しされた場合に呼び出されるコールバックを登録
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
          //リストの項目が長押しされた場合の処理
          @Override
          public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
            ListView listView = (ListView) parent;

            // クリックされたアイテムを取得
            // MyLinkData をセットしているので、MyLinkData として取り出す
            final MyLinkData item = (MyLinkData) listView.getItemAtPosition(position);
            AlertDialog.Builder alert = new AlertDialog.Builder(SearchResultActivity.this);

            // メッセージの設定
            alert.setMessage("お気に入り登録しますか？");

            // 「はい」を選択したときの処理
            alert.setNegativeButton("はい", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // Yesボタンが押された時の処理
                // データベースに商品名、平均レビュー、価格、レビューURL、購入ページURLを保存
                db_helper = new DatabeseHelper(SearchResultActivity.this);
                SQLiteDatabase db = db_helper.getWritableDatabase();

                //db_helper.onCreate(db);
                // テーブルが作成されていないとき、テーブルを作成する
                db_helper.CreateTable(db);
                try {
                  // 既にお気に入りに登録されているか確認
                  String[] params = {item.reviewUrl};
                  Cursor cs = db.query("favorite", null, "reviewUrl = ?", params, null, null, null);
                  if (cs.moveToFirst()) {
                    // 既にお気に入りに登録されているとき
                    Toast.makeText(SearchResultActivity.this, "既にお気に入りに登録されています", Toast.LENGTH_LONG).show();
                  } else {
                    // お気に入りに登録されてなかったとき
                    try {
                      // データをデータベースに挿入する
                      db_helper.InsertData(db, item.name, item.proUrl, item.price, item.review, item.reviewUrl);
                    } catch (SQLException e) {
                      e.printStackTrace();
                      Log.i("insert Error", "insert");
                    } finally {
                      db.close();
                    }

                    // データベースに保存したデータのIDを取得し、png形式のファイルでサムネイル画像を保存
                    try {
                      db = db_helper.getReadableDatabase();
                      int id = 0;
                      String[] param = {item.reviewUrl};
                      Cursor cus = db.query("favorite", null, "reviewUrl = ?", param, null, null, null, null);
                      if (cus.moveToFirst()) {
                        // データベースに保存したデータのIDを取得
                        id = cus.getInt(cus.getColumnIndex("id"));

                        // 画像をローカルにpng形式で保存する
                        FileOutputStream out = null;
                        String fileName = "image" + id + ".png";
                        try {
                          out = SearchResultActivity.this.openFileOutput(fileName, Context.MODE_PRIVATE);
                          Bitmap bitmap = ((BitmapDrawable) item.myDrawable).getBitmap();
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
                      Toast.makeText(SearchResultActivity.this, "お気に入りに登録しました", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                      Toast.makeText(SearchResultActivity.this, "お気に入りに登録できませんでした", Toast.LENGTH_LONG).show();
                    } finally {
                      db.close();
                    }
                  }
                } catch (Exception e) {
                  Toast.makeText(SearchResultActivity.this, "エラー", Toast.LENGTH_LONG).show();
                } finally {
                  // データベースとの接続を切断する
                  db.close();
                }
              }
            });

            // 「いいえ」を選択したときの処理
            alert.setPositiveButton("いいえ", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // Noボタンが押された時の処理
                // なにもしない
              }
            });
            alert.show();

            return true;
          }
        });
      }
    });
    task.execute(yahooUri, rakutenUri);
  }

  public byte[] getByteArrayFromDrawable(Drawable d) {
    Bitmap bitmap = ((BitmapDrawable) d).getBitmap();
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    byte[] bitMapData = stream.toByteArray();
    return bitMapData;
  }
}