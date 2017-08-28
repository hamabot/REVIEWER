package com.example.reviewer;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FavoriteActivity extends AppCompatActivity {
  private DatabeseHelper db_helper = null;
  // 画像表示用 MyArratAdapter
  FavoriteActivity.MyArrayAdapter adapter = null;
  // ListView
  ListView listView = null;

  // MyArratAdapter 用プライベートクラス
  private class MyLinkData {
    String name;
    String proUrl;
    String price;
    String review;
    String reviewUrl;

    MyLinkData(String name, String proUrl, String price, String review, String reviewUrl) {
      this.name = name;
      this.proUrl = proUrl;
      this.price = price;
      this.review = review;
      this.reviewUrl = reviewUrl;
    }
  }

  // R.layout.list を使った MyLinkData 専用 ArrayAdapter
  private class MyArrayAdapter extends ArrayAdapter<FavoriteActivity.MyLinkData> {

    public MyArrayAdapter() {
      // MyArrayAdapter に必要なのは SearchViewActivity のインスタンスのみです
      // ( 実際はそれも必要ありませんが、内部の初期化の都合で渡さないとエラーになります )
      super(FavoriteActivity.this, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      // 継承元の ArrayAdapter の内部とほぼ同等の行の view の処理
      View rowView = convertView;
      if (rowView == null) {
        LayoutInflater inflater = FavoriteActivity.this.getLayoutInflater();
        rowView = inflater.inflate(R.layout.list, null);
      }

      // R.layout.list 用のデータ
      FavoriteActivity.MyLinkData myLinkData = FavoriteActivity.MyArrayAdapter.this.getItem(position);

      TextView textView = (TextView) rowView.findViewById(R.id.mark);
      if (myLinkData.proUrl.matches(".*.rakuten.*")) {
        textView.setText("楽天市場");
      } else {
        textView.setText("Yahoo!");
      }

      SQLiteDatabase database = db_helper.getReadableDatabase();
      try {
        String[] params = {myLinkData.reviewUrl};
        Cursor result = database.query("favorite", null, "reviewUrl = ?", params, null, null, null, null);
        result.moveToFirst();
        int id = result.getInt(result.getColumnIndex("id"));
        String fileName = "image" + id + ".png";
        InputStream input = null;
        try {
          input = FavoriteActivity.this.openFileInput(fileName);
        } catch (FileNotFoundException e) {
          // エラー処理
          e.printStackTrace();
          Log.i("image Error", "image");
        }
        Bitmap image = BitmapFactory.decodeStream(input);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
        imageView.setImageBitmap(image);
      } catch (SQLException e) {
        e.printStackTrace();
        Log.i("SQL Error", "Read");
      } finally {
        database.close();
      }

      // R.layout.list 用のコンテンツへデータをセット
      // 1) 名前
      TextView textView1 = (TextView) rowView.findViewById(R.id.name);
      textView1.setText(myLinkData.name);
      // 2) アイコン画像
      //ImageView imageView = (ImageView) rowView.findViewById(R.id.imageView);
      //imageView.setImageDrawable(myLinkData.myDrawable);
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
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_favorite);

    ////////////////////////////////////////////////////////////////////////
    // ツールバーをアクションバーとしてセット
    Toolbar toolbar = (Toolbar) findViewById(R.id.favoritetool_bar);
    //setSupportActionBar(toolbar);
    toolbar.setLogo(R.mipmap.ic_launcher);
    toolbar.setTitle("お気に入り");
    setSupportActionBar(toolbar);
    ////////////////////////////////////////////////////////////////////////

    listView = (ListView) FavoriteActivity.this.findViewById(R.id.listView);

    db_helper = new DatabeseHelper(FavoriteActivity.this);
    SQLiteDatabase db = db_helper.getReadableDatabase();

    try {
      final Cursor cursor = db.query("favorite", null, null, null, null, null, null);
      if (cursor.moveToFirst()) {
        final int num = cursor.getCount();
        final String[] name = new String[num], review = new String[num], price = new String[num];
        String[] reviewUrl = new String[num], proUrl = new String[num];

        for (int i = 0; i < num; i++, cursor.moveToNext()) {
          name[i] = cursor.getString(cursor.getColumnIndex("name"));
          review[i] = cursor.getString(cursor.getColumnIndex("review"));
          price[i] = cursor.getString(cursor.getColumnIndex("price"));
          reviewUrl[i] = cursor.getString(cursor.getColumnIndex("reviewUrl"));
          proUrl[i] = cursor.getString(cursor.getColumnIndex("proUrl"));
        }

        FavoriteActivity.MyLinkData[] myLinks = new FavoriteActivity.MyLinkData[num];
        for (int i = 0; i < num; i++) {
          myLinks[i] = new FavoriteActivity.MyLinkData(name[i], proUrl[i], price[i], review[i], reviewUrl[i]);
        }

        // リストビュー用のアダプターを作成
        adapter = new FavoriteActivity.MyArrayAdapter();
        adapter.addAll(myLinks);

        listView.setEmptyView(findViewById(R.id.empty));
        // 画像を全て取得してからリストビューにデータ(アダプター) をセット
        listView.setAdapter(adapter);

        // リストビューのアイテムがクリックされた時の処理
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            ListView listView = (ListView) parent;
            // クリックされたアイテムを取得
            // MyLinkData をセットしているので、MyLinkData として取り出す
            FavoriteActivity.MyLinkData item = (FavoriteActivity.MyLinkData) listView.getItemAtPosition(position);

            SQLiteDatabase database = db_helper.getReadableDatabase();
            byte[] bitMapData = null;
            try {
              String[] params = {item.reviewUrl};
              Cursor result = database.query("favorite", null, "reviewUrl = ?", params, null, null, null, null);
              result.moveToFirst();
              int Id = result.getInt(result.getColumnIndex("id"));
              String fileName = "image" + Id + ".png";
              InputStream input = null;
              try {
                input = FavoriteActivity.this.openFileInput(fileName);
              } catch (FileNotFoundException e) {
                // エラー処理
                e.printStackTrace();
                Log.i("image Error", "image");
              }
              Bitmap bitmap = BitmapFactory.decodeStream(input);
              ByteArrayOutputStream stream = new ByteArrayOutputStream();
              bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
              bitMapData = stream.toByteArray();
            } catch (SQLException e) {
              e.printStackTrace();
              Log.i("SQL Error", "Read");
            } finally {
              database.close();
            }

            // WebView を開く
            Intent intent = new Intent(getApplicationContext(), WebViewActivity.class);
            intent.putExtra("Name", item.name);
            intent.putExtra("Review", item.review);
            intent.putExtra("Price", item.price);
            intent.putExtra("Image", bitMapData);
            intent.putExtra("reviewURL", item.reviewUrl);
            intent.putExtra("proURL", item.proUrl);
            startActivity(intent);
          }
        });

        //リストの項目が長押しされた場合に呼び出されるコールバックを登録
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
          //リストの項目が長押しされた場合の処理
          @Override
          public boolean onItemLongClick(final AdapterView parent, View view, int position, final long id) {
            ListView listView = (ListView) parent;

            // クリックされたアイテムを取得
            // MyLinkData をセットしているので、MyLinkData として取り出す
            final FavoriteActivity.MyLinkData item = (FavoriteActivity.MyLinkData) listView.getItemAtPosition(position);
            AlertDialog.Builder alert = new AlertDialog.Builder(FavoriteActivity.this);

            // メッセージの設定
            alert.setMessage("お気に入りから削除しますか？");

            // 「はい」を選択したときの処理
            alert.setNegativeButton("はい", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // Yesボタンが押された時の処理
                // データベースに保存されているデータの削除
                //  商品名、平均レビュー、価格、レビューURL、購入ページURL
                db_helper = new DatabeseHelper(FavoriteActivity.this);
                SQLiteDatabase db = db_helper.getReadableDatabase();

                // 画像ファイルの削除
                try {
                  String[] params = {item.reviewUrl};
                  Cursor cus = db.query("favorite", null, "reviewUrl = ?", params, null, null, null, null);
                  cus.moveToFirst();
                  int id = cus.getInt(cus.getColumnIndex("id"));
                  String dFileName = "image" + id + ".png";
                  FavoriteActivity.this.deleteFile(dFileName);
                  db.delete("favorite", "reviewUrl = ?", params);
                  Toast.makeText(FavoriteActivity.this, "お気に入りから削除しました", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                  Toast.makeText(FavoriteActivity.this, "お気に入りから削除できませんでした", Toast.LENGTH_LONG).show();
                } finally {
                  // データベースとの接続を切断する
                  db.close();
                }
                // 要素を削除し、リストを更新
                adapter.remove(item);
                adapter.notifyDataSetChanged();
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
    } catch (Exception e) {
      e.printStackTrace();
      Log.i("SQL Error", "Read Error");
    } finally {
      // データベースとの接続を切断
      db.close();
    }
  }
}
