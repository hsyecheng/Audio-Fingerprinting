package yecheng.music.database;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.json.JSONException;
import org.json.JSONObject;
import yecheng.music.ReadFile;
import yecheng.music.fingerprint.Fingerprint;

import java.io.File;
import java.sql.*;

/**
 * Created by hsyecheng on 2015/6/12.
 */
@SuppressWarnings("FieldCanBeLocal")
public class MysqlDB {
    private Connection dbConn;
    private Statement dbStatement;
    private PreparedStatement insertMusic;

    private final String driver = "com.mysql.jdbc.Driver";
    private final String url;
    private final String host;
    private final int port;
    private final String database;
    private final String user;
    private final String password;
    private final String CREATE_MUSIC_INFO_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `MusicInfo` (" +
            "`id` INT NOT NULL AUTO_INCREMENT," +
            "`Title` VARCHAR(200)," +
            "`Artist` VARCHAR(200)," +
            "`Album` VARCHAR(200)," +
            "`FileDir` VARCHAR(400) NULL," +
            "`InfoDir` VARCHAR(400) NULL," +
            "`audio_length` double," +
            "PRIMARY KEY (`id`)," +
            "UNIQUE INDEX `id_UNIQUE` (`id` ASC))" +
            "ENGINE = InnoDB;";
    private final String CREATE_HASH_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS `HashTable` (" +
            "`idHashTable` INT NOT NULL AUTO_INCREMENT," +
            "`Hash` INT NOT NULL," +
            "`MusicInfo_id` INT NOT NULL," +
            "`Time` INT NOT NULL," +
            "PRIMARY KEY (`idHashTable`)," +
            "FOREIGN KEY  (`MusicInfo_id`) REFERENCES `MusicInfo` (`id`) ON DELETE CASCADE," +
            "INDEX `Hash` (`Hash` ASC)  " +
            "KEY_BLOCK_SIZE=1)" +
            "ENGINE = InnoDB;";
    private final String INSERT_SONG_QUERY = "INSERT INTO `MusicInfo` " +
            "(`Title`, `Artist`, `Album`, `FileDir`, `InfoDir`, audio_length) " +
            "VALUES ( ? , ? , ? , ? , ? , ? );";

    public MysqlDB(String host, int port, String database, String user, String password) {
        super();
        this.host = host;
        this.port = port;
        this.database = database;
        this.user = user;
        this.password = password;
        this.url = "jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?user=" + this.user;
        try {
            Class.forName(driver);
            dbConn = DriverManager.getConnection(url, this.user, this.password);
            if(dbConn.isClosed())
                throw new Exception("can not open Database");
            dbStatement = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            dbStatement.setFetchSize(Integer.MIN_VALUE);
        }catch(Exception e){
            e.printStackTrace();
        }

        try {
            insertMusic = dbConn.prepareStatement(INSERT_SONG_QUERY, Statement.RETURN_GENERATED_KEYS);
            dbConn.createStatement().executeUpdate(CREATE_MUSIC_INFO_TABLE_QUERY);
            dbConn.createStatement().executeUpdate(CREATE_HASH_TABLE_QUERY);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    public synchronized void insert(String fileDir){
        ReadFile readFile = new ReadFile();
        try {
            File file = new File(fileDir);
            readFile.readFile(file);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        Fingerprint fp = readFile.fingerprint;
        String Title = readFile.Title;
        String Artist = readFile.Artist;
        String Album = readFile.Album;
        double audio_length = readFile.audio_length;
        String InfoDir = "";

        int id;

        try {
            insertMusic.setString(1,Title);
            insertMusic.setString(2,Artist);
            insertMusic.setString(3,Album);
            insertMusic.setString(4,fileDir);
            insertMusic.setString(5,InfoDir);
            insertMusic.setDouble(6,audio_length);
            insertMusic.executeUpdate();

            ResultSet rs=insertMusic.getGeneratedKeys();
            rs.next();
            id = rs.getInt(1);
            rs.close();
        } catch (SQLException e) {
            System.out.print(e.getSQLState());
            e.printStackTrace();
            return;
        }

        StringBuilder buf = new StringBuilder("INSERT INTO `HashTable` " +
                "(`Hash`, `MusicInfo_id`, `Time`) " +
                "VALUES");

        for(Fingerprint.Link link : fp.getLinkList()){
            Index.Info info  = new Index.Info(id,link);
            buf.append("(").append(info.hash).append(",").append(info.id).append(",").append(info.time).append("),");
        }
        buf.replace(buf.length()-1,buf.length(),";");

        try {
            dbStatement.execute(buf.toString());
        }catch(MySQLSyntaxErrorException e){
            System.err.print("SQL Syntax error: " + buf.toString());
        }catch (SQLException e) {
            System.out.print(e.getSQLState());
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public synchronized ResultSet search(int hash){
        String exec = "SELECT * from `HashTable` WHERE Hash="+ hash + ";";

        ResultSet resultSet;
        try {
            resultSet = dbStatement.executeQuery(exec);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return resultSet;
    }

    public synchronized ResultSet searchAll(int[] hash){
        int len = hash.length;
        String tmp1 = "SELECT * FROM `HashTable` WHERE Hash in(";
        StringBuilder exec = new StringBuilder();

        exec.append(tmp1);
        for(int i = 0; i < len; i ++){
            exec.append(hash[i]).append(",");
        }
        len = exec.length();
        exec.replace(len - 1, len, ");");

        ResultSet resultSet;
        try {
            resultSet = dbStatement.executeQuery(exec.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return resultSet;
    }

    public synchronized ResultSet listAll(){
        String exec = "SELECT * FROM `HashTable`";
        ResultSet resultSet;
        try {
            resultSet = dbStatement.executeQuery(exec);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return resultSet;
    }

    public synchronized JSONObject getByID(int id){
        String exec = "SELECT Title, Artist, Album, audio_length FROM `MusicInfo` WHERE id=" + id;
        ResultSet resultSet;
        try {
            resultSet = dbStatement.executeQuery(exec);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        JSONObject map = new JSONObject();
        try {
            while (resultSet.next()){
                String Title = resultSet.getString(1);
                String Artist = resultSet.getString(2);
                String Album = resultSet.getString(3);
                double audio_length = resultSet.getDouble(4);

                map = map.put("Title",Title).put("Artist",Artist).put("Album",Album).put("audio_length", audio_length);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return map;
    }
}
