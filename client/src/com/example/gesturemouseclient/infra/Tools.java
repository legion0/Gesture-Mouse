package com.example.gesturemouseclient.infra;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Tools {
	public static boolean equals(String a, String b) {
		if (a == null && b == null) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}

	public static void showErrorModal(Activity activity, String title, String message) {
		showErrorModal(activity, title, message, "OK", null);
	}

	public static void showErrorModal(final Activity activity, String title, String message, String positiveButton, final Runnable runnable) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		if (title != null) {
			builder.setTitle(title);
		}
		if (message != null) {
			builder.setMessage(message);
		}
		builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
				if (runnable != null) {
					runnable.run();
				}
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
