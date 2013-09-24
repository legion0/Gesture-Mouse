import __main__
import os
import shutil
import yaml


SCRIPT_DIR = os.path.dirname(__main__.__file__)
SETTINGS_SAMPLE_FILE = os.path.join(SCRIPT_DIR, "settings-sample.yml")
HOME_DIR = os.getenv('USERPROFILE') or os.path.expanduser("~")  # prefer windows USERPROFILE for windows/cygwin mixes
CONFIG_DIR = os.path.join(HOME_DIR, ".config", __main__.__APP_NAME__)
SETTINGS_FILE = os.path.join(CONFIG_DIR, "settings.yml")


def load_settings():
	if not os.path.exists(SETTINGS_FILE):
		if not os.path.exists(os.path.dirname(SETTINGS_FILE)):
			os.makedirs(os.path.dirname(SETTINGS_FILE))
		shutil.copy(SETTINGS_SAMPLE_FILE, SETTINGS_FILE)
	with open(SETTINGS_FILE) as f:
		return yaml.load(f)

TCP_PORT = "tcp_port"
UDP_PORT = "udp_port"
BROADCAST_PORT = "broadcast_port"
WINDOW_DETECTION_DELAY = "window_detection_delay"
