package yecheng.music.tool;

/**
 * Created by hsyecheng on 2015/5/31.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class StringReplace {
    public static String stringReplace(String str){
        String tmp;
        tmp = str.replace("\\","\\\\");
        tmp = tmp.replace("\"","\\\"");
        tmp = tmp.replace("\'","\\\'");
        return tmp;
    }

    public static String stringReplace(CharSequence seq){
        if(seq == null) return "";
        return stringReplace(seq.toString());
    }
}
