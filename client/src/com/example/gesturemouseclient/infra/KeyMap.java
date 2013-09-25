package com.example.gesturemouseclient.infra;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import android.view.KeyEvent;

public class KeyMap {

	private static int MOD_HOLD = 1 << 16;
	private static int MOD_RELEASE = 2 << 16;

	public static int holdKey(int key) {
		return key | MOD_HOLD;
	}

	public static int releaseKey(int key) {
		return key | MOD_RELEASE;
	}

	public static final int VK_LBUTTON = 1;
	public static final int VK_RBUTTON = 2;
	public static final int VK_CANCEL = 3;
	public static final int VK_MBUTTON = 4;
	public static final int VK_BACK = 8;
	public static final int VK_TAB = 9;
	public static final int VK_CLEAR = 12;
	public static final int VK_RETURN = 13;
	public static final int VK_SHIFT = 16;
	public static final int VK_CONTROL = 17;
	public static final int VK_MENU = 18;
	public static final int VK_PAUSE = 19;
	public static final int VK_CAPITAL = 20;
	public static final int VK_KANA = 21;
	public static final int VK_HANGEUL = 21;
	public static final int VK_HANGUL = 21;
	public static final int VK_JUNJA = 23;
	public static final int VK_FINAL = 24;
	public static final int VK_HANJA = 25;
	public static final int VK_KANJI = 25;
	public static final int VK_ESCAPE = 27;
	public static final int VK_CONVERT = 28;
	public static final int VK_NONCONVERT = 29;
	public static final int VK_ACCEPT = 30;
	public static final int VK_MODECHANGE = 31;
	public static final int VK_SPACE = 32;
	public static final int VK_PRIOR = 33;
	public static final int VK_NEXT = 34;
	public static final int VK_END = 35;
	public static final int VK_HOME = 36;
	public static final int VK_LEFT = 37;
	public static final int VK_UP = 38;
	public static final int VK_RIGHT = 39;
	public static final int VK_DOWN = 40;
	public static final int VK_SELECT = 41;
	public static final int VK_PRINT = 42;
	public static final int VK_EXECUTE = 43;
	public static final int VK_SNAPSHOT = 44;
	public static final int VK_INSERT = 45;
	public static final int VK_DELETE = 46;
	public static final int VK_HELP = 47;
	public static final int VK_LWIN = 91;
	public static final int VK_RWIN = 92;
	public static final int VK_APPS = 93;
	public static final int VK_NUMPAD0 = 96;
	public static final int VK_NUMPAD1 = 97;
	public static final int VK_NUMPAD2 = 98;
	public static final int VK_NUMPAD3 = 99;
	public static final int VK_NUMPAD4 = 100;
	public static final int VK_NUMPAD5 = 101;
	public static final int VK_NUMPAD6 = 102;
	public static final int VK_NUMPAD7 = 103;
	public static final int VK_NUMPAD8 = 104;
	public static final int VK_NUMPAD9 = 105;
	public static final int VK_MULTIPLY = 106;
	public static final int VK_ADD = 107;
	public static final int VK_SEPARATOR = 108;
	public static final int VK_SUBTRACT = 109;
	public static final int VK_DECIMAL = 110;
	public static final int VK_DIVIDE = 111;
	public static final int VK_F1 = 112;
	public static final int VK_F2 = 113;
	public static final int VK_F3 = 114;
	public static final int VK_F4 = 115;
	public static final int VK_F5 = 116;
	public static final int VK_F6 = 117;
	public static final int VK_F7 = 118;
	public static final int VK_F8 = 119;
	public static final int VK_F9 = 120;
	public static final int VK_F10 = 121;
	public static final int VK_F11 = 122;
	public static final int VK_F12 = 123;
	public static final int VK_F13 = 124;
	public static final int VK_F14 = 125;
	public static final int VK_F15 = 126;
	public static final int VK_F16 = 127;
	public static final int VK_F17 = 128;
	public static final int VK_F18 = 129;
	public static final int VK_F19 = 130;
	public static final int VK_F20 = 131;
	public static final int VK_F21 = 132;
	public static final int VK_F22 = 133;
	public static final int VK_F23 = 134;
	public static final int VK_F24 = 135;
	public static final int VK_NUMLOCK = 144;
	public static final int VK_SCROLL = 145;
	public static final int VK_LSHIFT = 160;
	public static final int VK_RSHIFT = 161;
	public static final int VK_LCONTROL = 162;
	public static final int VK_RCONTROL = 163;
	public static final int VK_LMENU = 164;
	public static final int VK_RMENU = 165;
	public static final int VK_PROCESSKEY = 229;
	public static final int VK_ATTN = 246;
	public static final int VK_CRSEL = 247;
	public static final int VK_EXSEL = 248;
	public static final int VK_EREOF = 249;
	public static final int VK_PLAY = 250;
	public static final int VK_ZOOM = 251;
	public static final int VK_NONAME = 252;
	public static final int VK_PA1 = 253;
	public static final int VK_OEM_CLEAR = 254;
	public static final int VK_MOUSEEVENTF_XDOWN = 0x0080;
	public static final int VK_MOUSEEVENTF_XUP = 0x0100;
	public static final int VK_MOUSEEVENTF_WHEEL = 0x0800;
	public static final int VK_XBUTTON1 = 0x05;
	public static final int VK_XBUTTON2 = 0x06;
	public static final int VK_VOLUME_MUTE = 0xAD;
	public static final int VK_VOLUME_DOWN = 0xAE;
	public static final int VK_VOLUME_UP = 0xAF;
	public static final int VK_MEDIA_NEXT_TRACK = 0xB0;
	public static final int VK_MEDIA_PREV_TRACK = 0xB1;
	public static final int VK_MEDIA_PLAY_PAUSE = 0xB3;
	public static final int VK_BROWSER_BACK = 0xA6;
	public static final int VK_BROWSER_FORWARD = 0xA7;

