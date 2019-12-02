package com.karaoke.utils;

import java.util.Random;

// Used to generate the random BGV
public class RandomGen {

	Random fileSeedRandom;
	private static RandomGen _instance;
	public RandomGen() {
		fileSeedRandom = new Random(System.currentTimeMillis());
	}

	public static RandomGen getInstance() {
		if (_instance == null)
			_instance = new RandomGen();
		return _instance;
	}

	public int getNextIndex(int bound) {
		if (bound == 0) bound = 1;
		return fileSeedRandom.nextInt(bound);
	}
}
