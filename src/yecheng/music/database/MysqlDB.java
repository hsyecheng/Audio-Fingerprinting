package yecheng.music.database;

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
    private final String url = "jdbc:mysql://127.0.0.1:3306/musiclibary?user=yecheng";
    private final String user = "yecheng";
    private final String password = "yecheng";

    public MysqlDB() {
        super();

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

        String exec = "INSERT INTO `musiclibary`.`musicinfo` " +
                "(`Title`, `Artist`, `Album`, `FileDir`, `InfoDir`) " +
                "VALUES ( ? , ? , ? , ? , ? );";
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
        String InfoDir = "";

        /*String exec = "INSERT INTO `musiclibary`.`musicinfo` " +
                "(`Title`, `Artist`, `Album`, `FileDir`, `InfoDir`) " +
                "VALUES ('" + stringReplace("") + "', '" + stringReplace("") + "', '" + stringReplace("") + "', '"
                + stringReplace(fileDir) + "','" + stringReplace("") + "');";*/

        int id;

        /*String exec = "INSERT INTO `musiclibary`.`musicinfo` " +
                "(`Title`, `Artist`, `Album`, `FileDir`, `InfoDir`) " +
                "VALUES ('?,?,?,?,?);";*/
        try {
            insertMusic.setString(1,Title);
            insertMusic.setString(2,Artist);
            insertMusic.setString(3,Album);
            insertMusic.setString(4, fileDir);
            insertMusic.setString(5,InfoDir);
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
        exec = "select idMusicInfo from `musiclibary`.`musicinfo` " +
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



        StringBuilder buf = new StringBuilder("INSERT INTO `musiclibary`.`hashtable` " +
                "(`Hash`, `ID`, `Time`) " +
                "VALUES");

        for(Fingerprint.Link link : fp.getLinkList()){
            Index.Info info  = new Index.Info(id,link);
            buf.append("(").append(info.hash).append(",").append(info.id).append(",").append(info.time).append("),");
        }
        buf.replace(buf.length()-1,buf.length(),";");

        try {
            dbStatement.execute(buf.toString());
        } catch (SQLException e) {
            System.out.print(e.getSQLState());
            e.printStackTrace();
        }
        /*exec = "INSERT INTO `musiclibary`.`hashtable` " +
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
        String exec = "SELECT * from `musiclibary`.`hashtable` WHERE Hash="+ hash + ";";

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
        String tmp1 = "SELECT * FROM `musiclibary`.`hashtable` WHERE Hash in(";
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
        String exec = "SELECT * FROM `musiclibary`.`hashtable`";
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
        String exec = "SELECT * FROM `musiclibary`.`musicinfo` WHERE idMusicInfo=" + id;
        ResultSet resultSet;
        try {
            resultSet = dbStatement.executeQuery(exec);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        Map<String,String> map = new HashMap<>();
        try {
            while (resultSet.next()){
                String Title = resultSet.getString(2);
                String Artist = resultSet.getString(3);
                String Album = resultSet.getString(4);

                map.put("Title",Title);
                map.put("Artist",Artist);
                map.put("Album",Album);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return new JSONObject(map);
    }
}
