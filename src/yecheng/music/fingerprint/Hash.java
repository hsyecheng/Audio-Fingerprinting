package yecheng.music.fingerprint;

/**
 * Created by hsyecheng on 2015/6/12.
 */
public class Hash {
    public static int hash(Fingerprint.Link link){
        int dt = link.end.intTime - link.start.intTime; //
        int df = link.end.intFreq - link.start.intFreq + 300; // 300
        int freq = link.start.intFreq; // 5000

        return freq + 5000*(df + 600 * dt);
    }

    @SuppressWarnings("unused")
    public static int[] hash2link(int hash){
        int freq = hash % 5000;
        int df = (hash / 5000) % 600;
        int dt = hash / 5000 / 600;

        return new int[] {freq,df,dt};
    }
}
