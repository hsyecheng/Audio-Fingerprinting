package yecheng.spectrogram;

import org.jtransforms.fft.FloatFFT_1D;

import java.util.ArrayList;

/**
 * Created by hsyecheng on 2015/6/11.
 */
public class Spectrogram {
    public final ArrayList<float[]> stft;
    public final float[] freq;
    public final float[] time;

    private final FloatFFT_1D fft;
    private final float[] fftData;
    public Spectrogram(float[] data, @SuppressWarnings("SameParameterValue") int windowsType, int windowSize, int overlap, float fs) {
        super();
        int dataLen = data.length;
        int stepSize = windowSize - overlap;
        stft = new ArrayList<>();
        freq = new float[windowSize / 2];
        time = new float[dataLen / stepSize];
        fft = new FloatFFT_1D(windowSize);
        fftData = new float[windowSize * 2];

        Window window = new Window(windowsType, windowSize);

        for(int i = 0; i < dataLen; ) {
            if(i + windowSize > data.length){
                break;
            }
            float[] win;
            try {
                win = window.window(data, i);
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            //calc fft
            calcFFT(win);
            //add
            addSTFT();
            i += stepSize;
        }
        calcFreq(fs);
        calcTime(fs, stepSize);
    }

    private void calcFFT(float[] win){
        for(int i = 0; i < win.length ; i ++){
            fftData[i * 2] = win[i];
            fftData[i * 2 + 1] = 0;
        }
        fft.complexForward(fftData);
    }

    private void addSTFT(){
        float[] half = new float[fftData.length / 2];
        System.arraycopy(fftData,0,half,0,fftData.length / 2);
        stft.add(half);
    }

    private void calcFreq(float fs){

        for(int i = 0; i < freq.length; i++){
            freq[i] = fs * i / freq.length;
        }
    }

    private void calcTime(float fs, int stepSize){
        for(int i = 0; i < time.length; i++) {
            time[i] = stepSize * i / fs;
        }
    }
}
