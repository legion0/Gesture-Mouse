import sys, os
import threading
import socket
import random
import ctypes, math
from ctypes import wintypes
from copy import deepcopy

import msgpack
import win32gui, win32process, win32api, win32con

from SysTrayIcon import SysTrayIcon

import settings as SETTINGS
import protocol

#temps
import time
from msgpacknsd import NSDServer
from math import sqrt

from win32api import GetSystemMetrics
import keyboard
import json

SCRIPT_DIR = os.path.dirname(__file__)
SCRIPT_NAME = os.path.basename(__file__)
SETTINGS_DIR = SCRIPT_DIR

MOUSE_LISTENER_BUFFER_SIZE = 1024
BROADCAST_LISTENER_BUFFER_SIZE = 1024
CONTROL_BUFFER_SIZE = 1024

SESSIONS = {}
SESSIONS_LOCK = threading.Lock()
SESSION_ID = 0

event_shutdown = threading.Event()

settings = None
broadcast_listener_thread = None

def main(args):
	global settings
# 	seq = [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.HOLD] + ([keyboard.KEYS.VK_RIGHT, 0, 0]*30) + [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.RELEASE]
# 	time.sleep(3)
# 	seq = [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.HOLD] + [keyboard.KEYS.VK_LBUTTON] + [keyboard.KEYS.VK_SHIFT|keyboard.MODEFIERS.RELEASE]
# 	keyboard.execute_sequence(seq)
# 	exit(0)
	# Load Settings
	settings_file_path = os.path.join(SETTINGS_DIR, "settings.yml")
	settings = SETTINGS.load_settings(settings_file_path)

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

	#Set Tray Icon
	tray_thread = threading.Thread(target=tray_handler)
	tray_thread.start()

	tray_thread.join()
	#time.sleep(60)
	#event_shutdown.set()

	client_server_thread.join()
	broadcast_listener_thread.join()
	window_monitor_thread.join()

def tray_handler():
	hover_text = "Gesture Mouse Server"
	menu_options = (
#		('Say Hello', None, kill_server),
#		('Switch Icon', None, kill_server),
#		('A sub-menu', None, (
#			('Say Hello to Simon', None, kill_server),
#			('Switch Icon', None, kill_server),
#		))
	)

	SysTrayIcon("icon.ico", hover_text, menu_options, on_quit=kill_server, default_menu_index=1)

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

def new_session():
	global SESSIONS
	client_disconnect_event = threading.Event()
	udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	udp_sock.settimeout(1)
	udp_sock.bind(("", 0))
	udp_port = udp_sock.getsockname()[1]
	session_id = gen_session_id()
	session = {"id": session_id, "udp_sock": udp_sock, "udp_port": udp_port, "disconnected": client_disconnect_event, "lock": threading.Lock()}
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
		print json.dumps(msg["apps"], sort_keys=True, indent=4, separators=(',', ': ')) # XXX
		session = new_session()
		mouse_listener_thread = threading.Thread(target=mouse_listener, args=(session,))
		with session["lock"]:
			session["name"] = msg["name"]
			session["control_port"] = msg["port"]
			session["apps"] = msg["apps"]
			session["ip"] = addr[0]
			session["mouse_listener_thread"] = mouse_listener_thread
			session_id = session["id"]
			udp_port = session["udp_port"]
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
	key_down = False
	if key_event == 0: # Volume Up pressed
		win32api.mouse_event(win32con.MOUSEEVENTF_LEFTDOWN,0,0,0,0)
		key_down = True
	elif key_event == 1: # Volume UP released
		win32api.mouse_event(win32con.MOUSEEVENTF_LEFTUP,0,0,0,0)
	elif key_event == 2: # Volume Down pressed
		win32api.mouse_event(win32con.MOUSEEVENTF_RIGHTDOWN,0,0,0,0)
		key_down = True
	elif key_event == 3: # Volume Down released
		win32api.mouse_event(win32con.MOUSEEVENTF_RIGHTUP,0,0,0,0)
	if key_down:
		with session["lock"]:
			delay_drag = session.get("settings", {}).get("mouse", {}).get("delay_drag", False)
			if delay_drag:
				now = get_timestamp()
				session["mouse_filter_low_end"] = now + 500 # delay mouse input for 100 miliseconds

