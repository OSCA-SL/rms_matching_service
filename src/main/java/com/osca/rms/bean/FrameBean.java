package com.osca.rms.bean;

import javax.sound.sampled.AudioInputStream;
import java.sql.Timestamp;


public class FrameBean {
    private AudioInputStream audioInputStream;
    private Timestamp dateTime;
    private int channelId;

    public FrameBean(AudioInputStream audioInputStream, Timestamp dateTime, int channelId){
        this.audioInputStream = audioInputStream;
        this.dateTime = dateTime;
        this.channelId = channelId;
    }

    public AudioInputStream getAudioInputStream() {
        return audioInputStream;
    }

    public void setAudioInputStream(AudioInputStream audioInputStream) {
        this.audioInputStream = audioInputStream;
    }

    public Timestamp getDateTime() {
        return dateTime;
    }

    public void setDateTime(Timestamp dateTime) {
        this.dateTime = dateTime;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }
}

