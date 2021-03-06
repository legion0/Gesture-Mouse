__APP_NAME__ = "gesture_mouse_server"
__VERSION__ = "1.0.0"
from SysTrayIcon import SysTrayIcon
# from copy import deepcopy
# from ctypes import wintypes
# from math import sqrt
from msgpacknsd import NSDServer
from win32api import GetSystemMetrics
# import ctypes
import math
# import json
import keyboard
import msgpack
# import protocol
# import random
import settings as SETTINGS
import socket
import sys
import os
import threading
import time
import win32api

SCRIPT_DIR = os.path.dirname(__file__)
SCRIPT_NAME = os.path.basename(__file__)
HOME_DIR = os.getenv('USERPROFILE') or os.path.expanduser("~")  # prefer windows USERPROFILE for windows/cygwin mixes
LOG_DIR = os.path.join(HOME_DIR, ".logs", __APP_NAME__)
CACHE_DIR = os.path.join(HOME_DIR, ".cache", __APP_NAME__)
DATA_DIR = os.path.join(HOME_DIR, ".data", __APP_NAME__)
CONFIG_DIR = os.path.join(HOME_DIR, ".config", __APP_NAME__)
SETTINGS_DIR = CONFIG_DIR

MOUSE_LISTENER_BUFFER_SIZE = 1024
BROADCAST_LISTENER_BUFFER_SIZE = 1024
CONTROL_BUFFER_SIZE = 1024

SESSIONS = {}
SESSIONS_LOCK = threading.Lock()
SESSION_ID = 0

event_shutdown = threading.Event()

settings = None
broadcast_listener_thread = None

import windows

def main(args):
	global settings
# 	seq = [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.HOLD] + ([keyboard.KEYS.VK_RIGHT, 0, 0]*30) + [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.RELEASE]
# 	time.sleep(3)
# 	seq = [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.HOLD] + [keyboard.KEYS.VK_LBUTTON] + [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.RELEASE]
# 	keyboard.execute_sequence(seq)
# 	exit(0)
	# Load Settings
	settings = SETTINGS.load_settings()

	# Start threads
	bl_args = (
		settings[SETTINGS.BROADCAST_PORT],
		settings[SETTINGS.TCP_PORT],
	)
	broadcast_listener_thread = threading.Thread(target=broadcast_listener, args=bl_args)
	broadcast_listener_thread.start()

	window_monitor_thread = threading.Thread(target=window_monitor)
	window_monitor_thread.start()

	client_server_thread = threading.Thread(target=client_server)
	client_server_thread.start()

	# Set Tray Icon
	tray_thread = threading.Thread(target=tray_handler)
	tray_thread.start()

	tray_thread.join()
	# time.sleep(60)
	event_shutdown.set()

	client_server_thread.join()
	broadcast_listener_thread.join()
	window_monitor_thread.join()

def tray_handler():
	global TRAY_HNWD
	hover_text = "Gesture Mouse Server"
	menu_options = (
# 		('Say Hello', None, kill_server),
# 		('Switch Icon', None, kill_server),
# 		('A sub-menu', None, (
# 			('Say Hello to Simon', None, kill_server),
# 			('Switch Icon', None, kill_server),
# 		))
	)
	TRAY_HNWD = SysTrayIcon("icon.ico", hover_text, menu_options, on_quit=kill_server, default_menu_index=1)
	TRAY_HNWD.show_balloon_tip("Startup", "Gesture mouse is running.")
	TRAY_HNWD.start()

def kill_server(_):
	event_shutdown.set()

def shutting_down():
	return event_shutdown.is_set()

def client_server():
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, True)
	sock.settimeout(1)
	sock.bind(("", settings[SETTINGS.TCP_PORT]))
	sock.listen(3)
	serve_sock_function(sock, request_handler)
	sock.close()

def serve_sock_function(sock, func):
	print "Serving %s on %s:%s" % (func, sock.getsockname()[0], sock.getsockname()[1])
	threads = []
	while True:
		if shutting_down():
			break
		try:
			conn, addr = sock.accept()
			conn.setblocking(True)
		except socket.timeout:
			continue
		print "serve_sock_function", "Serving %s to %s" % (func, repr(addr))
		thread = threading.Thread(target=func, args=(conn, addr))
		threads.append(thread)
		thread.start()
	for thread in threads:
		thread.join()

