package com.osca.rms;

import com.google.gson.Gson;
import com.osca.rms.bean.MatchingRequestBean;
import com.osca.rms.util.MatchingUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatchingService extends HttpServlet {

    private final Logger logger = LogManager.getLogger(MatchingService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        InputStream inputStream = req.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String request = "";
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            request = request + line + "\n";
        }
        logger.info("Matching started : "+request);
        MatchingRequestBean requestBean = new Gson().fromJson(request, MatchingRequestBean.class);
        ExecutorService executorService = Executors.newFixedThreadPool(requestBean.getChannels().keySet().size());
        long stTime = System.currentTimeMillis();
        MatchChannels(executorService, requestBean);


        executorService.shutdown();
        logger.info("Matching took " + (System.currentTimeMillis() - stTime) / 1000 + " S");
    }

    private void MatchChannels(ExecutorService executorService, MatchingRequestBean requestBean) {
        for (int i : requestBean.getChannels().keySet()) {
            MatchingUtil matchingUtil = new MatchingUtil(i, requestBean.getFolder_path(), requestBean.getChannels().get(i));
            executorService.execute(matchingUtil);
        }
    }
}
