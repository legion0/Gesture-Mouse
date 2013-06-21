import sys, os
import threading
import socket

import msgpack
import win32gui, win32process, win32api

import settings as SETTINGS
import protocol

#temps
import time

SCRIPT_DIR = os.path.dirname(__file__)
SCRIPT_NAME = os.path.basename(__file__)
SETTINGS_DIR = SCRIPT_DIR

event_shutdown = threading.Event()

settings = None
broadcast_listener_thread = None

def main(args):
	global settings
	settings_file_path = os.path.join(SETTINGS_DIR, "settings.yml")
	settings = SETTINGS.load_settings(settings_file_path)

	bl_args = (
		settings[SETTINGS.BROADCAST_PORT],
		settings[SETTINGS.TCP_PORT],
		settings[SETTINGS.UDP_PORT],
	)
	broadcast_listener_thread = threading.Thread(target=broadcast_listener, args=bl_args)
	broadcast_listener_thread.start()

	known_applications = (
		"Power Point",
		"Eclipse SDK",
		"Notepad++",
		"Windows Task Manager",
		"Google Chrome",
		"explorer.exe",
		"cygwin.exe"
	)

	monitor_args = (
		("127.0.0.1", 35202),
		[x.lower() for x in known_applications]
	)
	window_monitor_thread = threading.Thread(target=window_monitor, args=monitor_args)
	window_monitor_thread.start()

	time.sleep(30)

	event_shutdown.set()
	broadcast_listener_thread.join()
	window_monitor_thread.join()

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

def window_monitor(client_addr, known_applications):
	active_application = None
	while True:
		if event_shutdown.is_set():
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

		application_name = extract_application_name(window_title, file_name, known_applications)
		if application_name != active_application:
			active_application = application_name
			send_app_name_to_client(client_addr, active_application)
		time.sleep(settings[SETTINGS.WINDOW_DETECTION_DELAY])

def send_app_name_to_client(client_addr, active_application):
	print "active_application:", active_application
	msg = {"app": active_application}
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	try:
		sock.connect(client_addr)
		sock.sendall(msg)
	except socket.error as ex:
		print >> sys.stderr, ex
	finally:
		sock.close()

def extract_application_name(window_title, file_name, known_applications):
	if window_title == "" and file_name == "explorer.exe": # Desktop
		return None
	application_name = None
	for app_name in known_applications: # First try by title
		if window_title.endswith(app_name):
			application_name = app_name
			break
	if application_name is None: # Cannot find by title
		if file_name in known_applications:
			application_name = file_name
	return application_name

def broadcast_listener(broadcast_port, tcp_port, udp_port):
	response = {"service": "GM", "tcp": tcp_port, "udp": udp_port}

	sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, True)
	sock.settimeout(1)
	sock.bind(("", broadcast_port))
	while True:
		if event_shutdown.is_set():
			break
		try:
			data, addr = sock.recvfrom(1024)
		except socket.timeout:
			continue
		print "addr:", addr
		print "data:", data
		try:
			msg = msgpack.unpackb(data)
		except ValueError:
			continue
		print "message:", msg
		if protocol.BROADCAST.SERVICE in msg and msg[protocol.BROADCAST.SERVICE] == protocol.BROADCAST.GM:
			new_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
			new_sock.sendto(msgpack.packb(response), (addr[0], broadcast_port))

class Namespace(object):
	def __init__(self, adict):
		self.__dict__.update(adict)

if __name__ == '__main__':
	main(sys.argv[1:])
