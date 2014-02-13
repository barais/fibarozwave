/*
 * Copyright (c) 2013 Alexander Zagumennikov
 *
 * SOFTWARE NOTICE AND LICENSE
 *
 * This file is part of ZWave4J.
 *
 * ZWave4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ZWave4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZWave4J.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.zwave4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zagumennikov
 */
public class Main {

	private static long homeId;
	private static boolean ready = false;
	private static ValueId id = null;
	private static List<Short> nodelist = new ArrayList<Short>();
	private static List<ValueId> valueslist = new ArrayList<ValueId>();

	
	
	public static void main(String[] args) throws IOException {
		NativeLibraryLoader.loadLibrary(ZWave4j.LIBRARY_NAME, ZWave4j.class);

		final Options options = Options.create(args[0], "/home/barais/app/fibaro/zwave4j/", "");
		options.addOptionBool("ConsoleOutput", false);
		options.lock();

		final Manager manager = Manager.create();

		final NotificationWatcher watcher = new NotificationWatcher() {
			@Override
			public void onNotification(Notification notification, Object context) {
				switch (notification.getType()) {
				case DRIVER_READY:
					System.out.println(String.format("Driver ready\n"
							+ "\thome id: %d", notification.getHomeId()));

					homeId = notification.getHomeId();
					break;
				case DRIVER_FAILED:
					System.out.println("Driver failed");
					break;
				case DRIVER_RESET:
					System.out.println("Driver reset");
					break;
				case AWAKE_NODES_QUERIED:
					System.out.println("Awake nodes queried");
					break;
				case ALL_NODES_QUERIED:
					System.out.println("All nodes queried");
					manager.writeConfig(homeId);
					ready = true;
					break;
				case ALL_NODES_QUERIED_SOME_DEAD:
					System.out.println("All nodes queried some dead");
					manager.writeConfig(homeId);
					ready = true;
					break;
				case POLLING_ENABLED:
					System.out.println("Polling enabled");
					break;
				case POLLING_DISABLED:
					System.out.println("Polling disabled");
					break;
				case NODE_NEW:
					System.out.println(String.format("Node new\n"
							+ "\tnode id: %d", notification.getNodeId()));
					break;
				case NODE_ADDED:
					System.out.println(String.format("Node added\n"
							+ "\tnode id: %d", notification.getNodeId()));
					nodelist.add(notification.getNodeId());
					break;
				case NODE_REMOVED:
					System.out.println(String.format("Node removed\n"
							+ "\tnode id: %d", notification.getNodeId()));
					break;
				case ESSENTIAL_NODE_QUERIES_COMPLETE:
					System.out
							.println(String.format(
									"Node essential queries complete\n"
											+ "\tnode id: %d",
									notification.getNodeId()));
					break;
				case NODE_QUERIES_COMPLETE:
					System.out.println(String.format("Node queries complete\n"
							+ "\tnode id: %d", notification.getNodeId()));
					break;
				case NODE_EVENT:
					System.out.println(String.format("Node event\n"
							+ "\tnode id: %d\n" + "\tevent id: %d",
							notification.getNodeId(), notification.getEvent()));
					break;
				case NODE_NAMING:
					System.out.println(String.format("Node naming\n"
							+ "\tnode id: %d", notification.getNodeId()));
					break;
				case NODE_PROTOCOL_INFO:
					System.out
							.println(String.format("Node protocol info\n"
									+ "\tnode id: %d\n" + "\ttype: %s",
									notification.getNodeId(), manager
											.getNodeType(
													notification.getHomeId(),
													notification.getNodeId())));
					break;
				case VALUE_ADDED:
					System.out.println(String.format("Value added\n"
							+ "\tnode id: %d\n" + "\tcommand class: %d\n"
							+ "\tinstance: %d\n" + "\tindex: %d\n"
							+ "\tgenre: %s\n" + "\ttype: %s\n"
							+ "\tlabel: %s\n" + "\tvalue: %s", notification
							.getNodeId(), notification.getValueId()
							.getCommandClassId(), notification.getValueId()
							.getInstance(), notification.getValueId()
							.getIndex(), notification.getValueId().getGenre()
							.name(),
							notification.getValueId().getType().name(), manager
									.getValueLabel(notification.getValueId()),
							getValue(notification.getValueId())));
					if (notification.getNodeId() == 6)
						valueslist.add(notification.getValueId());
					break;
				case VALUE_REMOVED:
					System.out.println(String.format("Value removed\n"
							+ "\tnode id: %d\n" + "\tcommand class: %d\n"
							+ "\tinstance: %d\n" + "\tindex: %d", notification
							.getNodeId(), notification.getValueId()
							.getCommandClassId(), notification.getValueId()
							.getInstance(), notification.getValueId()
							.getIndex()));
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
					id = notification.getValueId();
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
					System.out.println(String.format("Group\n"
							+ "\tnode id: %d\n" + "\tgroup id: %d",
							notification.getNodeId(),
							notification.getGroupIdx()));
					break;

				case SCENE_EVENT:
					System.out.println(String.format("Scene event\n"
							+ "\tscene id: %d", notification.getSceneId()));
					break;
				case CREATE_BUTTON:
					System.out.println(String.format("Button create\n"
							+ "\tbutton id: %d", notification.getButtonId()));
					break;
				case DELETE_BUTTON:
					System.out.println(String.format("Button delete\n"
							+ "\tbutton id: %d", notification.getButtonId()));
					break;
				case BUTTON_ON:
					System.out.println(String.format("Button on\n"
							+ "\tbutton id: %d", notification.getButtonId()));
					break;
				case BUTTON_OFF:
					System.out.println(String.format("Button off\n"
							+ "\tbutton id: %d", notification.getButtonId()));
					break;
				case NOTIFICATION:
					System.out.println("Notification");
					System.out.println(notification.toString());

					break;
				default:
					System.out.println(notification.getType().name());
					break;
				}
			}
		};
		manager.addWatcher(watcher, null);

		final String controllerPort = args[1];

		manager.addDriver(controllerPort);

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				ValueId id2 = null;
				AtomicReference<Boolean> val = new AtomicReference<Boolean>();

				for (ValueId id1 : valueslist) {

					if (id1.getCommandClassId() == 37) {
						id2 = id1;
						break;
					}
				}
				//System.err.println(manager.isValueReadOnly(id2));
				
				
				AtomicReference<Float> val7 = new AtomicReference<Float>();

				ValueId id5 = null;
				for (ValueId id1 : valueslist) {

					if (id1.getCommandClassId() == 49) {
						id5 = id1;
						break;
					}
				}


				while (true) {
					manager.getValueAsBool(id2, val);

					System.err.println("switch value set"
							+ manager.setValueAsBool(id2, !val.get()));
					System.err.println("switch value set"
							+ manager.setValueAsBool(id2, !val.get()));
					System.err.println("switch value set"
							+ manager.setValueAsBool(id2, !val.get()));
					
					manager.refreshValue(id5);
					//System.err.println("polled " + manager.isValuePolled(id5));
					manager.getValueAsFloat(id5, val7);
					System.err.println("power value get " + val7.get());
					
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});

