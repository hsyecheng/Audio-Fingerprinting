package yecheng.music.database;

import com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException;
import org.json.JSONException;
import org.json.JSONObject;
import yecheng.music.ReadFile;
import yecheng.music.fingerprint.Fingerprint;

import java.io.File;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

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
            dbConn = DriverManager.getConnection(url, user, password);
            if(dbConn.isClosed())
                throw new Exception("can not open Database");
            dbStatement = dbConn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            dbStatement.setFetchSize(Integer.MIN_VALUE);
        }catch(Exception e){
            e.printStackTrace();
        }

        String exec = "INSERT INTO `musiclibary`.`MusicInfo` " +
                "(`Title`, `Artist`, `Album`, `FileDir`, `InfoDir`, audio_length) " +
                "VALUES ( ? , ? , ? , ? , ? , ? );";
        try {
            insertMusic = dbConn.prepareStatement(exec, Statement.RETURN_GENERATED_KEYS);
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

        /*String exec = "INSERT INTO `musiclibary`.`MusicInfo` " +
                "(`Title`, `Artist`, `Album`, `FileDir`, `InfoDir`) " +
                "VALUES ('" + stringReplace("") + "', '" + stringReplace("") + "', '" + stringReplace("") + "', '"
                + stringReplace(fileDir) + "','" + stringReplace("") + "');";*/

        int id;

        /*String exec = "INSERT INTO `musiclibary`.`MusicInfo` " +
                "(`Title`, `Artist`, `Album`, `FileDir`, `InfoDir`) " +
                "VALUES ('?,?,?,?,?);";*/
        try {
            insertMusic.setString(1,Title);
            insertMusic.setString(2,Artist);
            insertMusic.setString(3,Album);
            insertMusic.setString(4, fileDir);
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

        /*int id;
        ResultSet rs;
        exec = "select idMusicInfo from `musiclibary`.`MusicInfo` " +
                "where FileDir='" + stringReplace(fileDir) +   "';";
        try {
            rs = dbStatement.executeQuery(exec);
            rs.next();
            id = rs.getInt(1);
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }*/



        StringBuilder buf = new StringBuilder("INSERT INTO `musiclibary`.`HashTable` " +
                "(`Hash`, `ID`, `Time`) " +
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
        /*exec = "INSERT INTO `musiclibary`.`HashTable` " +
                "(`Hash`, `ID`, `Time`) " +
                "VALUES ('" + stringReplace("") + "', '" + stringReplace("") + "', '" + stringReplace("") + "');";
        try {
            dbStatement.execute(exec);
        } catch (SQLException e) {
            System.out.print(e.getSQLState());
            e.printStackTrace();
        }*/
    }

    @SuppressWarnings("unused")
    public synchronized ResultSet search(int hash){
        String exec = "SELECT * from `musiclibary`.`HashTable` WHERE Hash="+ hash + ";";

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
        String tmp1 = "SELECT * FROM `musiclibary`.`HashTable` WHERE Hash in(";
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
        String exec = "SELECT * FROM `musiclibary`.`HashTable`";
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
        String exec = "SELECT Title, Artist, Album, audio_length FROM `musiclibary`.`MusicInfo` WHERE idMusicInfo=" + id;
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