def read_msg(sock):
	try:
		data = sock.recv(CONTROL_BUFFER_SIZE)
		return msgpack.unpackb(data)
	except socket.timeout:
		print >> sys.stderr, "read_msg", "timeout"
	except ValueError:
		print >> sys.stderr, "read_msg", "cannot unpack:", repr(data)
	except socket.error as ex:
		print >> sys.stderr, "read_msg", type(ex), repr(ex)
	return None


def gen_session_id():
	with gen_session_id.lock:
		gen_session_id.cid += 1
		new_session_id = gen_session_id.cid
	return str(new_session_id)
gen_session_id.cid = -1
gen_session_id.lock = threading.Lock()

def new_session(msg, addr):
	global SESSIONS
	client_disconnect_event = threading.Event()
	udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	udp_sock.settimeout(1)
	udp_sock.bind(("", 0))
	udp_port = udp_sock.getsockname()[1]
	session_id = gen_session_id()
	session = {"id": session_id, "udp_sock": udp_sock, "udp_port": udp_port, "disconnected": client_disconnect_event, "lock": threading.Lock()}
	session["name"] = msg["name"]
	session["control_port"] = msg["port"]
	session["apps"] = msg["apps"]
	session["ip"] = addr[0]
	mouse_listener_thread = threading.Thread(target=mouse_listener, args=(session,))
	session["mouse_listener_thread"] = mouse_listener_thread
	session["settings"] = {
		"mouse": {
			"delay_drag": True
		}
	}
	with SESSIONS_LOCK:
		SESSIONS[session["id"]] = session
	return session

def request_handler(sock, addr):
	print "request_handler", "connection from:", addr
	msg = read_msg(sock)
	print "request_handler", "msg:", repr(msg)
	if msg is None:
		return
	if "name" in msg:
		session = new_session(msg, addr)
		with session["lock"]:
			udp_port = session["udp_port"]
			session_id = session["id"]
			mouse_listener_thread = session["mouse_listener_thread"]
		response = {"session_id": session_id, "udp": udp_port}
		print "request_handler", "response:", repr(response)
		sock.sendall(msgpack.packb(response))
		mouse_listener_thread.start()
	elif "session_id" in msg:
		with SESSIONS_LOCK:
			session = SESSIONS.get(msg["session_id"])
		if session is None:
			return
		client_msg_handler(session, msg)

def handle_key_event(session, msg):
	key_event = msg["key_event"]
	if type(key_event) is int and keyboard.is_mouse_event(key_event):
		handle_mouse_drag(session, key_event)
	if type(key_event) is int:
		key_event = [key_event]
	keyboard.execute_sequence(key_event)

def handle_keyboard_key(session, key_event):
	keyboard.execute_sequence([key_event])

def handle_mouse_drag(session, key_event):
	if keyboard.is_key_hold(key_event):
		with session["lock"]:
			delay_drag = session.get("settings", {}).get("mouse", {}).get("delay_drag", False)
			if delay_drag:
				now = get_timestamp()
				session["mouse_filter_low_end"] = now + 500  # delay mouse input for 100 miliseconds

def find_session(session_id):
	with SESSIONS_LOCK:
		return SESSIONS.get(session_id, None)

def delete_session(session_id):
	with SESSIONS_LOCK:
		session = SESSIONS.pop(session_id, None)
	if session is None:
		return
	with session["lock"]:
		session["disconnected"].set()
		mouse_listener_thread = session["mouse_listener_thread"]
	mouse_listener_thread.join()
	print "bye, bye", session_id
	with SESSIONS_LOCK:
		if len(SESSIONS) > 0:
			still_alive_ids = [session["id"] for session in SESSIONS.viewvalues()]
			print "Still alive: %r." % still_alive_ids

def client_msg_handler(session, msg):
	if "key_event" in msg:
		handle_key_event(session, msg)
	elif "close" in msg:
		session_id = msg["session_id"]
		delete_session(session_id)
	elif "gesture" in msg:
		action = None
		gesture_id = msg["gesture"]
		print "client_msg_handler", "gesture_id:", repr(gesture_id)
		with session["lock"]:
			window_title, process_name = get_active_window_data()
			active_app = find_in_list(session["apps"], application_finder(window_title, process_name))
		if active_app is not None:
			for gesture in active_app["gestures"]:
				if gesture["id"] == gesture_id:
					action = gesture["action"]
					break
		print action
		if action is not None:
			keyboard.execute_sequence(action)

