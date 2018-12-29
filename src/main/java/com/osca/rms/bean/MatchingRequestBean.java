package com.osca.rms.bean;

import java.util.ArrayList;

public class MatchingRequestBean {
    private String folderpath;
    private String timestamp;
    private ArrayList<Integer> channels;

    public String getFolderpath() {
        return folderpath;
    }

    public void setFolderpath(String folderpath) {
        this.folderpath = folderpath;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public ArrayList<Integer> getChannels() {
        return channels;
    }

    public void setChannels(ArrayList<Integer> channels) {
        this.channels = channels;
    }
}
