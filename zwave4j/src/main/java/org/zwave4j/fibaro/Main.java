package org.zwave4j.fibaro;

public class Main {

	
	public static void main(String[] args) throws InterruptedException {
		FibaroPlugManager manager = new FibaroPlugManager();
		
		manager.init("/home/barais/app/fibaro/zwave4j/", "/home/barais/app/fibaro/zwave4j/","/dev/ttyUSB0");
		
		Thread.sleep(2000);
		manager.swithOff((short) 5);
		Thread.sleep(2000);
		manager.swithOn((short) 5);
		Thread.sleep(5000);
		manager.swithOff((short) 6);
		Thread.sleep(2000);
		manager.swithOn((short)6);
	}
}
