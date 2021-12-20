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
        String database = System.getenv().getOrDefault("YECHENG_DATABASE_NAME", "musiclibary");
        int port = Integer.parseInt(System.getenv().getOrDefault("YECHENG_DATABASE_PORT", "3306"));
        String host = System.getenv().getOrDefault("YECHENG_DATABASE_HOST", "localhost");
        String user = System.getenv().getOrDefault("YECHENG_DATABASE_USER", "user");
        String pass = System.getenv().getOrDefault("YECHENG_DATABASE_PASS", "pass");
        MysqlDB db = new MysqlDB(host, port, database, user, pass);
        File file = null;
        try {
            file = new File(arg[0]);
        }catch (IndexOutOfBoundsException e){
            System.err.println("Pass WAV audio file or folder as param");
            System.exit(1);
        }
        String[] name;
        boolean directory = true;
        if(file.isDirectory()){
            name = file.list();
        }
        else {
            name = new String[1];
            name[0] = file.getAbsolutePath();
            directory = false;
        }
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        int numElements = 0;
        try{
            FileWriter fw = new FileWriter("fingerprinted.txt");
            for (String aName : name) {
                if(!aName.toLowerCase().endsWith(".wav"))
                    continue;
                fw.write(aName + "\n");
                String filename = directory ? arg[0] + File.separator + aName : aName;
                executorService.execute(() -> {
                    System.out.println("Fingerprinting: " + filename);
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