# PROCESS_CREATE_PROCESS = 0x0080
# PROCESS_CREATE_THREAD = 0x0002
# PROCESS_DUP_HANDLE = 0x0040
PROCESS_QUERY_INFORMATION = 0x0400
# PROCESS_QUERY_LIMITED_INFORMATION = 0x1000
# PROCESS_SET_INFORMATION = 0x0200
# PROCESS_SET_QUOTA = 0x0100
# PROCESS_SUSPEND_RESUME = 0x0800
# PROCESS_TERMINATE = 0x0001
# PROCESS_VM_OPERATION = 0x0008
PROCESS_VM_READ = 0x0010
# PROCESS_VM_WRITE = 0x0020
# SYNCHRONIZE = 0x00100000L
# PROCESS_ALL_ACCESS = PROCESS_CREATE_PROCESS | PROCESS_CREATE_THREAD | PROCESS_DUP_HANDLE | PROCESS_QUERY_INFORMATION | PROCESS_QUERY_LIMITED_INFORMATION | PROCESS_SET_INFORMATION | PROCESS_SET_QUOTA | PROCESS_SUSPEND_RESUME | PROCESS_TERMINATE | PROCESS_VM_OPERATION | PROCESS_VM_READ | PROCESS_VM_WRITE | SYNCHRONIZE

ERROR_ACCESS_DENIED = 5
ERROR_PARTIAL_COPY = 299

def get_active_window_data():
	return (windows.get_foreground_title() or "").lower(), (windows.get_foreground_process_name() or "").lower()

def application_finder(window_title, process_name):
	def the_finder(item):
		windows = (window_title == "" and item["window_title"] == "")
		other = item["window_title"] != "" and (window_title.startswith(item["window_title"]) or window_title.endswith(item["window_title"]))
		return windows or other
	return the_finder

def delete_stail_sessions():
	global SESSIONS, SESSIONS_LOCK
	with SESSIONS_LOCK:
		for session_id in SESSIONS.keys():
			if SESSIONS[session_id].get("control_port_fails", 0) > 2:
				del SESSIONS[session_id]
				print "Deleted session %r" % session_id

def window_monitor():
	old_window_title = None
	old_process_name = None
	while not shutting_down():
		window_title, process_name = get_active_window_data()
		if window_title != old_window_title or process_name != old_process_name:
			old_window_title = window_title
			old_process_name = process_name
			print "Switched to: %r (%r)." % (window_title, process_name)

		delete_stail_sessions()
		with SESSIONS_LOCK:
			for session in SESSIONS.viewvalues():
				with session["lock"]:
					session_id = session["id"]
					app = find_in_list(session["apps"], application_finder(window_title, process_name))
					msg = None
					if app is not None and app != session.get("active_app"):
						session["active_app"] = app
						session["current_window_title"] = None
						session["current_process_name"] = None
						msg = {"app_id": app["id"]}
					elif app is None and (window_title != session.get("current_window_title") or process_name != session.get("current_process_name")):
						session["active_app"] = None
						session["current_window_title"] = window_title
						session["current_process_name"] = process_name
						msg = {"window_title": window_title, "process_name": process_name}
# 						print window_title.encode('utf-8')
# 						print msgpack.packb(window_title.encode('utf-8'))
# 						print map(ord,window_title.encode('utf-8'))
# 						print map(ord,window_title)
					if msg is not None:
						addr = (session["ip"], session["control_port"])
						print "Sending", repr(msg), "to", addr
						try:
							sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
							sock.connect(addr)
							sock.sendall(msgpack.packb(msg))
							sock.close()
							session["control_port_fails"] = 0
						# except socket.error as ex:
						except Exception as ex:
							if type(ex) == socket.error:
								session["control_port_fails"] = session.get("control_port_fails", 0) + 1
								print "session %s is not responding on address %s for the %s time." % (session_id, addr, session["control_port_fails"])
								continue
							raise ex
		time.sleep(settings[SETTINGS.WINDOW_DETECTION_DELAY])

def find_in_list(l, selector):
	for item in l:
		if selector(item):
			return item
	return None

