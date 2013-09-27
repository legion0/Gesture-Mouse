import ctypes, os
import win32con
import wintypes

__user32dll = ctypes.windll.LoadLibrary("User32.dll")
__advapi32dll = ctypes.windll.LoadLibrary("Advapi32.dll")
__kernel32dll = ctypes.windll.kernel32
__psapi_dll = ctypes.windll.LoadLibrary("Psapi.dll")

def get_foreground_title():
	hwnd = __user32dll.GetForegroundWindow()
	window_text_length = __user32dll.GetWindowTextLengthW(wintypes.HANDLE(hwnd))
	lpString = ctypes.create_unicode_buffer(window_text_length+1)
	__user32dll.GetWindowTextW(hwnd, lpString, window_text_length+1)
	return lpString.value

def get_foreground_process_name():
	hwnd = __user32dll.GetForegroundWindow()
	processid = wintypes.DWORD()
	__user32dll.GetWindowThreadProcessId(hwnd, wintypes.LPDWORD(processid))
	pshandle = __kernel32dll.OpenProcess(win32con.PROCESS_QUERY_INFORMATION | win32con.PROCESS_VM_READ, False, processid)
	chars_allocated = 128
	chars_returned = 128
	while chars_allocated == chars_returned:
		chars_allocated *= 2
		lpString = ctypes.create_unicode_buffer(chars_allocated+1)
		chars_returned = __psapi_dll.GetModuleFileNameExW(pshandle, 0, lpString, chars_allocated)
		if not chars_returned:
			return None
	__kernel32dll.CloseHandle(pshandle)
	return os.path.basename(lpString.value)

def get_screen_size():
	return __user32dll.GetSystemMetrics(win32con.SM_CXSCREEN), __user32dll.GetSystemMetrics(win32con.SM_CYSCREEN)
