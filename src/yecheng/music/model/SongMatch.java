package yecheng.music.model;


public class SongMatch{

    private int idSong;
    private Match match;

    public SongMatch(int idSong, Match match) {
        this.idSong = idSong;
        this.match = match;
    }

    public int getIdSong() {
        return idSong;
    }

    public Match getMatch() {
        return match;
    }
}