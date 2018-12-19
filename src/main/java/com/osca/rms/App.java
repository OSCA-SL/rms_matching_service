package com.osca.rms;

import com.google.gson.Gson;
import com.osca.rms.logic.audio.FeatureFinder;
import com.osca.rms.logic.audio.FormatConverter;
import com.osca.rms.util.FileUtil;
import com.osca.rms.util.ResultBundle;

import javax.sound.sampled.AudioInputStream;
import java.io.File;
import java.util.List;
import java.util.Map;

public class App {
    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        FileUtil.mp3ToWav(file.getAbsolutePath(), "./temp/test");
        FormatConverter newConverter = new FormatConverter();
        List<AudioInputStream> convertFormat = newConverter.convertFormat(new File("./temp/test.wav"));
        Map<Long, List<Integer>> hashMap = new FeatureFinder().extractFeaturesNew(convertFormat.get(0), 0, FeatureFinder.FFT_SHIFT_WIN_SIZE, true);
        System.out.println(hashMap.toString());
        new File("./temp/test.wav").delete();
        ResultBundle<String> retPack = new ResultBundle<String>(ResultBundle.RETURN_TYPE_ERROR, "Connection Failure");
        System.out.println(new Gson().toJson(hashMap));

    }
}
