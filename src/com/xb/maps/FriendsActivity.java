package com.xb.maps;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class FriendsActivity extends Activity {
	private static SQLiteDatabase db;
	private dbTools tool;
	private SimpleCursorAdapter friendAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_friends);
		
		db = DBconnection.open(this);
		tool = new dbTools(db);
		
		String[] from = new String[]{"name"};
		int[] to = new int[]{R.id.name_cell};
		friendAdapter = new SimpleCursorAdapter(this,R.layout.item,tool.findAll(),from,to,0);
		ListView friendLsView = (ListView) findViewById(R.id.lvw_friends_list);
		friendLsView.setAdapter(friendAdapter);
		friendLsView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent viewFriend = new Intent(FriendsActivity.this,DetailActivity.class);
				viewFriend.putExtra("rowId", id);
				startActivity(viewFriend);
			}
			
		});
		Button new_btn = (Button) findViewById(R.id.btn_friends_list_add);
		new_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(FriendsActivity.this,DetailActivity.class);
				startActivity(intent);
			}
		});
	}
	
	@Override
	protected void onResume() {
		Cursor cursor = tool.findAll();
		friendAdapter.changeCursor(cursor);
		super.onResume();
	}

	@Override
	protected void onStop() {
		friendAdapter.changeCursor(null);
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		DBconnection.close(db);
		super.onDestroy();
	}
	
	
	public static SQLiteDatabase getDatabase(){
		return db;
	}
}
