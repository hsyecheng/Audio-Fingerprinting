package yecheng.music;

import yecheng.music.fingerprint.Fingerprint;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by hsyecheng on 2015/6/13.
 * Read WAV file and generate Fingerprints.
 * The sampling rate of the WAV file should be 8000.
 */
public class ReadFile {
    public Fingerprint fingerprint;
    public String Title;
    public String Album;
    public String Artist;

    public ReadFile() {
        super();
    }

    private void getTabs(String filename){
        String[] strings = filename.split("}}");
        if(strings.length < 3){
            Title = filename;
            Album = "";
            Artist = "";
            return;
        }
        Title = strings[0];
        Album = strings[1];
        //remove .wav
        int len = strings[2].length();
        Artist = strings[2].substring(0,len - 4);
    }

    public void readFile(File file) throws Exception{
        AudioInputStream stream;
        try {
            stream = AudioSystem.getAudioInputStream(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        AudioFormat format = stream.getFormat();

        if(format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED){
            throw new Exception("Encoding must be PCM_SIGNED!");
        }

        if(format.getSampleRate() != 8000){
            throw new Exception("SampleRate must be 8000!");
        }

        int len = (int) stream.getFrameLength();

        float[] dataL = new float[len];
        float[] dataR = new float[len];

        ByteBuffer buf = ByteBuffer.allocate(4 * len);
        byte[] bytes = new byte[4 * len];
        try {
            //noinspection ResultOfMethodCallIgnored
            stream.read(bytes);
            buf.put(bytes);
            buf.rewind();

            for(int i = 0; i < len; i++){
                buf.order(ByteOrder.LITTLE_ENDIAN);
                dataL[i] = buf.getShort() / 32768f;
                dataR[i] = buf.getShort() / 32768f;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        float[] data = new float[len];
        for(int i = 0; i < len; i++){
            data[i] = dataL[i] + dataR[i];
            data[i] /= 2;
        }

        int fs = 8000;
        fingerprint = new Fingerprint(data, fs);
        getTabs(file.getName());
    }
}
