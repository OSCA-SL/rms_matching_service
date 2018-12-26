package com.osca.rms.logic.match;

import com.osca.rms.bean.FrameBean;
import com.osca.rms.logic.audio.Matching;
import com.osca.rms.util.DatabaseUtil;
import com.osca.rms.util.FileUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;

public class MatchManager implements Runnable{
    File mediaFile;
    Timestamp dateTime;
    int channelId;

    public MatchManager(File mediaFile, Timestamp dateTime, int channelId){
        this.mediaFile = mediaFile;
        this.dateTime = dateTime;
        this.channelId = channelId;
        System.out.println(mediaFile.getAbsolutePath());
    }
    @Override
    public void run() {
        Connection sqlConnection = DatabaseUtil.getConnection();
        System.out.println("dfgdfgdfgdfgd");
        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(mediaFile);
            AudioFormat baseFormat = in.getFormat();
            ByteArrayOutputStream bos;
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dateTime.getTime());
            while (true) {
                bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[(int) (baseFormat.getSampleRate() * FileUtil.FRAME_SIZE_IN_SECOND * baseFormat.getFrameSize())];
                int noOfBytes = in.read(buffer);
                if (noOfBytes == -1) {
                    break;
                } else {
                    bos.write(buffer, 0, noOfBytes);
                    bos.flush();
                    bos.close();
                    byte dataArray[] = bos.toByteArray();
                    float actualSampleLength = dataArray.length/(baseFormat.getSampleRate()*baseFormat.getFrameSize());
                    if( actualSampleLength > 10 )
                    {
                        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(dataArray), baseFormat, (long) (baseFormat.getSampleRate() * actualSampleLength));
                        FrameBean frameBean = new FrameBean(ais,new Timestamp(cal.getTime().getTime()),channelId);
                        new Thread(new Matching(frameBean,sqlConnection)).start();
                    }
                    cal.add(Calendar.SECOND, (int) actualSampleLength);
                }
            }

        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(String.valueOf(System.currentTimeMillis()));
        MatchManager matchManager = new MatchManager(new File("temp/test.wav"),Timestamp.valueOf(LocalDateTime.now()),1);
        new Thread(matchManager).start();

    }
}
