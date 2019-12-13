package com.karaoke.gk3decoder;

public class SetOptimize {
    static {
        System.loadLibrary("optsetupbox");
    }

    private static SetOptimize _instance = null;

    public static SetOptimize getOptimizer() {
        if (_instance == null) {
            _instance = new SetOptimize();
        }

        synchronized (_instance) {
            return _instance;
        }
    }

    /**
     * A native method that is implemented by the 'optsetupbox' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
