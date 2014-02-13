package org.zwave4j.fibaro;

import java.util.Observer;
import java.util.Set;

public interface FibaroPlugManagerItf {

	public abstract void init(String defaultconffolder, String saveconffolder,
			String controllerPort);

	public abstract void stop();

	public abstract Set<Short> listPlugs();

	public abstract void swithOn(short plugno);

	public abstract void swithOff(short plugno);

	public abstract void swithAllOn();

	public abstract void swithAllOff();

	public abstract void getInstantConsoForPlug(short plugno,
			AsyncCallback<Float> cb);

	public abstract void removeObserverForConso(Observer obs);

	public abstract void addObserverForConso(long delayinms, short plugno,
			Observer obs);

}