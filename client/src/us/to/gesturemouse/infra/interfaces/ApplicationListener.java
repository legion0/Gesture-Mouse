package us.to.gesturemouse.infra.interfaces;

import us.to.gesturemouse.dal.ApplicationDAL;

public interface ApplicationListener {
	public abstract void onApplicationChanged(ApplicationDAL applicationName);
}
