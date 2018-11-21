package com.osca.rms.logic.audio;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.tritonus.sampled.convert.PCM2PCMConversionProvider;

public class FormatConverter {

    public static final int RESAPLE_RATE = 8000;

    public List<AudioInputStream> convertFormat(File file) {

        List<AudioInputStream> streams = new ArrayList<AudioInputStream>();
        try {
            AudioInputStream din = null;
            AudioInputStream outDin = null;
            AudioInputStream outDin2 = null;
            if (file == null) {
                return streams;
            }
            PCM2PCMConversionProvider conversionProvider = new PCM2PCMConversionProvider();
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = in.getFormat();
            System.out.println();
            System.out.println("Base Format : " + baseFormat.toString());

            AudioFormat decodedFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(), 16, baseFormat.getChannels(),
                    baseFormat.getChannels() * 2, baseFormat.getSampleRate(),
                    false);

            din = AudioSystem.getAudioInputStream(decodedFormat, in);

            System.out.println("Decorded Format : " + decodedFormat.toString());

            AudioFormat format2 = getFormat2(baseFormat);
            outDin = AudioSystem.getAudioInputStream(format2, din);
            outDin2 = AudioSystem.getAudioInputStream(getFormat3(format2), outDin);

            streams.add(outDin2);
            streams.add(din);
            streams.add(in);
        } catch (Exception e) {
            streams = new ArrayList<AudioInputStream>();
            e.printStackTrace();
        }
        return streams;
    }

    private AudioFormat getFormat() {
        float sampleRate = 11025;//44100;
        int sampleSizeInBits = 16;
        int channels = 2; // mono
        boolean signed = true;
        boolean bigEndian = true;

        AudioFormat outDataFormat = new AudioFormat((float) 8000.0, (int) 8, (int) 1, true, false);
        return outDataFormat;

    }

    private AudioFormat getFormat2(AudioFormat sourceFormat) {
        AudioFormat targetFormat = new AudioFormat(
                sourceFormat.getEncoding(),
                RESAPLE_RATE,
                sourceFormat.getSampleSizeInBits(),
                sourceFormat.getChannels(),
                sourceFormat.getFrameSize(),
                RESAPLE_RATE,
                sourceFormat.isBigEndian());
        return targetFormat;
    }

    private AudioFormat getFormat3(AudioFormat sourceFormat) {
        AudioFormat targetFormat = new AudioFormat(
                sourceFormat.getEncoding(),
                sourceFormat.getSampleRate(),
                sourceFormat.getSampleSizeInBits(),
                1,
                sourceFormat.getFrameSize(),
                sourceFormat.getFrameRate(),
                sourceFormat.isBigEndian());
        return targetFormat;
    }
}