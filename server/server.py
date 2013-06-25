import sys, os
import threading
import socket
import random
import ctypes
from ctypes import wintypes

import msgpack
import win32gui, win32process, win32api

from SysTrayIcon import SysTrayIcon

import settings as SETTINGS
import protocol

#temps
import time

SCRIPT_DIR = os.path.dirname(__file__)
SCRIPT_NAME = os.path.basename(__file__)
SETTINGS_DIR = SCRIPT_DIR

MOUSE_LISTENER_BUFFER_SIZE = 1024
BROADCAST_LISTENER_BUFFER_SIZE = 1024
CONTROL_BUFFER_SIZE = 1024

CLIENTS = {}
CLIENTS_LOCK = threading.Lock()
SESSION_ID = 0

event_shutdown = threading.Event()

settings = None
broadcast_listener_thread = None

def main(args):
	global settings
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

	client_server_thread = threading.Thread(target=client_server)
	client_server_thread.start()

	#Set Tray Icon
	tray_thread = threading.Thread(target=tray_handler)
	tray_thread.start()

	tray_thread.join()
	client_server_thread.join()
	broadcast_listener_thread.join()

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
	serve_sock_function(sock, client_handler)
	sock.close()

def serve_sock_function(sock, func):
	print "Serving %s on %s:%s" % (func, sock.getsockname()[0], sock.getsockname()[1])
	threads = []
	while True:
		if shutting_down():
			break
		try:
			client = sock.accept()
		except socket.timeout:
			continue
		thread = threading.Thread(target=func, args=client)
		threads.append(thread)
		thread.start()
	for thread in threads:
		thread.join()

def read_msg(sock):
	data = sock.recv(CONTROL_BUFFER_SIZE)
	try:
		return msgpack.unpackb(data)
	except ValueError:
		print >> sys.stderr, "read_msg", "cannot unpack:", repr(data)
		return None

def client_handler(sock, addr):
	global SESSION_ID
	client_disconnect_event = threading.Event()
	udp_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	udp_sock.settimeout(1)
	udp_sock.bind(("", 0))
	udp_port = sock.getsockname()[1]
	sock.sendall(msgpack.packb({"udp": udp_port}))

	session = {"sock": sock, "udp_sock": udp_sock, "ip": addr[0], "disconnected": client_disconnect_event, "lock": threading.Lock()}
	with CLIENTS_LOCK:
		session_id = SESSION_ID
		SESSION_ID += 1
	session["id"] = session_id
	while True:
		if shutting_down():
			client_disconnect_event.set()
			break
		if client_disconnect_event.is_set():
			break
		msg = read_msg(sock)
		print "client_handler", "got msg from:", addr
		print msg
		if msg is None:
			continue
		client_msg_handler(session, msg)
	sock.close()
	udp_sock.close()

def client_msg_handler(session, msg):
	if "name" in msg:
		session["name"] = client_name = msg["name"]
		session["control_port"] = msg["port"]
		session["apps"] = msg["apps"]

		print "client_msg_handler", "new connection from:", client_name
		window_monitor_thread = threading.Thread(target=window_monitor, args=(session,))
		window_monitor_thread.start()
		mouse_listener_thread = threading.Thread(target=mouse_listener, args=(session,))
		mouse_listener_thread.start()
	elif "close" in msg:
		session["disconnected"].set()
		window_monitor_thread.join()
		mouse_listener_thread.join()
		session["sock"].close()
	elif "gid" in msg:
		action = None
		gid = msg["gid"]
		with session["lock"]:
			active_app = session["active_app"]
		if active_app is not None:
			for gesture in active_app["gestures"]:
				if gesture["gid"] == gid:
					action = gesture["action"]
					break
		print action

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

def window_monitor(session):
	client_disconnect_event = session["disconnected"]
	client_addr = (session["ip"], session["control_port"])
	active_app = None
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	apps = session["apps"]
	sock.connect(client_addr)
	while True:
		if client_disconnect_event.is_set():
			break
		handle = win32gui.GetForegroundWindow()
		window_title = win32gui.GetWindowText(handle).lower()

		file_name = None
		_, process_id = win32process.GetWindowThreadProcessId(handle)
		if process_id > 0:
			process_handle = win32api.OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ, False, process_id)
			file_path = win32process.GetModuleFileNameEx(process_handle, None)
			file_name = os.path.basename(file_path).lower()
			process_handle.Close()

		app = extract_application(window_title, file_name, apps)
		if app != active_app:
			active_app = app
			with session["lock"]:
				session["active_app"] = app
			app_name = app["name"] if "name" in app else None
			print "window_monitor", "active_app:", app_name
			sock.sendall({"app": app_name})

		time.sleep(settings[SETTINGS.WINDOW_DETECTION_DELAY])
	sock.close()

def extract_application(window_title, file_name, apps):
	if window_title == "" and file_name == "explorer.exe": # Desktop
		return None
	selected_app = None
	for app in apps: # First try by title
		if window_title.endswith(app["name"]):
			selected_app = app
			break
	if selected_app is None: # Cannot find by title
		for app in apps: # First try by title
			if file_name == app["name"]:
				selected_app = app
				break
	return selected_app

def mouse_listener(session):
	sock = session["udp_sock"]
	client_disconnect_event = session["disconnected"]
	while True:
		if client_disconnect_event.is_set():
			break
		try:
			data, addr = sock.recvfrom(MOUSE_LISTENER_BUFFER_SIZE)
		except socket.timeout:
			continue
		print "mouse_listener", "got msg from:", addr
		try:
			msg = msgpack.unpackb(data)
		except ValueError:
			print >> sys.stderr, "mouse_listener", "cannot unpack:", repr(data)
			continue
		print "mouse_listener", "msg:", msg
		x, y = win32api.GetCursorPos()
		x += random.randint(-40, 40)
		y += random.randint(-40, 40)
		win32api.SetCursorPos((x,y))
#		time.sleep(0.1)
	sock.close()

def broadcast_listener(broadcast_port, tcp_port):
	response = {"service": "GM", "tcp": tcp_port}

	sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, True)
#	sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
#	sock.settimeout(5)
	sock.bind(("", broadcast_port))
	print "Serving discovery on %s:%s." % sock.getsockname()
	while True:
		if shutting_down():
			break
		try:
			data, addr = sock.recvfrom(BROADCAST_LISTENER_BUFFER_SIZE)
		except socket.timeout:
			print "timeout"
			continue
		print "broadcast_listener", "discovery request from:", addr
		try:
			msg = msgpack.unpackb(data)
		except ValueError:
			continue
		print msg
		if protocol.BROADCAST.SERVICE in msg and msg[protocol.BROADCAST.SERVICE] == protocol.BROADCAST.GM:
			new_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
			addr = (addr[0], broadcast_port)
			print "broadcast_listener", "sending response to:", addr
			new_sock.sendto(msgpack.packb(response), addr)
	sock.close()

if __name__ == '__main__':
	main(sys.argv[1:])