def client_msg_handler(session, msg):
	if "key_event" in msg:
		handle_key_event(session, msg)
	elif "close" in msg:
		session_id = session["id"]
		with session["lock"]:
			session["disconnected"].set()
			session["mouse_listener_thread"].join()
		with SESSIONS_LOCK:
			del SESSIONS[session_id]
		print "bye, bye", session_id
	elif "gid" in msg:
		action = None
		gid = msg["gid"]
		print "client_msg_handler", "gid:", repr(gid)
		with session["lock"]:
			active_app = session["active_app"]
		if active_app is not None:
			for gesture in active_app["gestures"]:
				if gesture["gid"] == gid:
					action = gesture["action"]
					break
		print action
		if action is not None:
			keyboard.execute_sequence(action)

#PROCESS_CREATE_PROCESS = 0x0080
#PROCESS_CREATE_THREAD = 0x0002
#PROCESS_DUP_HANDLE = 0x0040
PROCESS_QUERY_INFORMATION = 0x0400
#PROCESS_QUERY_LIMITED_INFORMATION = 0x1000
#PROCESS_SET_INFORMATION = 0x0200
#PROCESS_SET_QUOTA = 0x0100
#PROCESS_SUSPEND_RESUME = 0x0800
#PROCESS_TERMINATE = 0x0001
#PROCESS_VM_OPERATION = 0x0008
PROCESS_VM_READ = 0x0010
#PROCESS_VM_WRITE = 0x0020
#SYNCHRONIZE = 0x00100000L
#PROCESS_ALL_ACCESS = PROCESS_CREATE_PROCESS | PROCESS_CREATE_THREAD | PROCESS_DUP_HANDLE | PROCESS_QUERY_INFORMATION | PROCESS_QUERY_LIMITED_INFORMATION | PROCESS_SET_INFORMATION | PROCESS_SET_QUOTA | PROCESS_SUSPEND_RESUME | PROCESS_TERMINATE | PROCESS_VM_OPERATION | PROCESS_VM_READ | PROCESS_VM_WRITE | SYNCHRONIZE

ERROR_ACCESS_DENIED = 5

def get_active_window_data():
	handle = win32gui.GetForegroundWindow()
	window_title = win32gui.GetWindowText(handle).lower()

	process_name = ""
	_, process_id = win32process.GetWindowThreadProcessId(handle)
	if process_id > 0:
		try:
			process_handle = win32api.OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, False, process_id)
			file_path = win32process.GetModuleFileNameEx(process_handle, None)
			process_name = os.path.basename(file_path).lower()
			process_handle.Close()
		except win32api.error as ex:
			if ex.winerror == ERROR_ACCESS_DENIED:
				pass
			else:
				raise ex
	return window_title, process_name

def application_finder(window_title, process_name):
	def the_finder(item):
		return window_title.startswith(item["window_title"]) or window_title.endswith(item["window_title"])
	return the_finder

def window_monitor():
	old_window_title = None
	old_process_name = None
	while not shutting_down():
		client_list = []
		window_title, process_name = get_active_window_data()
		if window_title != old_window_title or process_name != old_process_name:
			old_window_title = window_title
			old_process_name = process_name
			print "Switched to: %s (%s)." % (window_title, process_name)
		
			client_list = []
			with SESSIONS_LOCK:
				for session in SESSIONS.viewvalues():
					client_list.append({"addr":(session["ip"], session["control_port"]), "apps": deepcopy(session["apps"])})
	# 					app = extract_application(window_title, file_name, session["apps"])
	# 					if app != session.get("active_app"):
	# 						session["active_app"] = app
	# 						app_name = app["name"] if app is not None else None
	# 						print "window_monitor", "active_app:", app_name, "for session:", session["id"]
	# 						client_addr = (session["ip"], session["control_port"])
	# 						client_list.append((client_addr, app_name))
	
			for client in client_list:
				app = find_in_list(client["apps"], application_finder(window_title, process_name))
				appId = -1
				if app is not None:
					msg = {"app_id": app["id"]}
				else:
					msg = {"window_title": window_title, "process_name": process_name}
				print "Sending", msg, "to", client["addr"]
				try:
					sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
					sock.connect(client["addr"])
					sock.sendall(msgpack.packb(msg))
					sock.close()
				except socket.error as ex:
					continue # TODO: errors
		time.sleep(settings[SETTINGS.WINDOW_DETECTION_DELAY])

