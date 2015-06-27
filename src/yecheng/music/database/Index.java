package yecheng.music.database;

import yecheng.music.fingerprint.Fingerprint;
import yecheng.music.fingerprint.Hash;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by hsyecheng on 2015/6/12.
 */
public class Index {
    public static class Info{
        public final int hash;
        public final int id;
        public final int time;

        public Info(int id,Fingerprint.Link link) {
            super();
            this.id = id;
            this.time = link.start.intTime;
            this.hash = Hash.hash(link);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }

    private final HashMap<Long,Integer> hashMap;
    private MysqlDB sqlDB;
    public Index() {
        super();
        hashMap = new HashMap<>(400000);
    }

    public void loadDB(MysqlDB mysql){
        sqlDB = mysql;
    }

    private long maxId = -1;
    private int maxCount = -1;
    public int search(Fingerprint fp, @SuppressWarnings("SameParameterValue") int minHit){
        ArrayList<Fingerprint.Link> linkList =  fp.getLinkList();

        int[] linkHash = new int[linkList.size()];
        int[] linkTime = new int[linkList.size()];
        for(int i = 0; i < linkHash.length; i ++){
            linkHash[i] = Hash.hash(linkList.get(i));
            linkTime[i] = linkList.get(i).start.intTime;
        }

        return search(linkTime, linkHash,minHit);
    }

    @SuppressWarnings("WeakerAccess")
    public int search(int[] linkTime, int[] linkHash, int minHit){
        HashMap<Integer,Integer> linkHashMap = new HashMap<>(linkHash.length);
        for(int i = 0; i < linkHash.length; i ++){
            linkHashMap.put(linkHash[i],linkTime[i]);
        }

        ResultSet rs = sqlDB.searchAll(linkHash);

        try {
            while(rs.next()){
                int hash = rs.getInt(2);
                int id = rs.getInt(3);
                int time = rs.getInt(4);
                
                //Hits hits = new Hits(id, linkHashMap.get(hash) - time);
                Integer count;
                //if(hashMap.containsKey(hits))
                Long idHash = idHash(id,linkHashMap.get(hash) - time);
                count = hashMap.get(idHash);
                if(count == null) count = 0;
                hashMap.put(idHash,count + 1);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        hashMap.forEach((hash, integer) -> {
            if(integer > minHit && integer > maxCount){
                maxId = hash;
                maxCount = integer;
            }
        });

        return Hash2id(maxId);
    }

    public static Long idHash(int id, int time){
        return (long) ((id << 16) + time + (1 << 15));
    }

    public static int Hash2id(Long idHash){
        return (int)(idHash >> 16);
    }
}