	public static final int VK_A = 65;
	public static final int VK_B = 66;
	public static final int VK_C = 67;
	public static final int VK_D = 68;
	public static final int VK_E = 69;
	public static final int VK_F = 70;
	public static final int VK_G = 71;
	public static final int VK_H = 72;
	public static final int VK_I = 73;
	public static final int VK_J = 74;
	public static final int VK_K = 75;
	public static final int VK_L = 76;
	public static final int VK_M = 77;
	public static final int VK_N = 78;
	public static final int VK_O = 79;
	public static final int VK_P = 80;
	public static final int VK_Q = 81;
	public static final int VK_R = 82;
	public static final int VK_S = 83;
	public static final int VK_T = 84;
	public static final int VK_U = 85;
	public static final int VK_V = 86;
	public static final int VK_W = 87;
	public static final int VK_X = 88;
	public static final int VK_Y = 89;
	public static final int VK_Z = 90;
	public static final int VK_0 = 48;
	public static final int VK_1 = 49;
	public static final int VK_2 = 50;
	public static final int VK_3 = 51;
	public static final int VK_4 = 52;
	public static final int VK_5 = 53;
	public static final int VK_6 = 54;
	public static final int VK_7 = 55;
	public static final int VK_8 = 56;
	public static final int VK_9 = 57;

	public static final int VK_OEM_1 = 0xBA;
	public static final int VK_OEM_PLUS = 0xBB;
	public static final int VK_OEM_COMMA = 0xBC;
	public static final int VK_OEM_MINUS = 0xBD;
	public static final int VK_OEM_PERIOD = 0xBE;
	public static final int VK_OEM_2 = 0xBF;
	public static final int VK_OEM_3 = 0xC0;
	public static final int VK_OEM_4 = 0xDB;
	public static final int VK_OEM_5 = 0xDC;
	public static final int VK_OEM_6 = 0xDD;
	public static final int VK_OEM_7 = 0xDE;
	public static final int VK_OEM_8 = 0xDF;


