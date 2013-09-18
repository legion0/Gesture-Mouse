package com.example.gesturemouseclient.infra.interfaces;

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
}
