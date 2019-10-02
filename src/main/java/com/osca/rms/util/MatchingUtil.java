package com.osca.rms.util;

import com.osca.rms.bean.ClipBean;
import com.osca.rms.logic.match.MatchManager;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MatchingUtil implements Runnable {
    private int channelId;
    private String folderPath;
    private ArrayList<ClipBean> clipList;

    public MatchingUtil(int channelId, String folderPath, ArrayList<ClipBean> clipList){
        this.channelId = channelId;
        this.folderPath = folderPath;
        this.clipList = clipList;
    }

    @Override
    public void run() {
        for (ClipBean clipBean: clipList) {
            File mediaFile = new File(folderPath+"/"+clipBean.getFile_name());
            Timestamp timestamp = Timestamp.valueOf(clipBean.getTimestamp());
            new MatchManager(mediaFile,timestamp,channelId).run();

        }
    }
}
