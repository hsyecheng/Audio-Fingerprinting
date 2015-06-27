package yecheng.music.fingerprint;

import yecheng.spectrogram.Spectrogram;
import yecheng.spectrogram.Window;

import java.util.ArrayList;

/**
 * Created by hsyecheng on 2015/6/11.
 */
@SuppressWarnings("FieldCanBeLocal")
public class Fingerprint {
    private final int NPeaks = 3;
    private final int fftSize = 512;
    private final int overlap = 256;
    private final int C = 32;
    private final int peakRange = 5;

    private final ArrayList<Peak> peakList = new ArrayList<>();
    private final ArrayList<Link> linkList = new ArrayList<>();
    private final float[] freq;
    private final float[] time;

    private final float[] range_time = {1f, 3f};
    private final float[] range_freq = {-600f, 600f};
    //private final float[] melBand = MelFreq.MelBand(new float[] {150f, 550f, 950f, 1350f, 1750f});
    private final int[] Band = {11,22,35,50,69,91,117,149,187,231};

    private final float minFreq = 100;
    private final float maxFreq = 2000;
    private final float minPower = 0;

    public Fingerprint(float[] data, float fs) {
        super();
        Spectrogram spectrogram = new Spectrogram(data, Window.HANN, fftSize, overlap, fs);
        ArrayList<float[]> stft = spectrogram.stft;
        freq = spectrogram.freq;
        time = spectrogram.time;

        ArrayList<Peak> tmp = new ArrayList<>(C * NPeaks);
        int size = stft.size();
        int bandNum = Band.length - 1;
        for (int b = 0; b < bandNum; b++) {
            for (int i = 0; i < size; i++) {
                if (i != 0) {
                    if (i % C == 0 || i == size - 1) {
                        //Filter
                        tmp.removeIf(peak -> {
                            float peakFreq = freq[peak.intFreq];
                            return peakFreq < minFreq || peakFreq > maxFreq;
                        });
                        tmp.removeIf(peak -> peak.power <= minPower);

                        tmp.sort((o1, o2) ->
                                        Double.compare(o2.power, o1.power)
                        );

                        int end = tmp.size() < NPeaks ? tmp.size() : NPeaks;
                        peakList.addAll(tmp.subList(0, end));
                        tmp.clear();
                    }
                }

                float[] fft = stft.get(i);
                int len = Band[b + 1] - Band[b];
                int start = Band[b] * 2;
                len *= 2;
                float[] fft_band = new float[len];
                System.arraycopy(fft, start, fft_band, 0, len);
                FindPeaks find = new FindPeaks(NPeaks);
                find.findComplexPeaks(fft_band, peakRange);
                float[] power = find.power;
                int[] loc = find.locate;

                for (int j = 0; j < power.length; j++) {
                    loc[j] += Band[b];
                }

                for (int j = 0; j < NPeaks; j++) {
                    if (loc[j] == -1) {
                        continue;
                    }
                    Peak p = new Peak();
                    p.intFreq = loc[j];
                    p.intTime = i;
                    p.power = power[j];

                    tmp.add(p);
                }


                //if (true) {
                /*tmp.sort((o1, o2) ->
                                Double.compare(o2.power, o1.power)
                );
                int end = tmp.size() < NPeaks ? tmp.size() : NPeaks;
                peakList.addAll(tmp.subList(0, end));
                tmp.clear();*/
                //}
            }
        }

        peakList.sort((o1, o2) -> o1.intTime - o2.intTime);
        link(true);
    }

    private int inBand(int intFreq){
        int size = Band.length;
        if(intFreq < Band[0] || intFreq > Band[size - 1]) {
            return -1;
        }
        for(int i = 0; i < size - 1; i ++){
            if(Band[i + 1] > intFreq)
                return i;
        }
        return -1;
    }

    private void link(@SuppressWarnings("SameParameterValue") boolean band) {
        int n = peakList.size();
        for (int i = 0; i < n; i++) {
            Peak p1 = peakList.get(i);
            if (p1 == null) {
                continue;
            }

            //time start|end
            int tStart;
            int tEnd;
            int k;
            for (k = i + 1; k < n; k++) {
                float t = time[p1.intTime];
                float t2 = time[peakList.get(k).intTime];
                if (t2 - t >= range_time[0])
                    break;
            }
            tStart = k;
            for (; k < n; k++) {
                float t = time[p1.intTime];
                float t2 = time[peakList.get(k).intTime];
                if (t2 - t >= range_time[1])
                    break;
            }
            tEnd = k;
            //freq start|end
            float fstart, fend;
            fstart = freq[p1.intFreq] + range_freq[0];
            fend = freq[p1.intFreq] + range_freq[1];

            for (int i2 = tStart; i2 < tEnd; i2++) {
                Peak p2 = peakList.get(i2);
                if (p2 == null) {
                    continue;
                }

                if (band) {
                    int b1 = inBand(p1.intFreq);
                    int b2 = inBand(p2.intFreq);

                    //TODO
                    if(b1 == b2 && b1 != -1){
                        Link l = new Link(p1, p2);
                        linkList.add(l);
                     }
                } else {
                    if (freq[p2.intFreq] >= fstart && freq[p2.intFreq] <= fend) {
                        Link l = new Link(p1, p2);
                        linkList.add(l);
                    }
                }
            }
        }
    }

    public ArrayList<Link> getLinkList() {
        return linkList;
    }

    @SuppressWarnings("unused")
    public ArrayList<Peak> getPeakList() {
        return peakList;
    }

    public static class Peak {
        public int intFreq;
        public float power;
        public int intTime;
    }

    public static class Link {
        public final Peak start;
        public final Peak end;

        final float[] tmp = new float[3];

        public Link(Peak s, Peak e) {
            super();
            this.start = s;
            this.end = e;
            tmp[0] = s.intFreq;
            tmp[1] = e.intFreq;
            tmp[2] = e.intTime - s.intTime;
        }
    }
}


