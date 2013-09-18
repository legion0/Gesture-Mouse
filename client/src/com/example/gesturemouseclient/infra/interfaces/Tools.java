package com.example.gesturemouseclient.infra.interfaces;

import android.app.Activity;
import android.app.AlertDialog;

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
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		if (title != null) {
			builder.setTitle(title);
		}
		if (message != null) {
			builder.setMessage(message);
		}
		AlertDialog dialog = builder.create();
		dialog.show();
	}
}
