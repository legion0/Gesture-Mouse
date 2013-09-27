package us.to.gesturemouse.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import us.to.gesturemouse.infra.KeyMap;
import us.to.gesturemouse.infra.TextItemPair;
import us.to.gesturemouse.infra.Tools;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.gesturemouseclient.R;

public class CreateActionActivity extends Activity {

	private static final int REQUEST_RECORD_GESTURE = 0;
	private ImageView createActionBtn;
	private EditText actionEditTxt;
	private ListView keySpinner;
	private int appId;
	private String processName;
	private String windowTitle;
	private String applicationName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_action);

		// disable rotation and keep screen on.
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

		Intent intent = getIntent();
		appId = intent.getIntExtra("app_id", -1);
		applicationName = intent.getStringExtra("app_name");
		windowTitle = intent.getStringExtra("window_title");
		processName = intent.getStringExtra("process_name");
		createActionBtn = (ImageView) findViewById(R.id.createActionDoneBtn);
		actionEditTxt = (EditText) findViewById(R.id.actionText);
		keySpinner = (ListView) findViewById(R.id.createActionKeyListView);

		List<TextItemPair<Integer>> keyList = new ArrayList<TextItemPair<Integer>>(KeyMap.KEY_MAP.size());
		for (Entry<String, Integer> entry : KeyMap.KEY_MAP.entrySet()) {
			keyList.add(new TextItemPair<Integer>(entry.getKey().replace("VK_", "")+" Press", entry.getValue()));
			keyList.add(new TextItemPair<Integer>(entry.getKey().replace("VK_", "")+" Hold", KeyMap.holdKey(entry.getValue())));
			keyList.add(new TextItemPair<Integer>(entry.getKey().replace("VK_", "")+" Release", KeyMap.releaseKey(entry.getValue())));
		}

		ArrayAdapter<TextItemPair<Integer>> adapter = new ArrayAdapter<TextItemPair<Integer>>(this, R.layout.spinner_view_key,
				R.id.createActionKeyName, keyList);
		keySpinner.setAdapter(adapter);

		createActionBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("CreateActionActivity","on click to create action");
				int[] action = parseActionString(actionEditTxt.getText().toString());
				if (action.length > 0 ) {
					Log.d("CreateActionActivity","length: "+action.length);
					Log.d("CreateActionActivity","action[0]: "+action[0]);
					gotoRecordGesture(action);
				} else {
					Log.d("CreateActionActivity"," should error");
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

	private int[] parseActionString(String actionString) {
		actionString = actionString.replace(" ", "");
		//		if ("".equals(actionString)) {
		//			return new int[0];
		//		}
		String[] actionSplited = actionString.split(",");
		int[] action = new int[actionSplited.length];
		try {
			for (int i = 0; i < actionSplited.length; i++) {
				action[i] = Integer.parseInt(actionSplited[i]);
			}
		} catch (NumberFormatException e) {
			return new int[0];
		}
		return action;
	}

	private void openAlertDialog(String message) {
		Tools.showErrorModal(this, "Action Data Invalid", message);
		Log.w("CreateGestureActivity", " open alert should show");
	}

	protected void gotoRecordGesture(int[] action) {
		Intent intent = new Intent(this, CreateGestureActivity.class);
		intent.putExtra("action", action);
		intent.putExtra("app_id", appId);
		intent.putExtra("app_name", applicationName);
		intent.putExtra("window_title", windowTitle);
		intent.putExtra("process_name", processName);
		startActivityForResult(intent, REQUEST_RECORD_GESTURE);
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
