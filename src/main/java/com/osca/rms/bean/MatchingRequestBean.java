package com.osca.rms.bean;


import java.util.ArrayList;
import java.util.Map;

public class MatchingRequestBean {
    private String folder_path;
    private Map<Integer, ArrayList<ClipBean>> channels;

    public String getFolder_path() {
        return folder_path;
    }

    public void setFolder_path(String folder_path) {
        this.folder_path = folder_path;
    }

    public Map<Integer, ArrayList<ClipBean>> getChannels() {
        return channels;
    }

    public void setChannels(Map<Integer, ArrayList<ClipBean>> channels) {
        this.channels = channels;
    }
}
