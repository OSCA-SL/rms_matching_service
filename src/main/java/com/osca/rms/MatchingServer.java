package com.osca.rms;

import com.google.gson.Gson;
import com.osca.rms.bean.MatchingRequestBean;
import com.osca.rms.logic.audio.FeatureFinder;
import com.osca.rms.logic.audio.FormatConverter;
import com.osca.rms.logic.match.MatchManager;
import com.osca.rms.util.FileUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioInputStream;
import java.io.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchingServer extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InputStream inputStream = req.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String request = bufferedReader.readLine();
        MatchingRequestBean requestBean = new Gson().fromJson(request, MatchingRequestBean.class);
        ExecutorService executorService = Executors.newFixedThreadPool(requestBean.getChannels().size());
        MatchChannels(executorService,requestBean);
        executorService.shutdown();
    }

    private void MatchChannels(ExecutorService executorService, MatchingRequestBean requestBean) {
        for (int i:requestBean.getChannels()) {
            File mediaFile = new File(requestBean.getFolderpath()+"/"+i+".wav");
            Timestamp timestamp = Timestamp.valueOf(requestBean.getTimestamp());
            MatchManager manager = new MatchManager(mediaFile,timestamp,i);
            executorService.execute(manager);
        }
    }

    public static void main(String[] args) throws Exception {
        File file = new File(args[0]);
        FileUtil.mp3ToWav(file.getAbsolutePath(), "./temp/test");
        FormatConverter newConverter = new FormatConverter();
        AudioInputStream convertFormat = newConverter.convertFormat(new File("./temp/test.wav"));
        Map<Long, List<Integer>> hashMap = new FeatureFinder().extractFeaturesNew(convertFormat, 0, FeatureFinder.FFT_SHIFT_WIN_SIZE, true);
        System.out.println(hashMap.toString());
        new File("./temp/test.wav").delete();
        System.out.println(new Gson().toJson(hashMap));

    }
}
