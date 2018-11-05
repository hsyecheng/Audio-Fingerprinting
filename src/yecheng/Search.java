package yecheng;

import yecheng.music.ReadFile;
import yecheng.music.database.Index;
import yecheng.music.database.MysqlDB;
import yecheng.music.model.SongMatch;

import java.io.File;

/**
 * Created by hsyecheng on 2015/6/14.
 */
@SuppressWarnings("WeakerAccess")
public class Search {
    public static void main(String[] arg){
        MysqlDB db = new MysqlDB("127.0.0.1", 3306, "musiclibary", "user", "pass");
        Index index = new Index();
        ReadFile rf = new ReadFile();
        File file = null;
        try {
            file = new File(arg[0]);
        }
        catch (IndexOutOfBoundsException e){
            System.err.println("Pass WAV audio file to recognize");
            System.exit(1);
        }
        try {
            rf.readFile(file);
            index.loadDB(db);
            SongMatch song_match = index.search(rf.fingerprint,15);
            if(song_match.getIdSong() != -1) {
                System.out.println(db.getByID(song_match.getIdSong())
                        .put("confidence", song_match.getMatch().getCount())
                        .put("relative_confidence", (song_match.getMatch().getCount()/(double) rf.fingerprint.getLinkList().size())*100)
                        .put("offset", song_match.getMatch().getTime())
                        .put("offset_seconds", song_match.getMatch().getTime() * 0.03225806451612903));
            }
            else
                System.out.println("{}");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
}
