package yecheng.music.fingerprint;

/**
 * Created by hsyec_000 on 2015/6/11.
 * Find local peaks in fft data.
 */
@SuppressWarnings("WeakerAccess")
public class FindPeaks {
    final float[] power;
    final int[] locate;

    private final int nPeaks;


    public FindPeaks(int nPeaks){
        super();
        this.nPeaks = nPeaks;

        power = new float[nPeaks];
        locate = new int[nPeaks];
    }

    public void findComplexPeaks(float[] data, int neighborRange){
        int len = data.length / 2;
        for(int i = 0 ; i < nPeaks; i++){
            power[i] = -500;
            locate[i] = -1;
        }

        float[] data_power = new float[len];

        for(int i = 0; i < len; i ++){
            data_power[i] = (float) (10 * Math.log10(data[2 * i] * data[2 * i]
                    + data[2 * i + 1] * data[2 * i + 1]));
        }

        for(int k = 0; k < len; k ++){
            float pi = data_power[k];
            boolean add = true;
            for(int j = 0; j < neighborRange; j ++) {
                float pl,pr;

                if(k - neighborRange >= 0) {
                    pl = data_power[k - neighborRange];
                }else pl = pi - 1;

                if(k + neighborRange < len) {
                    pr = data_power[k + neighborRange];
                }else pr = pi - 1;

                if (pi < pl && pi < pr) {
                    add = false;
                }
            }
            if(add) add(pi, k);
        }
    }

    private void add(float p, int loc){
        for(int i = 0; i < power.length; i++){
            if(power[i] < p){
                for(int j = power.length - 1; j > i; j --){
                    power[j] = power[j-1];
                    locate[j] = locate[j-1];
                }
                power[i] = p;
                locate[i] = loc;
                break;
            }
        }
    }
/*
    private int inBand(float freq, float[] freqRange){
        int size = freqRange.length;
        if(freq < freqRange[0] || freq > freqRange[size - 1]) {
            return -1;
        }
        for(int i = 0; i < size - 1; i ++){
            if(freqRange[i + 1] > freq)
                return i;
        }
        return -1;
    }
*/
}
