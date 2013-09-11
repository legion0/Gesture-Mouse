package com.example.gesturemouseclient.infra.interfaces;

import com.example.gesturemouseclient.dal.ApplicationDAL;

public interface ApplicationListener {
	public abstract void onApplicationChanged(ApplicationDAL applicationName);
}
