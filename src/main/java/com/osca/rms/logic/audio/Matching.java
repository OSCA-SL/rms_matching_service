package com.osca.rms.logic.audio;

import com.osca.rms.bean.FrameBean;
import com.osca.rms.util.DatabaseUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioInputStream;
import java.sql.*;
import java.util.*;

public class Matching implements Runnable {
    private FrameBean frameBean;
    private Connection sqlConnection;

    private static final Logger logger = LogManager.getLogger(Matching.class);

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
                        logger.error("File Error : " + e.toString());
                    } finally {
                        try {
                            convertFormat.close();
                        } catch (Exception e) {
                            logger.error("IO Error : " + e.toString());
                        }
                    }
                }

                Map<Long, List<songHash>> persistedHashMap = new HashMap<Long, List<songHash>>();


                try {
                    StringBuilder squerStr = new StringBuilder("SELECT HV.* from fingerprints HV where HV.hash_key in (");
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
                    logger.error("DB Error : " + e.toString());
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
                logger.error("File Error : " + e.toString());
            }

            if (bestSong > 0) {
                DatabaseUtil.execute("INSERT into frame_match values ('" + frameBean.getChannelId() + "','" + frameBean.getDateTime().toString() + "','" + bestSong + "','" + bestCount + "')", sqlConnection);
                if (bestCount >= 18) {
                    Timestamp endTime = new Timestamp(frameBean.getDateTime().getTime() + (40 * 1000));
                    ResultSet resultSet = DatabaseUtil.executeQuery("SELECT id,song_id,match_id,gaps FROM channels WHERE id='" + frameBean.getChannelId() + "'", sqlConnection);
                    if (resultSet.next()) {
                        int song_id = resultSet.getInt(2);
                        int match_id = resultSet.getInt(3);
                        int gaps = resultSet.getInt(4);
                        if (song_id == bestSong & gaps <= 2) {
                            DatabaseUtil.execute("UPDATE channels SET gaps='0' where id='" + frameBean.getChannelId() + "'", sqlConnection);
                            DatabaseUtil.execute("UPDATE matches SET end='" + endTime.toString() + "' where id='" + match_id + "'", sqlConnection);
                        } else {
                            int recordId = DatabaseUtil.executeInsert("INSERT INTO matches (start,end,channel_id,song_id) VALUES ('" + frameBean.getDateTime().toString() + "','" + endTime.toString() + "','" + frameBean.getChannelId() + "','" + bestSong + "')", sqlConnection);
                            if (recordId > 0) {
                                DatabaseUtil.execute("UPDATE channels SET song_id='" + bestSong + "',match_id='" + recordId + "',gaps='0' where id='" + frameBean.getChannelId() + "'", sqlConnection);
                            } else {
                                logger.error("Match Insertion Failed");
                            }
                        }
                    } else {
                        logger.error("Channel not Found");
                    }
                } else {
                    DatabaseUtil.execute("UPDATE channels SET gaps = gaps + 1 where id='" + frameBean.getChannelId() + "'", sqlConnection);
                }
            } else {
                DatabaseUtil.execute("UPDATE channels SET gaps = gaps + 1 where id='" + frameBean.getChannelId() + "'", sqlConnection);
            }

        } catch (Exception e) {
            logger.error("File Error : " + e.toString());
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