	public static final Map<String, Integer> KEY_MAP;
	public static final Map<Integer, Integer> ANDROID_TO_WINDOWS_KEY_MAP;
	static {
		LinkedHashMap<String, Integer> tempMap = new LinkedHashMap<String, Integer>();
		tempMap.put("VK_LBUTTON", 1);
		tempMap.put("VK_RBUTTON", 2);
		tempMap.put("VK_CANCEL", 3);
		tempMap.put("VK_MBUTTON", 4);
		tempMap.put("VK_BACK", 8);
		tempMap.put("VK_TAB", 9);
		tempMap.put("VK_CLEAR", 12);
		tempMap.put("VK_RETURN", 13);
		tempMap.put("VK_SHIFT", 16);
		tempMap.put("VK_CONTROL", 17);
		tempMap.put("VK_MENU", 18);
		tempMap.put("VK_PAUSE", 19);
		tempMap.put("VK_CAPITAL", 20);
		tempMap.put("VK_KANA", 21);
		tempMap.put("VK_HANGEUL", 21);
		tempMap.put("VK_HANGUL", 21);
		tempMap.put("VK_JUNJA", 23);
		tempMap.put("VK_FINAL", 24);
		tempMap.put("VK_HANJA", 25);
		tempMap.put("VK_KANJI", 25);
		tempMap.put("VK_ESCAPE", 27);
		tempMap.put("VK_CONVERT", 28);
		tempMap.put("VK_NONCONVERT", 29);
		tempMap.put("VK_ACCEPT", 30);
		tempMap.put("VK_MODECHANGE", 31);
		tempMap.put("VK_SPACE", 32);
		tempMap.put("VK_PRIOR", 33);
		tempMap.put("VK_NEXT", 34);
		tempMap.put("VK_END", 35);
		tempMap.put("VK_HOME", 36);
		tempMap.put("VK_LEFT", 37);
		tempMap.put("VK_UP", 38);
		tempMap.put("VK_RIGHT", 39);
		tempMap.put("VK_DOWN", 40);
		tempMap.put("VK_SELECT", 41);
		tempMap.put("VK_PRINT", 42);
		tempMap.put("VK_EXECUTE", 43);
		tempMap.put("VK_SNAPSHOT", 44);
		tempMap.put("VK_INSERT", 45);
		tempMap.put("VK_DELETE", 46);
		tempMap.put("VK_HELP", 47);
		tempMap.put("VK_LWIN", 91);
		tempMap.put("VK_RWIN", 92);
		tempMap.put("VK_APPS", 93);
		tempMap.put("VK_NUMPAD0", 96);
		tempMap.put("VK_NUMPAD1", 97);
		tempMap.put("VK_NUMPAD2", 98);
		tempMap.put("VK_NUMPAD3", 99);
		tempMap.put("VK_NUMPAD4", 100);
		tempMap.put("VK_NUMPAD5", 101);
		tempMap.put("VK_NUMPAD6", 102);
		tempMap.put("VK_NUMPAD7", 103);
		tempMap.put("VK_NUMPAD8", 104);
		tempMap.put("VK_NUMPAD9", 105);
		tempMap.put("VK_MULTIPLY", 106);
		tempMap.put("VK_ADD", 107);
		tempMap.put("VK_SEPARATOR", 108);
		tempMap.put("VK_SUBTRACT", 109);
		tempMap.put("VK_DECIMAL", 110);
		tempMap.put("VK_DIVIDE", 111);
		tempMap.put("VK_F1", 112);
		tempMap.put("VK_F2", 113);
		tempMap.put("VK_F3", 114);
		tempMap.put("VK_F4", 115);
		tempMap.put("VK_F5", 116);
		tempMap.put("VK_F6", 117);
		tempMap.put("VK_F7", 118);
		tempMap.put("VK_F8", 119);
		tempMap.put("VK_F9", 120);
		tempMap.put("VK_F10", 121);
		tempMap.put("VK_F11", 122);
		tempMap.put("VK_F12", 123);
		tempMap.put("VK_F13", 124);
		tempMap.put("VK_F14", 125);
		tempMap.put("VK_F15", 126);
		tempMap.put("VK_F16", 127);
		tempMap.put("VK_F17", 128);
		tempMap.put("VK_F18", 129);
		tempMap.put("VK_F19", 130);
		tempMap.put("VK_F20", 131);
		tempMap.put("VK_F21", 132);
		tempMap.put("VK_F22", 133);
		tempMap.put("VK_F23", 134);
		tempMap.put("VK_F24", 135);
		tempMap.put("VK_NUMLOCK", 144);
		tempMap.put("VK_SCROLL", 145);
		tempMap.put("VK_LSHIFT", 160);
		tempMap.put("VK_RSHIFT", 161);
		tempMap.put("VK_LCONTROL", 162);
		tempMap.put("VK_RCONTROL", 163);
		tempMap.put("VK_LMENU", 164);
		tempMap.put("VK_RMENU", 165);
		tempMap.put("VK_PROCESSKEY", 229);
		tempMap.put("VK_ATTN", 246);
		tempMap.put("VK_CRSEL", 247);
		tempMap.put("VK_EXSEL", 248);
		tempMap.put("VK_EREOF", 249);
		tempMap.put("VK_PLAY", 250);
		tempMap.put("VK_ZOOM", 251);
		tempMap.put("VK_NONAME", 252);
		tempMap.put("VK_PA1", 253);
		tempMap.put("VK_OEM_CLEAR", 254);
		tempMap.put("VK_MOUSEEVENTF_XDOWN", 0x0080);
		tempMap.put("VK_MOUSEEVENTF_XUP", 0x0100);
		tempMap.put("VK_MOUSEEVENTF_WHEEL", 0x0800);
		tempMap.put("VK_XBUTTON1", 0x05);
		tempMap.put("VK_XBUTTON2", 0x06);
		tempMap.put("VK_VOLUME_MUTE", 0xAD);
		tempMap.put("VK_VOLUME_DOWN", 0xAE);
		tempMap.put("VK_VOLUME_UP", 0xAF);
		tempMap.put("VK_MEDIA_NEXT_TRACK", 0xB0);
		tempMap.put("VK_MEDIA_PREV_TRACK", 0xB1);
		tempMap.put("VK_MEDIA_PLAY_PAUSE", 0xB3);
		tempMap.put("VK_BROWSER_BACK", 0xA6);
		tempMap.put("VK_BROWSER_FORWARD", 0xA7);

		tempMap.put("VK_A", 65);
		tempMap.put("VK_B", 66);
		tempMap.put("VK_C", 67);
		tempMap.put("VK_D", 68);
		tempMap.put("VK_E", 69);
		tempMap.put("VK_F", 70);
		tempMap.put("VK_G", 71);
		tempMap.put("VK_H", 72);
		tempMap.put("VK_I", 73);
		tempMap.put("VK_J", 74);
		tempMap.put("VK_K", 75);
		tempMap.put("VK_L", 76);
		tempMap.put("VK_M", 77);
		tempMap.put("VK_N", 78);
		tempMap.put("VK_O", 79);
		tempMap.put("VK_P", 80);
		tempMap.put("VK_Q", 81);
		tempMap.put("VK_R", 82);
		tempMap.put("VK_S", 83);
		tempMap.put("VK_T", 84);
		tempMap.put("VK_U", 85);
		tempMap.put("VK_V", 86);
		tempMap.put("VK_W", 87);
		tempMap.put("VK_X", 88);
		tempMap.put("VK_Y", 89);
		tempMap.put("VK_Z", 90);
		tempMap.put("VK_0", 48);
		tempMap.put("VK_1", 49);
		tempMap.put("VK_2", 50);
		tempMap.put("VK_3", 51);
		tempMap.put("VK_4", 52);
		tempMap.put("VK_5", 53);
		tempMap.put("VK_6", 54);
		tempMap.put("VK_7", 55);
		tempMap.put("VK_8", 56);
		tempMap.put("VK_9", 57);

		tempMap.put("VK_OEM_1",0xBA);
		tempMap.put("VK_OEM_PLUS",0xBB);
		tempMap.put("VK_OEM_COMMA",0xBC);
		tempMap.put("VK_OEM_MINUS",0xBD);
		tempMap.put("VK_OEM_PERIOD",0xBE);
		tempMap.put("VK_OEM_2",0xBF);
		tempMap.put("VK_OEM_3",0xC0);
		tempMap.put("VK_OEM_4",0xDB);
		tempMap.put("VK_OEM_5",0xDC);
		tempMap.put("VK_OEM_6",0xDD);
		tempMap.put("VK_OEM_7",0xDE);
		tempMap.put("VK_OEM_8",0xDF);

		KEY_MAP = Collections.unmodifiableMap(tempMap);
		
		LinkedHashMap<Integer, Integer> tempMap2 = new LinkedHashMap<Integer, Integer>();
		tempMap2.put(KeyEvent.KEYCODE_SOFT_LEFT, VK_LEFT);
		tempMap2.put(KeyEvent.KEYCODE_SOFT_RIGHT, VK_RIGHT);
		tempMap2.put(KeyEvent.KEYCODE_HOME, VK_HOME);
		tempMap2.put(KeyEvent.KEYCODE_0, VK_0);
		tempMap2.put(KeyEvent.KEYCODE_1, VK_1);
		tempMap2.put(KeyEvent.KEYCODE_2, VK_2);
		tempMap2.put(KeyEvent.KEYCODE_3, VK_3);
		tempMap2.put(KeyEvent.KEYCODE_4, VK_4);
		tempMap2.put(KeyEvent.KEYCODE_5, VK_5);
		tempMap2.put(KeyEvent.KEYCODE_6, VK_6);
		tempMap2.put(KeyEvent.KEYCODE_7, VK_7);
		tempMap2.put(KeyEvent.KEYCODE_8, VK_8);
		tempMap2.put(KeyEvent.KEYCODE_9, VK_9);
		tempMap2.put(KeyEvent.KEYCODE_DPAD_UP, VK_UP);
		tempMap2.put(KeyEvent.KEYCODE_DPAD_DOWN, VK_DOWN);
		tempMap2.put(KeyEvent.KEYCODE_DPAD_LEFT, VK_LEFT);
		tempMap2.put(KeyEvent.KEYCODE_DPAD_RIGHT, VK_RIGHT);
		tempMap2.put(KeyEvent.KEYCODE_VOLUME_UP, VK_VOLUME_UP);
		tempMap2.put(KeyEvent.KEYCODE_VOLUME_DOWN, VK_VOLUME_DOWN);
		tempMap2.put(KeyEvent.KEYCODE_CLEAR, VK_OEM_CLEAR);
		tempMap2.put(KeyEvent.KEYCODE_A, VK_A);
		tempMap2.put(KeyEvent.KEYCODE_B, VK_B);
		tempMap2.put(KeyEvent.KEYCODE_C, VK_C);
		tempMap2.put(KeyEvent.KEYCODE_D, VK_D);
		tempMap2.put(KeyEvent.KEYCODE_E, VK_E);
		tempMap2.put(KeyEvent.KEYCODE_F, VK_F);
		tempMap2.put(KeyEvent.KEYCODE_G, VK_G);
		tempMap2.put(KeyEvent.KEYCODE_H, VK_H);
		tempMap2.put(KeyEvent.KEYCODE_I, VK_I);
		tempMap2.put(KeyEvent.KEYCODE_J, VK_J);
		tempMap2.put(KeyEvent.KEYCODE_K, VK_K);
		tempMap2.put(KeyEvent.KEYCODE_L, VK_L);
		tempMap2.put(KeyEvent.KEYCODE_M, VK_M);
		tempMap2.put(KeyEvent.KEYCODE_N, VK_N);
		tempMap2.put(KeyEvent.KEYCODE_O, VK_O);
		tempMap2.put(KeyEvent.KEYCODE_P, VK_P);
		tempMap2.put(KeyEvent.KEYCODE_Q, VK_Q);
		tempMap2.put(KeyEvent.KEYCODE_R, VK_R);
		tempMap2.put(KeyEvent.KEYCODE_S, VK_S);
		tempMap2.put(KeyEvent.KEYCODE_T, VK_T);
		tempMap2.put(KeyEvent.KEYCODE_U, VK_U);
		tempMap2.put(KeyEvent.KEYCODE_V, VK_V);
		tempMap2.put(KeyEvent.KEYCODE_W, VK_W);
		tempMap2.put(KeyEvent.KEYCODE_X, VK_X);
		tempMap2.put(KeyEvent.KEYCODE_Y, VK_Y);
		tempMap2.put(KeyEvent.KEYCODE_Z, VK_Z);
		tempMap2.put(KeyEvent.KEYCODE_COMMA, VK_OEM_COMMA);
		tempMap2.put(KeyEvent.KEYCODE_PERIOD, VK_OEM_PERIOD);
		tempMap2.put(KeyEvent.KEYCODE_TAB, VK_TAB);
		tempMap2.put(KeyEvent.KEYCODE_SPACE, VK_SPACE);
		tempMap2.put(KeyEvent.KEYCODE_MINUS, VK_OEM_MINUS);
		tempMap2.put(KeyEvent.KEYCODE_PLUS, VK_OEM_PLUS);
		tempMap2.put(KeyEvent.KEYCODE_MENU, VK_MENU);
		tempMap2.put(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, VK_MEDIA_PLAY_PAUSE);

		// manuall
		tempMap2.put(KeyEvent.KEYCODE_ENTER, VK_RETURN);
		tempMap2.put(KeyEvent.KEYCODE_DEL, VK_BACK);
		tempMap2.remove(KeyEvent.KEYCODE_BACK);

		ANDROID_TO_WINDOWS_KEY_MAP = Collections.unmodifiableMap(tempMap2);
	}
}
