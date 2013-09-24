package com.example.gesturemouseclient.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.gesturemouseclient.R;
import com.example.gesturemouseclient.infra.KeyMap;
import com.example.gesturemouseclient.infra.TextItemPair;
import com.example.gesturemouseclient.infra.Tools;

public class CreateActionActivity extends Activity {

	private static final int REQUEST_RECORD_GESTURE = 0;
	private Button createActionBtn;
	private EditText actionEditTxt;
	private ListView keySpinner;
	private int appId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_action);
		Intent intent = getIntent();
		appId = intent.getIntExtra("app_id", -1);
		createActionBtn = (Button) findViewById(R.id.createActionDoneBtn);
		actionEditTxt = (EditText) findViewById(R.id.actionText);
		keySpinner = (ListView) findViewById(R.id.createActionKeyListView);

		List<TextItemPair<Integer>> keyList = new ArrayList<TextItemPair<Integer>>(KeyMap.KEY_MAP.size());
		for (Entry<String, Integer> entry : KeyMap.KEY_MAP.entrySet()) {
			keyList.add(new TextItemPair<Integer>(entry.getKey(), entry.getValue()));
		}

		ArrayAdapter<TextItemPair<Integer>> adapter = new ArrayAdapter<TextItemPair<Integer>>(getApplicationContext(), R.layout.spinner_view_key,
				R.id.createActionKeyName, keyList);
		keySpinner.setAdapter(adapter);

		createActionBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String action = actionEditTxt.getText().toString();
				if (checkActionValidity(action)) {
					gotoRecordGesture(action);
				} else {
					openAlertDialog("The action you entered is not valid, you have to choose actions in the following format:\n"
							+ "comma separate numbers. the number should be in the range of the actions in the list.");
				}
			}
		});
		keySpinner.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				@SuppressWarnings("unchecked")
				int key_code = ((TextItemPair<Integer>) keySpinner.getItemAtPosition(position)).getItem();
				Editable actionText = actionEditTxt.getText();
				if (actionText.length() == 0) {
					actionText.append("" + key_code);
				} else {
					actionText.append(", " + key_code);
				}
			}
		});
	}

	private boolean checkActionValidity(String action) {
		String actionStr = action.replace(" ", "");
		String[] actionSplited = actionStr.split(",");
		try {
			for (int i = 0; i < actionSplited.length; i++) {
				Integer.parseInt(actionSplited[i]);
			}
		} catch (NumberFormatException e) {
			return false;
		}

		return true;
	}

	private void openAlertDialog(String message) {
		Tools.showErrorModal(this, "Action Data Invalid", message);
		Log.w("CreateGestureActivity", " open alert should show");
	}

	protected void gotoRecordGesture(String action) {
		Intent intent = new Intent(this, CreateGestureActivity.class);
		intent.putExtra("action", action);
		intent.putExtra("app_id", appId);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_RECORD_GESTURE && resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
