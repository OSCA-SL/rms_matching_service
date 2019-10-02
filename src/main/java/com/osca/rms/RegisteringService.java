package com.osca.rms;

import com.google.gson.Gson;
import com.osca.rms.bean.RegisteringRequestBean;
import com.osca.rms.logic.audio.FeatureFinder;
import com.osca.rms.logic.audio.FormatConverter;
import com.osca.rms.util.DatabaseUtil;
import com.osca.rms.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.AudioInputStream;
import java.io.*;
import java.sql.Connection;
import java.util.List;
import java.util.Map;


public class RegisteringService extends HttpServlet {

    private static final Logger logger = LogManager.getLogger(RegisteringService.class);

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        InputStream inputStream = req.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String request = "";
        String line = null;

        while ((line = bufferedReader.readLine()) != null) {
            request = request + line + "\n";
        }
        RegisteringRequestBean requestBean = new Gson().fromJson(request, RegisteringRequestBean.class);
        int songId = requestBean.getSongId();
        String wavFilePath = requestBean.getPath();
        wavFilePath = wavFilePath.substring(0, wavFilePath.length() - 4) + "_tmp.wav";

        if (FileUtil.mp3ToWav(requestBean.getPath(), wavFilePath)) {
            FormatConverter formatConverter = new FormatConverter();
            File wavFile = new File(wavFilePath);
            AudioInputStream songStream = formatConverter.convertFormat(wavFile);
            Map<Long, List<Integer>> hashMap = new FeatureFinder().extractFeaturesNew(songStream, 0, FeatureFinder.FFT_WIN_SIZE, true);

            Connection connection = DatabaseUtil.getConnection();
            String query = "";
            String data = "";
            for (Map.Entry<Long, List<Integer>> entry : hashMap.entrySet()) {
                Long hashKey = entry.getKey();
                for (Integer hashValue : entry.getValue()) {
                    data = "('" + hashKey + "','" + hashValue + "','" + songId + "'),";
                    query = query + data;
                }
            }
            query = query.substring(0, query.length() - 1);
            query = "INSERT INTO fingerprints (hash_key,hash_value,song_id) VALUES " + query;
            if (DatabaseUtil.execute(query, connection)) {
                DatabaseUtil.execute("UPDATE songs SET hash_status='1' WHERE id='" + songId + "'", connection);
            } else {
                resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "Database Error");
                DatabaseUtil.execute("UPDATE songs SET hash_status='1' WHERE id='" + songId + "'", connection);
            }
            DatabaseUtil.close(connection);
            FileUtils.forceDelete(wavFile);
        } else {
            resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, "File Error");
        }


    }
}
