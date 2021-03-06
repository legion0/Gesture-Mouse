import time
from random import randint

import win32api
import win32con

SLEEP_INTERVAL = (50, 10)
def sleep_interval():
	global SLEEP_INTERVAL
	duration = randint(SLEEP_INTERVAL[0]-SLEEP_INTERVAL[1], SLEEP_INTERVAL[0]+SLEEP_INTERVAL[1])
	return float(duration)/1000

def key_press(key, duration = None):
	key_down(key)
	if duration is not None:
		time.sleep(duration)
	key_up(key)

def key_down(key_event):
	if is_mouse_event(key_event):
		__handle_mouse_key(key_event)
	else:
		win32api.keybd_event(key_event, win32api.MapVirtualKey(key_event, 0))

def __handle_mouse_key(key_event):
	mouse_key = KEYBOARD_MOUSE_MAP.get(key_event)
	if mouse_key is None:
		return
	if type(mouse_key) is int:
		mouse_key = [mouse_key]
	for key_part in mouse_key:
		win32api.mouse_event(key_part,0,0,0,0)

def key_up(key_event):
	if is_mouse_event(key_event):
		__handle_mouse_key(key_event)
	else:
		win32api.keybd_event(key_event, win32api.MapVirtualKey(key_event, 0), win32con.KEYEVENTF_KEYUP)

def is_mouse_event(key_event):
	pure_key = get_pure_key(key_event)
	return pure_key in MOUSE_KEYS

class KEYS:
	VK_LBUTTON = 1
	VK_RBUTTON = 2
	VK_CANCEL = 3
	VK_MBUTTON = 4
	VK_BACK = 8
	VK_TAB = 9
	VK_CLEAR = 12
	VK_RETURN = 13
	VK_SHIFT = 16
	VK_CONTROL = 17
	VK_MENU = 18
	VK_PAUSE = 19
	VK_CAPITAL = 20
	VK_KANA = 21
	VK_HANGEUL = 21
	VK_HANGUL = 21
	VK_JUNJA = 23
	VK_FINAL = 24
	VK_HANJA = 25
	VK_KANJI = 25
	VK_ESCAPE = 27
	VK_CONVERT = 28
	VK_NONCONVERT = 29
	VK_ACCEPT = 30
	VK_MODECHANGE = 31
	VK_SPACE = 32
	VK_PRIOR = 33
	VK_NEXT = 34
	VK_END = 35
	VK_HOME = 36
	VK_LEFT = 37
	VK_UP = 38
	VK_RIGHT = 39
	VK_DOWN = 40
	VK_SELECT = 41
	VK_PRINT = 42
	VK_EXECUTE = 43
	VK_SNAPSHOT = 44
	VK_INSERT = 45
	VK_DELETE = 46
	VK_HELP = 47
	VK_0 = 48
	VK_1 = 49
	VK_2 = 50
	VK_3 = 51
	VK_4 = 52
	VK_5 = 53
	VK_6 = 54
	VK_7 = 55
	VK_8 = 56
	VK_9 = 57
	VK_A = 65
	VK_B = 66
	VK_C = 67
	VK_D = 68
	VK_E = 69
	VK_F = 70
	VK_G = 71
	VK_H = 72
	VK_I = 73
	VK_J = 74
	VK_K = 75
	VK_L = 76
	VK_M = 77
	VK_N = 78
	VK_O = 79
	VK_P = 80
	VK_Q = 81
	VK_R = 82
	VK_S = 83
	VK_T = 84
	VK_U = 85
	VK_V = 86
	VK_W = 87
	VK_X = 88
	VK_Y = 89
	VK_Z = 90
	VK_LWIN = 91
	VK_RWIN = 92
	VK_APPS = 93
	VK_NUMPAD0 = 96
	VK_NUMPAD1 = 97
	VK_NUMPAD2 = 98
	VK_NUMPAD3 = 99
	VK_NUMPAD4 = 100
	VK_NUMPAD5 = 101
	VK_NUMPAD6 = 102
	VK_NUMPAD7 = 103
	VK_NUMPAD8 = 104
	VK_NUMPAD9 = 105
	VK_MULTIPLY = 106
	VK_ADD = 107
	VK_SEPARATOR = 108
	VK_SUBTRACT = 109
	VK_DECIMAL = 110
	VK_DIVIDE = 111
	VK_F1 = 112
	VK_F2 = 113
	VK_F3 = 114
	VK_F4 = 115
	VK_F5 = 116
	VK_F6 = 117
	VK_F7 = 118
	VK_F8 = 119
	VK_F9 = 120
	VK_F10 = 121
	VK_F11 = 122
	VK_F12 = 123
	VK_F13 = 124
	VK_F14 = 125
	VK_F15 = 126
	VK_F16 = 127
	VK_F17 = 128
	VK_F18 = 129
	VK_F19 = 130
	VK_F20 = 131
	VK_F21 = 132
	VK_F22 = 133
	VK_F23 = 134
	VK_F24 = 135
	VK_NUMLOCK = 144
	VK_SCROLL = 145
	VK_LSHIFT = 160
	VK_RSHIFT = 161
	VK_LCONTROL = 162
	VK_RCONTROL = 163
	VK_LMENU = 164
	VK_RMENU = 165
	VK_PROCESSKEY = 229
	VK_ATTN = 246
	VK_CRSEL = 247
	VK_EXSEL = 248
	VK_EREOF = 249
	VK_PLAY = 250
	VK_ZOOM = 251
	VK_NONAME = 252
	VK_PA1 = 253
	VK_OEM_CLEAR = 254
	VK_MOUSEEVENTF_XDOWN = 0x0080
	VK_MOUSEEVENTF_XUP = 0x0100
	VK_MOUSEEVENTF_WHEEL = 0x0800
	VK_XBUTTON1 = 0x05
	VK_XBUTTON2 = 0x06
	VK_VOLUME_MUTE = 0xAD
	VK_VOLUME_DOWN = 0xAE
	VK_VOLUME_UP = 0xAF
	VK_MEDIA_NEXT_TRACK = 0xB0
	VK_MEDIA_PREV_TRACK = 0xB1
	VK_MEDIA_PLAY_PAUSE = 0xB3
	VK_BROWSER_BACK = 0xA6
	VK_BROWSER_FORWARD = 0xA7

	VK_OEM_1 = 0xBA
	VK_OEM_PLUS = 0xBB
	VK_OEM_COMMA = 0xBC
	VK_OEM_MINUS = 0xBD
	VK_OEM_PERIOD = 0xBE
	VK_OEM_2 = 0xBF
	VK_OEM_3 = 0xC0
	VK_OEM_4 = 0xDB
	VK_OEM_5 = 0xDC
	VK_OEM_6 = 0xDD
	VK_OEM_7 = 0xDE
	VK_OEM_8 = 0xDF

