import yaml

def load_settings(file_path):
	with open(file_path) as f:
		return yaml.load(f)

TCP_PORT = "tcp_port"
UDP_PORT = "udp_port"
BROADCAST_PORT = "broadcast_port"