		//t.start();

		final BufferedReader br = new BufferedReader(new InputStreamReader(
				System.in));

		String line;
		do {
			line = br.readLine();
			// System.err.println(line);
			/*
			 * if (!ready || line == null) { System.err.println("not ready");
			 * continue; }
			 */
 
			switch (line) {
			case "on":
				manager.setValueAsBool(id, true);
				manager.switchAllOn(homeId);
				break;
			case "on1":
				manager.setValueAsBool(id, true);
				break;
			case "on2":
				manager.switchAllOn(homeId);
				break;
			case "off":
				// ValueId id = new ValueId(homeId, nodeId, genre,
				// commandClassId, instance, index, type)
				manager.setValueAsBool(id, false);
				manager.switchAllOff(homeId);
				break;
			case "off1":
				// ValueId id = new ValueId(homeId, nodeId, genre,
				// commandClassId, instance, index, type)
				manager.setValueAsBool(id, false);

				break;
			case "off2":
				// ValueId id = new ValueId(homeId, nodeId, genre,
				// commandClassId, instance, index, type)
				manager.switchAllOff(homeId);

				break;
			case "test":
				// ValueId id = new ValueId(homeId, nodeId, genre,
				// commandClassId, instance, index, type)
				for (ValueId id1 : valueslist) {

					System.err.println(id1.getCommandClassId());
					System.err.println(manager.getValueLabel(id1));
					System.err.println(manager.getValueUnits(id1));
					AtomicReference<Boolean> val = new AtomicReference<Boolean>();
					// System.err.println(manager.getValueAsBool(id1, new
					// AtomicReference<Boolean>()))(id1));
				}
				break;

			case "test1":
				// ValueId id = new ValueId(homeId, nodeId, genre,
				// commandClassId, instance, index, type)
				AtomicReference<Boolean> val = new AtomicReference<Boolean>();
				AtomicReference<Float> val7 = new AtomicReference<Float>();

				ValueId id2 = null;
				ValueId id5 = null;
				for (ValueId id1 : valueslist) {

					if (id1.getCommandClassId() == 37) {
						id2 = id1;
						System.err.println(id1.getCommandClassId() + " "
								+ id1.getType().toString());
						manager.getValueAsBool(id2, val);
						System.err.println(val.get());

					} else if (id1.getCommandClassId() == 49) {
						id5 = id1;
						System.err.println(id1.getCommandClassId() + " "
								+ id1.getType().toString());
					}
					System.err.println(id1.getCommandClassId() + " "
							+ manager.isValueReadOnly(id1));

				}
				manager.refreshValue(id5);
				System.err.println(manager.isValueReadOnly(id2));

				// manager.pressButton(id2);

				// manager.releaseButton(id2);

				// manager.isValuePolled(id5);

				// manager.enablePoll(id5);

				System.err.println("polled " + manager.isValuePolled(id5));
				System.err.println("switch value get"
						+ manager.getValueAsBool(id2, val));
				manager.getValueAsFloat(id5, val7);
				System.err.println("switch value " + val.get());
				System.err.println("power value get " + val7.get());
				// manager.getValueAsBool(id2, val);
				// val.set(val.get());
				System.err.println("switch value set"
						+ manager.setValueAsBool(id2, !val.get()));
				System.err.println("switch value set"
						+ manager.setValueAsBool(id2, !val.get()));

				System.err.println("switch value get"
						+ manager.getValueAsBool(id2, val));
				System.err.println("switch value " + val.get());

				break;
			case "test2":
				// ValueId id = new ValueId(homeId, nodeId, genre,
				// commandClassId, instance, index, type)
				AtomicReference<Boolean> val1 = new AtomicReference<Boolean>();
				ValueId id3 = null;
				for (ValueId id1 : valueslist) {

					if (id1.getCommandClassId() == 37)
						id3 = id1;
				}
				manager.getValueAsBool(id3, val1);
				val1.set(val1.get());
				break;
			case "test3":
				// ValueId id = new ValueId(homeId, nodeId, genre,
				// commandClassId, instance, index, type)
				AtomicReference<Boolean> val2 = new AtomicReference<Boolean>();
				ValueId id4 = null;
				for (ValueId id1 : valueslist) {

					if (id1.getCommandClassId() == 37)
						id4 = id1;
				}
				System.err.println(manager.setValueAsBool(id4, true));
				break;

			}
		} while (line != null && !line.equals("q"));

		t.interrupt();
		br.close();

		manager.removeWatcher(watcher, null);
		manager.removeDriver(controllerPort);
		Manager.destroy();
		Options.destroy();
	}

	private static Object getValue(ValueId valueId) {
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
