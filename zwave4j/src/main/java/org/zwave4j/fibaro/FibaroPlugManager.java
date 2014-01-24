package org.zwave4j.fibaro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.zwave4j.Manager;
import org.zwave4j.NativeLibraryLoader;
import org.zwave4j.Notification;
import org.zwave4j.NotificationWatcher;
import org.zwave4j.Options;
import org.zwave4j.ValueId;
import org.zwave4j.ZWave4j;

public class FibaroPlugManager implements FibaroPlugManagerItf {

	private String controllerPort;
	private Options options;
	private Manager manager;

	private long homeId;
	private boolean ready = false;
	// private List<Short> nodelist = new ArrayList<Short>();
	private static Map<Short, List<ValueId>> valueslist = new HashMap<Short, List<ValueId>>();
	private static Map<Short, ValueId> valuesswitchlist = new HashMap<Short, ValueId>();
	private static Map<Short, ValueId> valuesconsolist = new HashMap<Short, ValueId>();
	private NotificationWatcher watcher;

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#init(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void init(String defaultconffolder, String saveconffolder,
			String controllerPort) {
		this.controllerPort = controllerPort;
		NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);

		options = Options.create(defaultconffolder, saveconffolder, "");
		options.addOptionBool("ConsoleOutput", false);
		options.lock();

		manager = Manager.create();

		watcher = new NotificationWatcher() {
			@Override
			public void onNotification(Notification notification, Object context) {
				switch (notification.getType()) {
				case DRIVER_READY:
					homeId = notification.getHomeId();
					break;
				case DRIVER_FAILED:
					System.out.println("Driver failed");
					break;
				case DRIVER_RESET:
					System.out.println("Driver reset");
					break;
				case AWAKE_NODES_QUERIED:
					// System.out.println("Awake nodes queried");
					break;
				case ALL_NODES_QUERIED:
					// System.out.println("All nodes queried");
					manager.writeConfig(homeId);
					ready = true;
					break;
				case ALL_NODES_QUERIED_SOME_DEAD:
					// System.out.println("All nodes queried some dead");
					manager.writeConfig(homeId);
					ready = true;
					break;
				/*
				 * case POLLING_ENABLED: System.out.println("Polling enabled");
				 * break; case POLLING_DISABLED:
				 * System.out.println("Polling disabled"); break; case NODE_NEW:
				 * System.out.println(String.format("Node new\n" +
				 * "\tnode id: %d", notification.getNodeId())); break;
				 */
				case NODE_ADDED:
					valueslist.put(notification.getNodeId(),
							new ArrayList<ValueId>());
					break;
				case NODE_REMOVED:
					break;
				case ESSENTIAL_NODE_QUERIES_COMPLETE:
					break;
				case NODE_QUERIES_COMPLETE:
					break;
				case NODE_EVENT:
					break;
				case NODE_NAMING:
					break;
				case NODE_PROTOCOL_INFO:
					break;
				case VALUE_ADDED:
					valueslist.get(notification.getNodeId()).add(
							notification.getValueId());
					if (notification.getValueId().getCommandClassId() == 37)
						valuesswitchlist.put(notification.getNodeId(),
								notification.getValueId());
					else if (notification.getValueId().getCommandClassId() == 49)
						valuesconsolist.put(notification.getNodeId(),
								notification.getValueId());
					break;
				case VALUE_REMOVED:
					valueslist.get(notification.getNodeId()).add(
							notification.getValueId());
					if (notification.getValueId().getCommandClassId() == 37)
						valuesswitchlist.remove(notification.getNodeId());
					else if (notification.getValueId().getCommandClassId() == 49)
						valuesconsolist.remove(notification.getNodeId());
					break;
				case VALUE_CHANGED:
					System.out.println(String.format("Value changed\n"
							+ "\tnode id: %d\n" + "\tcommand class: %d\n"
							+ "\tinstance: %d\n" + "\tindex: %d\n"
							+ "\tvalue: %s", notification.getNodeId(),
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex(),
							getValue(notification.getValueId())));
					break;
				case VALUE_REFRESHED:
					System.out.println(String.format("Value refreshed\n"
							+ "\tnode id: %d\n" + "\tcommand class: %d\n"
							+ "\tinstance: %d\n" + "\tindex: %d"
							+ "\tvalue: %s", notification.getNodeId(),
							notification.getValueId().getCommandClassId(),
							notification.getValueId().getInstance(),
							notification.getValueId().getIndex(),
							getValue(notification.getValueId())));
					break;
				case GROUP:
					break;

				case SCENE_EVENT:
					break;
				case CREATE_BUTTON:
					break;
				case DELETE_BUTTON:
					break;
				case BUTTON_ON:
					break;
				case BUTTON_OFF:
					break;
				case NOTIFICATION:
					System.out.println("Notification");
					break;
				default:
					System.out.println(notification.getType().name());
					break;
				}
			}
		};
		manager.addWatcher(watcher, null);

		manager.addDriver(controllerPort);

	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#stop()
	 */
	@Override
	public void stop() {
		manager.removeWatcher(watcher, null);
		manager.removeDriver(controllerPort);
		Manager.destroy();
		Options.destroy();

	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#listPlugs()
	 */
	@Override
	public Set<Short> listPlugs() {
		return valueslist.keySet();

	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#swithOn(short)
	 */
	@Override
	public void swithOn(short plugno) {
		ValueId id2 = valuesswitchlist.get(plugno);
		AtomicReference<Boolean> val = new AtomicReference<Boolean>();
		manager.getValueAsBool(id2, val);
		manager.setValueAsBool(id2, true);
		// manager.setValueAsBool(id2, true);
		// manager.setValueAsBool(id2, true);
	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#swithOff(int)
	 */
	@Override
	public void swithOff(int plugno) {
		ValueId id2 = valuesswitchlist.get(plugno);
		AtomicReference<Boolean> val = new AtomicReference<Boolean>();
		manager.getValueAsBool(id2, val);
		manager.setValueAsBool(id2, false);

	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#swithAllOn()
	 */
	@Override
	public void swithAllOn() {
		for (Short plugno : valuesswitchlist.keySet()) {
			ValueId id2 = valuesswitchlist.get(plugno);
			AtomicReference<Boolean> val = new AtomicReference<Boolean>();
			manager.getValueAsBool(id2, val);
			manager.setValueAsBool(id2, true);
		}
	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#swithAllOff()
	 */
	@Override
	public void swithAllOff() {
		for (Short plugno : valuesswitchlist.keySet()) {
			ValueId id2 = valuesswitchlist.get(plugno);
			AtomicReference<Boolean> val = new AtomicReference<Boolean>();
			manager.getValueAsBool(id2, val);
			manager.setValueAsBool(id2, false);
		}

	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#getInstantConsoForPlug(int, org.zwave4j.fibaro.AsyncCallback)
	 */
	@Override
	public void getInstantConsoForPlug(final int plugno,
			final AsyncCallback<Float> cb) {

		manager.addWatcher(new NotificationWatcher() {
			@Override
			public void onNotification(Notification notification, Object context) {
				switch (notification.getType()) {
				case VALUE_CHANGED:
					if (notification.getNodeId() == plugno
							&& notification.getValueId().getCommandClassId() == 49)
						cb.onSuccess((Float) getValue(notification.getValueId()));
					break;
				default:
					break;

				}
			}
		}, null);

		ValueId id2 = valuesconsolist.get(plugno);
		AtomicReference<Float> val = new AtomicReference<Float>();
		manager.getValueAsFloat(id2, val);
		manager.refreshValue(id2);
	}

	Map<Observer, Thread> observers = new HashMap<Observer, Thread>();

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#removeObserverForConso(java.util.Observer)
	 */
	@Override
	public void removeObserverForConso(final Observer obs) {
		observers.get(obs).interrupt();
		observers.remove(obs);

	}

	/* (non-Javadoc)
	 * @see org.zwave4j.fibaro.FibaroPlugManagerItf#addObserverForConso(long, short, java.util.Observer)
	 */
	@Override
	public void addObserverForConso(final long delayinms, final short plugno,
			final Observer obs) {
		if (!observers.containsKey(obs)) {
			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(delayinms);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					AsyncCallback<Float> cb = new AsyncCallback<Float>() {

						@Override
						public void onSuccess(Float result) {
							obs.update(null, result);
						}

						@Override
						public void onFailure(Throwable caught) {
							caught.printStackTrace();
						}
					};
					FibaroPlugManager.this.getInstantConsoForPlug(plugno, cb);

				}
			});

			t.start();
			observers.put(obs, t);
		}
	}

	private Object getValue(ValueId valueId) {
		switch (valueId.getType()) {
		case BOOL:
			AtomicReference<Boolean> b = new AtomicReference<>();
			Manager.get().getValueAsBool(valueId, b);
			return b.get();
		case BYTE:
			AtomicReference<Short> bb = new AtomicReference<>();
			Manager.get().getValueAsByte(valueId, bb);
			return bb.get();
		case DECIMAL:
			AtomicReference<Float> f = new AtomicReference<>();
			Manager.get().getValueAsFloat(valueId, f);
			return f.get();
		case INT:
			AtomicReference<Integer> i = new AtomicReference<>();
			Manager.get().getValueAsInt(valueId, i);
			return i.get();
		case LIST:
			return null;
		case SCHEDULE:
			return null;
		case SHORT:
			AtomicReference<Short> s = new AtomicReference<>();
			Manager.get().getValueAsShort(valueId, s);
			return s.get();
		case STRING:
			AtomicReference<String> ss = new AtomicReference<>();
			Manager.get().getValueAsString(valueId, ss);
			return ss.get();
		case BUTTON:
			return null;
		case RAW:
			AtomicReference<short[]> sss = new AtomicReference<>();
			Manager.get().getValueAsRaw(valueId, sss);
			return sss.get();
		default:
			return null;
		}
	}

}
