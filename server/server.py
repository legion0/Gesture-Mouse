import sys, os
import threading
import socket

import msgpack

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

	bl_args = {
		SETTINGS.BROADCAST_PORT: settings[SETTINGS.BROADCAST_PORT],
		SETTINGS.TCP_PORT: settings[SETTINGS.TCP_PORT],
		SETTINGS.UDP_PORT: settings[SETTINGS.UDP_PORT],
	}
	broadcast_listener_thread = threading.Thread(target=broadcast_listener, kwargs=bl_args)
	broadcast_listener_thread.setDaemon(True)
	broadcast_listener_thread.start()
	time.sleep(10)
	event_shutdown.set()
	broadcast_listener_thread.join()

def broadcast_listener(*args, **kwargs):
	broadcast_port = kwargs[SETTINGS.BROADCAST_PORT]
	tcp_port = kwargs[SETTINGS.TCP_PORT]
	udp_port = kwargs[SETTINGS.UDP_PORT]
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
