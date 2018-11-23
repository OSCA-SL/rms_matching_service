package com.osca.rms;

import com.osca.rms.logic.audio.FeatureFinder;
import com.osca.rms.logic.audio.FormatConverter;
import com.osca.rms.util.FileUtil;

import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class App
{
    public static void main( String[] args )throws Exception
    {
        // /home/osca/Music/Anagathaye-Wayo-www.hirufm.lk.mp3
        File file = new File("./temp/07  - Palayanne naa maa.mp3");
        FileUtil.mp3ToWav(file.getAbsolutePath(),"./temp/test");
        FormatConverter newConverter = new FormatConverter();
        List<AudioInputStream> convertFormat = newConverter.convertFormat(new File("./temp/test.wav"));
        Map<Long, List<Integer>> hashMap = new FeatureFinder().extractFeaturesNew(convertFormat.get(0),0,FeatureFinder.FFT_SHIFT_WIN_SIZE,true);
        System.out.println(hashMap.toString());


    }
}
