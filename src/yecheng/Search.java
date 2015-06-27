package yecheng;

import yecheng.music.ReadFile;
import yecheng.music.database.Index;
import yecheng.music.database.MysqlDB;

import java.io.File;

/**
 * Created by hsyecheng on 2015/6/14.
 */
@SuppressWarnings("WeakerAccess")
public class Search {
    public static void main(String[] arg){
        MysqlDB db = new MysqlDB();
        Index index = new Index();
        ReadFile rf = new ReadFile();
        File file = new File(arg[0]);
        try {
            rf.readFile(file);
            index.loadDB(db);
            int id = index.search(rf.fingerprint,15);
            System.out.println(id);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.toString());
        }
    }
}
