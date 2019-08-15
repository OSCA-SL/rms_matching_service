package com.osca.rms.bean;

public class RegisteringRequestBean {

    private int songId;
    private String path;

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
