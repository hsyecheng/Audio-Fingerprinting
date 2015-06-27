package yecheng;

import yecheng.music.database.MysqlDB;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hsyecheng on 2015/6/12.
 */
@SuppressWarnings("WeakerAccess")
public class Insert {
    public static void main(String[] arg){
        MysqlDB db = new MysqlDB();
        File file = new File(arg[0]);
        String[] name;
        if(file.isDirectory()){
            name = file.list();
        }
        else {
            name = new String[1];
            name[0] = "\b";
        }
        ExecutorService executorService = Executors.newFixedThreadPool(4);

        for (String aName : name) {
            if(!aName.substring(aName.length() - 4).equals(".wav"))
                continue;
            String filename = arg[0] + "\\" + aName;
            executorService.execute(() -> {
                System.out.println(filename);
                db.insert(filename);
            });
        }

        executorService.shutdown();
    }

}
