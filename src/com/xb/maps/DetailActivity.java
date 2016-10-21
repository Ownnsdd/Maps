package com.xb.maps;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class DetailActivity extends Activity {
	
	private dbTools tool;
	private EditText nameEditText;
	private EditText phoneEditText;
	private long rowid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);
		
		tool= new dbTools(FriendsActivity.getDatabase());
		
		nameEditText = (EditText) findViewById(R.id.detail_name_edit);
		phoneEditText = (EditText) findViewById(R.id.detail_phone_edit);
		Button saveButton = (Button) findViewById(R.id.detail_save_btn);
		
		Bundle extras = getIntent().getExtras();
		if(extras!=null){
			rowid = extras.getLong("rowId");
			Friend friend = tool.findById(rowid);
			nameEditText.setText(friend.getName());
			phoneEditText.setText(friend.getPhone());
		}
		
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String message = "保存成功!";
				if(nameEditText.getText().length() !=0){
					saveFriend();
					finish();
				}else {
					message = "名字不能为空";
				}
				AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this);
				builder.setTitle("Friend");
				builder.setMessage(message);
				builder.setPositiveButton("确定", null);
				builder.show();
			}
		});
		
	}
	
	private void saveFriend(){
		Friend friend = new Friend();
		friend.setName(nameEditText.getText().toString());
		friend.setPhone(phoneEditText.getText().toString());
		if(getIntent().getExtras()==null){
			tool.insert(friend);
		}else {
			friend.setID(rowid);
			tool.update(friend);
		}
		
	}
}
