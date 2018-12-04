package com.osca.rms.util;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncodingAttributes;

import java.io.File;

public class FileUtil {
    public static boolean mp3ToWav(String mp3File,String targetPath){

        Encoder encoder = new Encoder();
        EncodingAttributes encoAttrs = new EncodingAttributes();
        AudioAttributes audioAttr = new AudioAttributes();
        String mp3Format = "mp3";
        String wavFormat = "wav";

        encoAttrs.setFormat(wavFormat);

        File source = new File(mp3File);
        File target = new File(targetPath+".wav");

        audioAttr.setCodec("pcm_s16le");
        encoAttrs.setAudioAttributes(audioAttr);
        try{
            encoder.encode(source, target, encoAttrs);
        }catch(Exception e){
            System.out.println("Mp3 to wav converting failed! "+e.getMessage());
            return false;
        }
        return true;
    }
}
