package com.osca.rms.logic.audio;

import com.osca.rms.bean.FrameBean;
import com.osca.rms.util.DatabaseUtil;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Matching implements Runnable {
    FrameBean frameBean;
    Connection sqlConnection;

    public Matching(FrameBean frameBean, Connection sqlConnection) {
        this.frameBean = frameBean;
        this.sqlConnection = sqlConnection;
    }

    @Override
    public void run() {
        long stTime = 0;
        Map<Long, Set<Integer>> allHashMap = new HashMap<>();
        try {

            Statement stmt = null;
            ResultSet rs = null;
            int bestCount = 0;
            int bestSong = -1;
            try {

                for (int slide = 0; slide <= 1024; slide += 256) {
                    AudioInputStream convertFormat = null;
                    try {
                        FormatConverter newConverter = new FormatConverter();
                        convertFormat = newConverter.convertFormat(frameBean.getAudioInputStream());
                        FeatureFinder featureFinder = new FeatureFinder();
                        Map<Long, List<Integer>> hashMap = featureFinder.extractFeaturesNew(convertFormat, slide, FeatureFinder.FFT_SHIFT_WIN_SIZE, false);
                        boolean isSilentFrame = hashMap.keySet().size() == 1 && hashMap.get(hashMap.keySet().stream().findFirst().get()) == null;
                        if (isSilentFrame) {
                            return;
                        }
                        for (Long hash : hashMap.keySet()) {
                            Set<Integer> listTimes = new HashSet<>();
                            if (allHashMap.containsKey(hash)) {
                                listTimes = allHashMap.get(hash);
                            } else {
                                allHashMap.put(hash, listTimes);
                            }
                            listTimes.addAll(hashMap.get(hash));
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            convertFormat.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                Map<Long, List<songHash>> persistedHashMap = new HashMap<Long, List<songHash>>();


                try {
                    StringBuilder squerStr = new StringBuilder("SELECT HV.* from fingerprint_hash_value HV where HV.hash_key in (");
                    for (Long hash : allHashMap.keySet()) {
                        squerStr.append(hash + ",");

                    }
                    String finalQueryStr = squerStr.substring(0, squerStr.length() - 1);
                    finalQueryStr += ")";
                    rs = DatabaseUtil.executeQuery(finalQueryStr, sqlConnection);
                    while (rs.next()) {
                        long hashKey = rs.getLong("hash_key");
                        List<songHash> songHashList = new ArrayList<songHash>();
                        if (persistedHashMap.containsKey(hashKey)) {
                            songHashList = persistedHashMap.get(hashKey);
                        } else {
                            persistedHashMap.put(hashKey, songHashList);
                        }
                        songHash sh = new songHash();
                        sh.setSongId(rs.getInt("song_id"));
                        sh.setTime(rs.getInt("hash_value"));
                        songHashList.add(sh);
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                } finally {
                    DatabaseUtil.close(stmt);
                    DatabaseUtil.close(rs);
                }


                Map<Integer, Map<Integer, Integer>> matchMap = new HashMap<Integer, Map<Integer, Integer>>();
                for (Long hash : allHashMap.keySet()) {
                    Set<Integer> times = allHashMap.get(hash);
                    for (int t : times) {
                        if (persistedHashMap.get(hash) != null) {
                            List<songHash> persistedSongTimes = persistedHashMap.get(hash);
                            for (songHash sh : persistedSongTimes) {
                                int offset = Math.abs(sh.getTime() - t);
                                Map<Integer, Integer> tmpMap = null;
                                int currentOffset = -1;
                                if (matchMap.get(sh.getSongId()) == null) {
                                    tmpMap = new HashMap<Integer, Integer>();
                                    tmpMap.put(offset, 1);
                                    matchMap.put(sh.getSongId(), tmpMap);
                                    currentOffset = 1;
                                } else {
                                    tmpMap = matchMap.get(sh.getSongId());
                                    Integer count = tmpMap.get(offset);
                                    if (count == null) {
                                        tmpMap.put(offset, new Integer(1));
                                        currentOffset = 1;
                                    } else {
                                        tmpMap.put(offset, new Integer(count + 1));
                                        currentOffset = count + 1;
                                    }
                                }

                                if (bestCount < currentOffset) {
                                    bestCount = currentOffset;
                                    bestSong = sh.getSongId();
                                }
                            }

                        }
                    }
                }


                Statement stmtStat = null;

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (bestSong > 0 && bestCount >= 18) {
                boolean state = DatabaseUtil.execute("INSERT into frame_match values ('" + frameBean.getChannelId() + "','" + frameBean.getDateTime().toString() + "','" + bestSong + "','" + bestCount + "')", sqlConnection);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}

class songHash {
    private int time;
    private int songId;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public int getSongId() {
        return songId;
    }

    public void setSongId(int songId) {
        this.songId = songId;
    }
}
