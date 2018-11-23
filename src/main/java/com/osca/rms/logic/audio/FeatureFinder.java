package com.osca.rms.logic.audio;

import com.osca.rms.model.Complex;
import com.osca.rms.model.FFT;
import org.apache.commons.lang3.ArrayUtils;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.*;

public class FeatureFinder {

    public static final int FFT_WIN_SIZE = 4096;
    public static final int FFT_SHIFT_WIN_SIZE = 1024;
    public final int UPPER_LIMIT = 300;//300;
    public final int LOWER_LIMIT = 35;//40;
    public final int[] RANGE = new int[] { 40, 80, 120, 180, UPPER_LIMIT + 1 };
    private static final int FUZ_FACTOR = 2;


    public Map<Long, List<Integer>> extractFeaturesNew(AudioInputStream in, int silde, int shiftWin, boolean isRegister)
    {
        long timeMills = System.currentTimeMillis();
        System.out.println("ExtractFeatures started!");
        Map<Long, List<Integer>>  retMap = new HashMap<Long, List<Integer>>();


        //Skip pre defined(pass as param) number of bits from the begining
        while( silde > 0 )
        {
            try {
                int readUpto = silde;
                if( silde > 4096 )
                {
                    readUpto = 4096;
                }
                silde = silde - 4096;

                byte[] buffer = new byte[(int) 4096];
                in.read(buffer, 0, readUpto);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int time = 0;
        double highscores[];
        long points[];
        float totalRms = 0;
        int totalBuckets = 0;
        try {
            List<Byte> audioData = new ArrayList<Byte>();
            List<Integer> maxPointsForSilents = new ArrayList<Integer>();
            int numSilents = 0;
            while (true) {
                int count = 0;
                byte[] buffer = new byte[(int) FeatureFinder.FFT_WIN_SIZE];
                if( audioData.isEmpty() )
                {
                    //read a window to the buffer
                    count = in.read(buffer, 0, FeatureFinder.FFT_WIN_SIZE);
                    //if we reach at the end stop the loop
                    if( count != FeatureFinder.FFT_WIN_SIZE )
                    {
                        break;
                    }
                    //Copy ending bytes(FFT_WIN_SIZE-FFT_SHIFT_WIN_SIZE) to a new array.
                    byte[] subarray = ArrayUtils.subarray(buffer, shiftWin, buffer.length);
                    audioData.addAll( Arrays.asList(ArrayUtils.toObject(subarray)) );

                }
                else if( audioData.size() == (FeatureFinder.FFT_WIN_SIZE - shiftWin) )
                {
                    Byte[] prevData = new Byte[audioData.size()];
                    prevData = audioData.toArray(prevData);
                    //Copy previous data to the buffer
                    System.arraycopy(ArrayUtils.toPrimitive(prevData), 0, buffer, 0, audioData.toArray().length);
                    //Fill remaining bytes(size of shiftWin) from in to the buffer.
                    count = in.read(buffer, audioData.toArray().length-1, shiftWin);
                    if( count != shiftWin )
                    {
                        break;
                    }
                    audioData = new ArrayList<Byte>();
                    //Copy ending bytes(FFT_WIN_SIZE-FFT_SHIFT_WIN_SIZE) to a new array.
                    byte[] subarray = ArrayUtils.subarray(buffer, shiftWin, buffer.length);
                    audioData.addAll( Arrays.asList(ArrayUtils.toObject(subarray)) );
                }
                else
                {
                    break;
                }

                Complex[] results = new Complex[FeatureFinder.FFT_WIN_SIZE];
                Complex[] complex = new Complex[FeatureFinder.FFT_WIN_SIZE];
                for (int i = 0; i < FeatureFinder.FFT_WIN_SIZE; i++) {
                    complex[i] = new Complex(buffer[i], 0);
                }
                results = FFT.fft(complex);

                highscores = new double[5];
                for (int j = 0; j < 5; j++) {
                    highscores[j] = 0;
                }

                points = new long[5];
                for (int j = 0; j < 5; j++) {
                    points[j] = 0;
                }

                for (int freq = LOWER_LIMIT; freq < UPPER_LIMIT - 1; freq++) {
                    // Get the magnitude:
                    double mag = Math.log(results[freq].abs() + 1);

                    // Find out which range we are in:
                    int index = getIndex(freq);

                    // Save the highest magnitude and corresponding frequency:
                    if (mag > highscores[index]) {
                        highscores[index] = mag;
                        points[index] = freq;
                    }
                }
                double highOfHScore = 0;
                for(double hScore : highscores)
                {
                    if( highOfHScore < hScore )
                    {
                        highOfHScore = hScore;
                    }
                }
                maxPointsForSilents.add( (int)highOfHScore*100 );


                long h = hash(points[0], points[1], points[2],points[3]);


                List<Integer> listPoints = null;
                if ((listPoints = retMap.get(h)) == null) {
                    listPoints = new ArrayList<Integer>();
                    listPoints.add(time);
                    retMap.put(h, listPoints);
                } else {
                    listPoints.add(time);
                }

                time++;

                float rms = 0f;
                float[] samples = new float[buffer.length];
                for(int i = 0, s = 0; i < buffer.length;) {
                    int sample = 0;

                   sample = buffer[i++];

                    samples[s++] = sample / 32768f;
                }

                for(float sample : samples) {
                    rms += sample * sample;
                }
                rms = rms*1000000;
                if( rms < 11000 )
                {
                    numSilents++;
                }

            }

            if( !isRegister && numSilents >= 25 )
            {
                retMap = new HashMap<Long, List<Integer>>();
                retMap.put((long)(numSilents), null);
                return retMap;
            }

        } catch (Exception e) {
            e.printStackTrace();
            retMap = new HashMap<Long, List<Integer>>();
        }

        System.out.println("ExtractFeatures completed within "+ (System.currentTimeMillis() - timeMills) +" Ms" );
        return retMap;
    }

    public int getIndex(int freq) {
        int i = 0;
        while (RANGE[i] < freq)
            i++;
        return i;
    }

    private long hash(long p1, long p2, long p3, long p4) {
        return (p4 - (p4 % FUZ_FACTOR)) * 100000000 + (p3 - (p3 % FUZ_FACTOR))
                * 100000 + (p2 - (p2 % FUZ_FACTOR)) * 100
                + (p1 - (p1 % FUZ_FACTOR));
    }
}
