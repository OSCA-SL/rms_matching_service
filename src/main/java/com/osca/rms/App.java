package com.osca.rms;

import com.osca.rms.logic.audio.FormatConverter;
import com.osca.rms.util.FileUtil;

import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.util.List;

public class App
{
    public static void main( String[] args )
    {
        // /home/osca/Music/Anagathaye-Wayo-www.hirufm.lk.mp3
        File file = new File("./temp/07  - Palayanne naa maa.mp3");
        FileUtil.mp3ToWav(file.getAbsolutePath(),"./temp/test");
        FormatConverter newConverter = new FormatConverter();
        List<AudioInputStream> convertFormat = newConverter.convertFormat(new File("./temp/test.wav"));

    }
}
