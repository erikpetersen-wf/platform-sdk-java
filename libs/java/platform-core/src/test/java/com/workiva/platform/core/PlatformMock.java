package com.workiva.platform.core;

class PlatformMock extends PlatformCore {
	boolean setAllowedIPs(String allowedIP) {
		return allowedIPs.add(allowedIP);
	}
}

