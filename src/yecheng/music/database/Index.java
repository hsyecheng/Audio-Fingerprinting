package yecheng.music.database;

import yecheng.music.fingerprint.Fingerprint;
import yecheng.music.fingerprint.Hash;
import yecheng.music.model.Match;
import yecheng.music.model.SongMatch;

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

    private final HashMap<Long, Match> hashMap;
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
    private int maxTime = -1;
    public SongMatch search(Fingerprint fp, @SuppressWarnings("SameParameterValue") int minHit){
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
    public SongMatch search(int[] linkTime, int[] linkHash, int minHit){
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
                Match count;
                //if(hashMap.containsKey(hits))
                Long idHash = idHash(id,linkHashMap.get(hash) - time);
                count = hashMap.get(idHash);
                if(count == null) count = new Match(0, linkHashMap.get(hash) - time);
                count.updateCount();
                hashMap.put(idHash,count);
            }
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
            return new SongMatch(-1, new Match(-1, -1));
        }

        hashMap.forEach((hash, countTime) -> {
            if(countTime.getCount() > minHit && countTime.getCount() > maxCount){
                maxId = hash;
                maxCount = countTime.getCount();
                maxTime = countTime.getTime();
            }
        });
        Integer offset = -maxTime;
        return new SongMatch(Hash2id(maxId),  new Match(maxCount, offset));
    }

    public static Long idHash(int id, int time){
        return (long) ((id << 16) + time + (1 << 15));
    }

    public static int Hash2id(Long idHash){
        return (int)(idHash >> 16);
    }
}
