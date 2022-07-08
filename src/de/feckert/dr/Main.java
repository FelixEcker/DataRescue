package de.feckert.dr;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Scanner;

public class Main {
	public static Scanner userInput = new Scanner(System.in);
	public static volatile int[] backBuffer = new int[1024*2048];
	public static volatile boolean searching = true;
	public static InputThread inputThread;
	
	public static void main(String[] args) throws IOException {
		System.out.println("Data Rescue Utility - String Search.");
		System.out.println("This Utility Program searches through a binary dump for Strings,");
		System.out.println("please note that the search ignores spaces or if a match is a full word or not!");
		System.out.println("Search is also case sensitive!\n\n");
		
		System.out.print("Please specify a binary file to search through] ");
		File file = new File(userInput.nextLine());
		
		if (!file.exists()) {
			System.err.println("File not found!");
			return;
		}
		
		System.out.print("Please enter the number of Strings to be searched for] ");
		int nStrings = userInput.nextInt(); userInput.nextLine();
		String[] strings = new String[nStrings];
		
		// Get Search Strings
		for (int i = 0; i < nStrings; i++) {
			System.out.printf("String %s: ", i+1);
			strings[i] = userInput.nextLine();
		}
		
		searchFile(file, strings);
	}
	
	public static void searchFile(File source, String[] searchStrings) throws IOException {
		FileInputStream stream = new FileInputStream(source);
		
		// Create SearchObject for each SearchString
		LinkedList<SearchObject> searchObjects = new LinkedList<>();
		for (int i = 0; i < searchStrings.length; i++) {
			SearchObject temp = new SearchObject(searchStrings[i]);
			searchObjects.add(temp);
			temp.start();
		}
		
		// Stat Variables
		// Some of these have to be arrays even though they hold just a single value
		// so that they can be used in lambdas
		long[] index = {1};
		long totalSize = source.length();
		long totalSizeMB = totalSize/(1024*1024);
		long startTime = System.currentTimeMillis();
		
		new Thread(new Runnable() {
			public void run() {
				long kbs = 0;
				long passedTime = System.currentTimeMillis();
				while(searching) {
					try {
						System.out.print(""); // This needs to be here else it doesnt print for some reason
						if (index[0] % 1024 == 0) { // Refresh passedTime and processing speed
							passedTime = (System.currentTimeMillis()-startTime)/1000;
							kbs = (index[0]/1024)/passedTime;
						}
						
						// Redisplay Stats
						if (index[0] % 1024*1024 == 0) {
							System.out.printf("Progress: %d/%d MB ", index[0]/(1024*1024), totalSizeMB);
							System.out.printf(" (%d kb/s, %d seconds passed)\r", 
									kbs,
									passedTime);
						}
					} catch (ArithmeticException e) {
						continue;
					}
				}
			}
		}).start();
		
		// Prepare backbuffer
		int[] buffer;
		for (int i = 0; i < backBuffer.length; i++) {
			backBuffer[i] = stream.read();
		}
		inputThread = new InputThread(stream);
		inputThread.start();
		System.out.println("Searching with 2 Buffers (size 2MB)!");
		while (index[0]-1 < totalSize) {
			// Swap Buffers
			buffer = backBuffer;
			inputThread.fill = true;
			
			// Search buffer for matches
			for (int i = 0; i < buffer.length; i++) {
				int curChar = buffer[i];
				if (curChar == -1) {  // exit if ends reached
					stream.close();
					searching = false;
					System.out.println("\nFinished search, results below: ");
					searchObjects.forEach((v) -> System.out.println(v.toString()));
					return;
				}
				
				searchObjects.forEach((v) -> {
					v.check((char) curChar, index[0]-1);
				});
				index[0]++;
			}
		}
	}
}
