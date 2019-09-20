package com.osca.rms.util;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class FileUtil {
    private static final Logger logger = LogManager.getLogger(FileUtil.class);
    public static final int FRAME_SIZE_IN_SECOND = 40;

    public static boolean mp3ToWav(String mp3File,String targetPath){

        Encoder encoder = new Encoder();
        EncodingAttributes encoAttrs = new EncodingAttributes();
        AudioAttributes audioAttr = new AudioAttributes();
        String mp3Format = "mp3";
        String wavFormat = "wav";

        encoAttrs.setFormat(wavFormat);

        File source = new File(mp3File);
        File target = new File(targetPath);

        audioAttr.setCodec("pcm_s16le");
        encoAttrs.setAudioAttributes(audioAttr);
        try{
            encoder.encode(source, target, encoAttrs);
        }catch(Exception e){
            logger.error("MP3 to WAV conversion failed : "+e.toString());
            return false;
        }
        return true;
    }
}
