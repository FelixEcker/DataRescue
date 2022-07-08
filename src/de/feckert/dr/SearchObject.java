package de.feckert.dr;

import java.util.LinkedList;

public class SearchObject implements Runnable {
	public int stringIndex = 0;
	
	public char[] goal;
	public String goalString;
	public LinkedList<Long> matchPoints = new LinkedList<>();
	public long lastMatch = -1;
	
	public SearchObject(String goal) {
		this.goal = goal.toCharArray();
		this.goalString = goal;
	}
	
	/**
	 * Checks for match at current search index
	 * */
	public void check(char c, long index) {
		if (goal[stringIndex] == c) {
			stringIndex++;
			if (stringIndex == goal.length) {
				lastMatch = index;
				stringIndex = 0;
			}
		} else {
			stringIndex = 0;
			if (goal[stringIndex] == c) {
				stringIndex++;
			}
		}
	}
	
	public void start() {
		new Thread(this).start();
	}
	
	// Adding matches in a seperate thread improved performance,
	// though it seems a little retarded it prevents the search
	// from stopping for ~50ms everytime a match is found.
	public void run() {
		while(Main.searching) {
			if (lastMatch == -1) continue;
			matchPoints.add(lastMatch);
			lastMatch = -1;
		}
	}
	
	public String toString() {
		String[] out = {String.format("List of matches for String \"%s\":\n", goalString)};
		matchPoints.forEach((v) -> out[0] += "		"+String.valueOf(v));
		
		return out[0];
	}
}
