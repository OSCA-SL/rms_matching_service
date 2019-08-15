package com.osca.rms;

import com.google.gson.Gson;
import com.osca.rms.bean.MatchingRequestBean;
import com.osca.rms.logic.match.MatchManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchingService extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InputStream inputStream = req.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String request = bufferedReader.readLine();
        MatchingRequestBean requestBean = new Gson().fromJson(request, MatchingRequestBean.class);
        ExecutorService executorService = Executors.newFixedThreadPool(requestBean.getChannels().size());
        long stTime = System.currentTimeMillis();
        MatchChannels(executorService, requestBean);
        System.out.println("Matching took " + (System.currentTimeMillis() - stTime) / 1000 + " S");
        executorService.shutdown();
    }

    private void MatchChannels(ExecutorService executorService, MatchingRequestBean requestBean) {
        for (int i : requestBean.getChannels()) {
            File mediaFile = new File(requestBean.getFolderpath() + "/" + i + ".wav");
            Timestamp timestamp = Timestamp.valueOf(requestBean.getTimestamp());
            MatchManager manager = new MatchManager(mediaFile, timestamp, i);
            executorService.execute(manager);
        }
    }
}
