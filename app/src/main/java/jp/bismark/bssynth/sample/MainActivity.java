package jp.bismark.bssynth.sample;

public class MainActivity {

    static {
        System.loadLibrary("bssynth");
    }
    private static MainActivity _instance = null;

    public static MainActivity getPlayer() {
        if (_instance == null) {
            _instance = new MainActivity();
        }

        synchronized (_instance) {
            return _instance;
        }
    }

    public native int Initialize(int samplerate, int blocksize, String strUnkown);
    public native int SetFile(String midPath);
    public native int Start();
    public native int Stop();
    public native int Seek(int nticks);
    public native int IsPlaying();
    public native int GetKeyControl();
    public native int SetKeyControl(int key);
    public native int GetTotalClocks();
    public native int GetCurrentClocks();
    public native int GetSpeedControl();
    public native int SetSpeedControl(int speed);
    public native int GetPortSelectionMethod();
    public native int SetPortSelectionMethod(int portmode);
}