# def extract_application(window_title, file_name, apps):
# 	if window_title == "" and file_name == "explorer.exe": # Desktop
# 		return None
# 	selected_app = None
# 	for app in apps: # First try by title
# 		if window_title.endswith(app["name"].lower()):
# 			selected_app = app
# 			break
# 	if selected_app is None: # Cannot find by title
# 		for app in apps: # First try by title
# 			if file_name == app["name"].lower():
# 				selected_app = app
# 				break
# 	return selected_app

def get_timestamp():
	return int(time.time() * 1000)
SCREEN_WIDTH = GetSystemMetrics(0)
SCREEN_HEIGHT = GetSystemMetrics(1)
print_round = 0
Y_AXIS_STRECH = 90
X_AXIS_STRECH = 90
HISTORY_SIZE = 25
SPEED_SENSITIVITY = 3
def mouse_listener(session):
	global print_round
	sock = session["udp_sock"]
	client_disconnect_event = session["disconnected"]
	x_last = 0.0
	y_last = 0.0
	while True:
		if client_disconnect_event.is_set():
			break
		try:
			data, _ = sock.recvfrom(MOUSE_LISTENER_BUFFER_SIZE)
		except socket.timeout:
			continue

		try:
			msg = msgpack.unpackb(data)
		except ValueError:
			print >> sys.stderr, "mouse_listener", "cannot unpack:", repr(data)
			continue

		current_x, current_y = win32api.GetCursorPos()

		pitch = -math.degrees(msg[1])
# 		pitch = math.degrees(msg[2])
# 		pitch = 180 - pitch
# 		if pitch > 180:
# 			pitch -= 360


		roll = math.degrees(msg[2])
# 		roll = -math.degrees(msg[1])

		# Calc new x,y:
		shift_threshold = 1.5
		smooth_factor = 4.0
		power_factor = 1.3
		diff_bound = 2
		if abs(roll) > shift_threshold:
			sign = roll / abs(roll)
			factor = ((roll - (shift_threshold * sign)) / smooth_factor)
			factor_sign = factor / abs(factor)
			factor = (abs(factor) ** power_factor) * factor_sign
			factor = max(min(factor, 40), -40)
			if abs(x_last - current_x) > diff_bound:
				x_last = current_x
			x = x_last + factor

# 			x_diff_sum = x_diff_sum + (x_last - x)*smooth_factor
#  			if abs(x_diff_sum) >= diff_bound:
#  				x_diff_sum = 0.0
#  			if x_diff_sum > 1 or x_diff_sum < -1:
#  				x = x - x_diff_sum
#  				x_diff_sum = 0.0
		else:
			x = current_x

		if abs(pitch) > shift_threshold:
			sign = -pitch / abs(pitch)
			factor = (-pitch - (shift_threshold * sign)) / smooth_factor
			factor_sign = factor / abs(factor)
			factor = (abs(factor) ** power_factor) * factor_sign
			factor = max(min(factor, 40), -40)
			if abs(y_last - current_y) > diff_bound:
				y_last = current_y
			y = y_last + factor

# 			y_diff_sum = y_diff_sum + (y_last - y)*smooth_factor
#  			if abs(y_diff_sum) >= diff_bound:
#  				y_diff_sum = 0.0
#  			if y_diff_sum > 1 or y_diff_sum < -1:
#  				y = y - y_diff_sum
#  				y_diff_sum = 0.0
		else:
			y = current_y

		print_round = (print_round + 1) % 100
# 		if print_round == 0:
# 			print int(x-current_x), int(y-current_y), roll, pitch
# 			print "x: ",x, "y: ",y
# 			print "x diff: ",x - x_last,"y diff", y - y_last, "x_diff_sum: ",x_diff_sum," y_diff_sum: ",y_diff_sum
# 		x_diff = int(x-current_x)
# 		y_diff = int(y-current_y)
# 		if (x_diff != 0) or (y_diff != 0):
# 			print x_diff, y_diff, roll, pitch
		x_last = x
		y_last = y
		round_x = int(x)
		round_y = int(y)
		if round_x != current_x or round_y != current_y:
			try:
				win32api.SetCursorPos((round_x, round_y))
			except win32api.error:
				pass
	sock.close()

def broadcast_listener(broadcast_port, tcp_port):
	machine_name = socket.gethostbyaddr(socket.gethostname())[0]
	server = NSDServer()
	server.add_service("GM", tcp_port, extra_info={"machine_name": machine_name})
	server.start()
	while not shutting_down():
		time.sleep(1)
	server.stop()

if __name__ == '__main__':
	main(sys.argv[1:])
