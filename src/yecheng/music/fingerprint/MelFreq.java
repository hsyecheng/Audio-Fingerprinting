package yecheng.music.fingerprint;

/**
 * Created by hsyecheng on 2015/6/19.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MelFreq {
    //Mel = 2595*log10(1+freq/700)

    public static float Mel2Freq(float melFreq){
        return (float) ((Math.pow(10,(melFreq/2595) - 1)) * 700);
    }

    @SuppressWarnings("WeakerAccess")
    public static float Freq2Mel(float freq){
        return (float) (2595 * Math.log10(1+freq/700));
    }

    public static float[] MelBand(float[] freq){
        for(int i = 0; i < freq.length; i++){
            freq[i] = Freq2Mel(freq[i]);
        }
        return freq;
    }

}
