package yecheng;

import org.json.JSONObject;
import yecheng.music.database.Index;
import yecheng.music.database.MysqlDB;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static yecheng.music.database.Index.Hash2id;

/**
 * Created by hsyecheng on 2015/6/16.
 * This is a server providing music recognition service;
 */
@SuppressWarnings("WeakerAccess")
public class Server {
    private final static int port = 1234;

    public static class ServerDataBase{
        class Node{
            int id;
            int time;
        }
        final HashMap<Integer,ArrayList<Node>> Database;
        final HashMap<Long,Integer> hashMap;

        public ServerDataBase() {
            super();
            Database = new HashMap<>(2560000);
            hashMap = new HashMap<>(4000000);

            MysqlDB sqlDB = new MysqlDB("127.0.0.1", 3306, "musiclibary", "root", "dejavu");
            ResultSet rs = sqlDB.listAll();

            try {
                while(rs.next()){
                    int hash = rs.getInt(2);
                    int id = rs.getInt(3);
                    int time = rs.getInt(4);
                    Node node = new Node();
                    node.id = id;
                    node.time = time;

                    ArrayList<Node> idList = Database.get(hash);
                    if(idList == null){
                        idList = new ArrayList<>();
                        Database.put(hash,idList);
                    }
                    idList.add(node);
                }
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private long maxId = -1;
        private int maxCount = -1;
        public int search(int[] linkTime,int[] linkHash, @SuppressWarnings("SameParameterValue") int minHit){
            hashMap.clear();
            for(int i = 0; i < linkHash.length; i ++){
                final ArrayList<Node> list = Database.get(linkHash[i]);
                if(list == null) continue;
                final int time = linkTime[i];
                list.forEach(node -> {
                    long idHash = Index.idHash(node.id, node.time - time);
                    Integer count = hashMap.get(idHash);
                    if(count == null) count = 0;
                    hashMap.put(idHash,count + 1);
                });
            }

            maxId = -1;
            maxCount = -1;

            Map<Long,Integer> list = new HashMap<>();
            hashMap.forEach((hash, integer) -> {
                if (integer > minHit) {
                    list.put(hash,integer);
                }

                if (integer > minHit && integer > maxCount) {
                    maxId = hash;
                    maxCount = integer;
                }
            });

            System.out.println(Hash2id(maxId) + ":" + maxCount);
            return Hash2id(maxId);
        }
    }

    public static void main(String[] arg) {
        System.out.println("Loading Database!");
        ServerDataBase dataBase = new ServerDataBase();
        System.gc();
        System.out.println("Complete!");
        MysqlDB mysqlDB = new MysqlDB("127.0.0.1", 3306, "musiclibary", "user", "pass");

        try {
            ServerSocket serverSocket = new ServerSocket(port);

            Socket socket;

            while (true) {
                try {
                    socket = serverSocket.accept();

                    InputStream is = socket.getInputStream();
                    ByteBuffer buf = ByteBuffer.allocate(4);
                    byte[] a = new byte[4];
                    //noinspection ResultOfMethodCallIgnored
                    is.read(a);
                    buf.put(a);
                    buf.rewind();
                    int size = buf.getInt();
                    buf.rewind();

                    int[] link = new int[size];
                    int[] time = new int[size];

                    int i = 0;
                    while (is.read(a) != -1) {
                        buf.put(a);
                        buf.rewind();
                        if (i < size) {
                            link[i] = buf.getInt();
                        } else {
                            time[i - size] = buf.getInt();
                        }
                        i++;
                        buf.rewind();
                    }

                    int id = dataBase.search(time, link, 18);
                    JSONObject jsonObject = mysqlDB.getByID(id);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream());
                    if(jsonObject == null)
                        writer.write("");
                    else writer.write(jsonObject.toString());
                    writer.close();
                    System.out.println(jsonObject.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}