MOUSE_KEYS = (KEYS.VK_LBUTTON, KEYS.VK_RBUTTON, KEYS.VK_XBUTTON1, KEYS.VK_XBUTTON2)

class MODEFIERS:
	HOLD = 1 << 16
	RELEASE = 2 << 16

def get_pure_key(key_event):
	if key_event & MODEFIERS.RELEASE:
		return key_event - MODEFIERS.RELEASE
	if key_event & MODEFIERS.HOLD:
		return key_event - MODEFIERS.HOLD
	return key_event

def is_key_hold(key_event):
	return key_event & MODEFIERS.HOLD

def is_key_release(key_event):
	return key_event & MODEFIERS.RELEASE

def set_key_hold(key):
	return key | MODEFIERS.HOLD

def set_key_release(key):
	return key | MODEFIERS.RELEASE

KEYBOARD_MOUSE_MAP = {
	set_key_hold(KEYS.VK_LBUTTON) : win32con.MOUSEEVENTF_LEFTDOWN,
	set_key_release(KEYS.VK_LBUTTON) : win32con.MOUSEEVENTF_LEFTUP,
	KEYS.VK_LBUTTON : [win32con.MOUSEEVENTF_LEFTDOWN, win32con.MOUSEEVENTF_LEFTUP],
	set_key_hold(KEYS.VK_RBUTTON) : win32con.MOUSEEVENTF_RIGHTDOWN,
	set_key_release(KEYS.VK_RBUTTON) : win32con.MOUSEEVENTF_RIGHTUP,
	KEYS.VK_RBUTTON : [win32con.MOUSEEVENTF_RIGHTDOWN, win32con.MOUSEEVENTF_RIGHTUP],
}

def execute_sequence(seq):
	for key in seq:
		if is_mouse_event(key):
			__handle_mouse_key(key)
		elif key == 0:
			time.sleep(sleep_interval())
		elif is_key_hold(key):
			key_down(key)
		elif is_key_release(key):
			key_up(key)
		else:
			key_press(key)
