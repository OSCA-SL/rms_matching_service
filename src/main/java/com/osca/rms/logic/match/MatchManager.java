package com.osca.rms.logic.match;

import com.osca.rms.bean.FrameBean;
import com.osca.rms.logic.audio.Matching;
import com.osca.rms.util.DatabaseUtil;
import com.osca.rms.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Calendar;

public class MatchManager implements Runnable{
    File mediaFile;
    Timestamp dateTime;
    int channelId;
    private static final Logger logger = LogManager.getLogger(MatchManager.class);

    public MatchManager(File mediaFile, Timestamp dateTime, int channelId){
        this.mediaFile = mediaFile;
        this.dateTime = dateTime;
        this.channelId = channelId;

    }
    @Override
    public void run() {
        Connection sqlConnection = DatabaseUtil.getConnection();
        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(mediaFile);
            AudioFormat baseFormat = in.getFormat();
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(dateTime.getTime());
            while (true) {

                byte[] buffer = new byte[(int) (baseFormat.getSampleRate() * FileUtil.FRAME_SIZE_IN_SECOND * baseFormat.getFrameSize())];
                int noOfBytes = in.read(buffer);
                if (noOfBytes == -1) {
                    break;
                } else {

                    float actualSampleLength = buffer.length/(baseFormat.getSampleRate()*baseFormat.getFrameSize());
                    if( actualSampleLength > 10 )
                    {
                        AudioInputStream ais = new AudioInputStream(new ByteArrayInputStream(buffer), baseFormat, (long) (baseFormat.getSampleRate() * actualSampleLength));
                        FrameBean frameBean = new FrameBean(ais,new Timestamp(cal.getTime().getTime()),channelId);
                        new Matching(frameBean,sqlConnection).run();
                    }
                    cal.add(Calendar.SECOND, (int) actualSampleLength);
                }
            }
            FileUtils.forceDelete(mediaFile);
        } catch (UnsupportedAudioFileException e) {
            logger.error("Unsupported Audio File : " + e.toString());
        } catch (IOException e) {
            logger.error("File Error : " + e.toString());
        }
        DatabaseUtil.close(sqlConnection);

    }
}
