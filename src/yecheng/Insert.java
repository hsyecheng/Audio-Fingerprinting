package yecheng;

import yecheng.music.database.MysqlDB;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hsyecheng on 2015/6/12.
 */
@SuppressWarnings("WeakerAccess")
public class Insert {
    public static void main(String[] arg){
        MysqlDB db = new MysqlDB("127.0.0.1", 3306, "musiclibary", "user", "pass");
        File file = null;
        try {
            file = new File(arg[0]);
        }catch (IndexOutOfBoundsException e){
            System.err.println("Pass WAV audio file or folder as param");
            System.exit(1);
        }
        String[] name;
        if(file.isDirectory()){
            name = file.list();
        }
        else {
            name = new String[1];
            name[0] = "\b";
        }
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int numElements = 0;
        try{
            FileWriter fw = new FileWriter("fingerprinted.txt");
            for (String aName : name) {
                if(!aName.substring(aName.length() - 4).equals(".wav"))
                    continue;
                fw.write(aName + "\n");
                String filename = arg[0] + File.separator + aName;
                executorService.execute(() -> {
                    System.out.println(filename);
                    db.insert(filename);
                });
                numElements++;

            }
            fw.close();
            executorService.shutdown();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

}
