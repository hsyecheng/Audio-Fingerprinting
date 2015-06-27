package yecheng;

import org.json.JSONException;
import org.json.JSONObject;
import yecheng.music.ReadFile;
import yecheng.music.fingerprint.Fingerprint;
import yecheng.music.fingerprint.Hash;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by hsyecheng on 2015/6/16.
 * This is a client that can print music information.
 */
@SuppressWarnings("WeakerAccess")
public class Client {
    @SuppressWarnings("WeakerAccess")
    private final static String ip = "127.0.0.1";
    private final static int port = 1234;

    public static void main(String[] arg){
        long startTime = System.currentTimeMillis();
        try {
            Socket s = new Socket(ip,port);
            OutputStream out = s.getOutputStream();
            ReadFile rf = new ReadFile();
            File file = new File(arg[0]);
            try {
                rf.readFile(file);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Can't open file!");
                return;
            }
            ArrayList<Fingerprint.Link> link = rf.fingerprint.getLinkList();


            ByteBuffer buf = ByteBuffer.allocate(4);
            byte[] a = new byte[4];
            buf.putInt(link.size());
            buf.rewind();
            buf.get(a);
            out.write(a);
            buf.rewind();
            for(int i = 0; i < link.size(); i ++){
                //System.out.println(Hash.hash(link.get(i)));
                buf.putInt(Hash.hash(link.get(i)));
                buf.rewind();
                buf.get(a);
                out.write(a);
                buf.rewind();
            }

            for(int i = 0; i < link.size(); i ++){
                //System.out.println(link.get(i).start.intTime);
                buf.putInt(link.get(i).start.intTime);
                buf.rewind();
                buf.get(a);
                out.write(a);
                buf.rewind();
            }
            s.shutdownOutput();
            BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));
            String line = reader.readLine();

            String Title,Artist,Album;
            if(!line.equals("{}")) {
                try {
                    JSONObject jsonObject = new JSONObject(line);
                    Title = jsonObject.getString("Title");
                    Artist = jsonObject.getString("Artist");
                    Album = jsonObject.getString("Album");

                    System.out.println("Title; " + Title);
                    System.out.println("Artist; " + Artist);
                    System.out.println("Album; " + Album);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            s.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.toString());
        }
        System.out.println(System.currentTimeMillis() - startTime);
    }
}
