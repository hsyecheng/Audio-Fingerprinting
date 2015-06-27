package yecheng.spectrogram;

/**
 * Created by hsyecheng on 2015/6/11.
 */
public class Window {
    private static final int RECT = 1;
    public static final int HANN = 2;

    private final float[] window;
    private final int windowSize;

    public Window(int windowType, int windowSize) {
        super();
        this.windowSize = windowSize;

        window = new float[windowSize];
        switch (windowType) {
            case RECT:
                initRectWindow(windowSize);
                break;
            case HANN:
                initHannWindow(windowSize);
                break;
        }
    }

    public float[] window(float[] data, int pos){
        int size = windowSize;
        if(pos + size > data.length){
            size = data.length - pos;
        }
        float[] win = new float[windowSize];
        System.arraycopy(data,pos,win,0,size);

        for(int i = 0; i < win.length ; i ++){
            win[i] = win[i] * window[i];
        }
        return win;
    }

    private void initRectWindow(int windowSize){
        for(int i = 0; i < windowSize ; i ++){
            window[i] = 1;
        }
    }

    private void initHannWindow(int windowSize){
        for(int i = 0; i < windowSize ; i ++){
            window[i] = (float) (0.5*(1-Math.cos(2 * Math.PI * i/(windowSize - 1))));
        }
    }

    @SuppressWarnings("unused")
    public static float[] window(float[] data, int pos, int windowType, int windowSize){
        int size = windowSize;
        if(pos + size > data.length){
            size = data.length - pos;
        }
        float[] win = new float[windowSize];
        System.arraycopy(data,pos,win,0,size);
        switch (windowType) {
            case RECT:
                rect(win);
                break;
            case HANN:
                hann(win);
                break;
        }
        return win;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static float[] rect(float[] data){
        //w(t) = 1;
        int n = data.length;
        float w = 1;

        for(int i = 0; i < n ; i ++){
            data[i] = data[i] * w;
        }
        return data;
    }

    @SuppressWarnings("UnusedReturnValue")
    private static float[] hann(float[] data){
        //w(t) = 0.5*(1-cos(2*pi*k/(N-1)));
        int n = data.length;
        float w;

        for(int i = 0; i < n ; i ++){
            w = (float) (0.5*(1-Math.cos(2 * Math.PI * i/(n - 1))));
            data[i] = data[i] * w;
        }
        return data;
    }
}
