package com.tickrobot.ide.nativeticide.tools;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by xushun on 2017/1/9.
 */

public class ReadProgram {
    public static byte[] readProgram() {
        FileInputStream fis = null;
        File file = new File("/mnt/sdcard/firmware.hex");

        // every line, except last one, has has 45 bytes (including \r\n)
        int programLines = (int) Math.ceil(file.length() / 45.0);
        // every line has 32 bytes of program data (excluding checksums, addresses, etc.)
        int unusedBytes = 45 - 32;
        // calculate program length according to program lines and unused bytes
        int programLength = (int) file.length() - (programLines * unusedBytes);
        // the actualy program data is half the size, as the hex file represents hex data in individual chars
        programLength /= 2;
        // create a byte array with the program length
        byte[] program = new byte[programLength];

        try {
            // open the file stream
            Log.d("d", "opening hex file");
            fis = new FileInputStream(file);
            Log.d("d", "Total program size (in bytes) : " + programLength);
            Log.d("d", "Total file size to read (in bytes) : " + fis.available());

            int content;
            int lineIndex = 0;
            int lineNumber = 1;
            int programIndex_read = 0;
            char[] line = new char[45];
            // read the file byte by byte
            while ((content = fis.read()) != -1) {
                // append byte to the line
                line[lineIndex++] = (char) content;
                // when the line is complete
                if (content == 10) {
                    // take only the actual program data form the line
                    for (int index = 9; index < lineIndex - 4; index += 2) {
                        // convert hexadecimals represented as chars into bytes
                        program[programIndex_read++] = Integer.decode("0x" + line[index] + line[index + 1]).byteValue();
                    }
                    // start a new line
                    lineIndex = 0;
                }
            }
        } catch (IOException e) {
            Log.d("d", "reading hex failed: " + e.getMessage());
        } finally {
            try {
                if (fis != null)
                    fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return program;
    }
}
