package de.feckert.dr;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Constantly loads the backBuffer
 * */
public class InputThread implements Runnable {
	private Thread t;
	private FileInputStream stream;
	public boolean fill = false;
	
	public InputThread(FileInputStream stream) {
		this.t = new Thread(this);
		this.stream = stream;
	}
	
	public void start() {
		this.t.start();
	}
	
	public void run() {
		while(Main.searching) {
			if (fill) { // Set to true once buffer was swapped
				Main.backBuffer = new int[1024*2048];
				for (int i = 0; i < Main.backBuffer.length; i++) {
					try {
						Main.backBuffer[i] = stream.read();
					} catch (IOException e) {
						continue;
					}
				}
			}
		}
	}
}