def find_in_list(l, selector):
	for item in l:
		if selector(item):
			return item
	return None

def extract_application(window_title, file_name, apps):
	if window_title == "" and file_name == "explorer.exe": # Desktop
		return None
	selected_app = None
	for app in apps: # First try by title
		if window_title.endswith(app["name"].lower()):
			selected_app = app
			break
	if selected_app is None: # Cannot find by title
		for app in apps: # First try by title
			if file_name == app["name"].lower():
				selected_app = app
				break
	return selected_app

def get_timestamp():
	return int(time.time() * 1000)
SCREEN_WIDTH = GetSystemMetrics(0)
SCREEN_HEIGHT = GetSystemMetrics(1)
print_round = 0
Y_AXIS_STRECH = 90
X_AXIS_STRECH = 60
HISTORY_SIZE = 25
SPEED_SENSITIVITY = 3
def mouse_listener(session):
	global print_round
	sock = session["udp_sock"]
	client_disconnect_event = session["disconnected"]
	while True:
		if client_disconnect_event.is_set():
			break
		try:
			data, addr = sock.recvfrom(MOUSE_LISTENER_BUFFER_SIZE)
		except socket.timeout:
			continue
		#print "mouse_listener", "got msg from:", addr
		try:
			msg = msgpack.unpackb(data)
		except ValueError:
			print >> sys.stderr, "mouse_listener", "cannot unpack:", repr(data)
			continue
		#filter_low_end = session.get("mouse_filter_low_end", now)

		current_x, current_y = win32api.GetCursorPos()

		#Calc New y:
		pitch = math.degrees(msg[2])
		pitch = 180-pitch
		if pitch > 180:
			pitch -= 360
		y = int( (-pitch/Y_AXIS_STRECH + 0.5) * SCREEN_HEIGHT)

		#Calc new x
		roll = math.degrees(msg[1])
		x = int( (-roll/X_AXIS_STRECH + 0.5) * SCREEN_WIDTH)



		#Add to history
		history = session.get("sample_history", [])
		history.append((float(x),float(y)))
		if len(history) > HISTORY_SIZE:
			history = history[-HISTORY_SIZE:]
		session["sample_history"] = history

#		#Calc avarage
#		def factor(i):
#			return 1
		avg = [0,0]
#		for i in xrange(len(history)):
#			avg[0] += factor(i) * history[i][0]
#			avg[1] += factor(i) * history[i][1]
#		avg = (avg[0]/len(history), avg[1]/len(history))
##		avg = (speed[0]/len(diffs), speed[1]/len(diffs))
#
#		#Calc speed
		speed = (0,0)
#		if len(history) > HISTORY_SIZE/2:
#			diffs = [0] * (len(history)-1)
#			for i in xrange(len(diffs)):
#				diffs[i] = (history[i+1][0] - history[i][0], history[i+1][1] - history[i][1])
#			speed = reduce(lambda x, y: (x[0]+y[0], x[1]+y[1]), diffs)
#			speed = (speed[0]/len(diffs), speed[1]/len(diffs))
#
#		#Check cursor speed:
#		if math.fabs(speed[0]) < SPEED_SENSITIVITY:
#			x = int(avg[0])
#		if math.fabs(speed[1]) < SPEED_SENSITIVITY:
#			y = int(avg[1])

		print_round = (print_round + 1) % 10
		if print_round == 0:
			print (pitch, roll), x, math.fabs(speed[0]), y, math.fabs(speed[1]), avg

		try:
			pass#win32api.SetCursorPos((x,y))
		except win32api.error:
			pass
#		time.sleep(0.1)
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
