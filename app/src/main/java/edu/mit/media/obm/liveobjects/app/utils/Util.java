package edu.mit.media.obm.liveobjects.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class Util {
    public static Bitmap getBitmap(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byteChunk = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(byteChunk)) != -1) {
            byteArrayOutputStream.write(byteChunk, 0, bytesRead);
        }
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        BitmapFactory.Options bfOptions = new BitmapFactory.Options();
        bfOptions.inPurgeable = true;
        Bitmap resultBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, bfOptions);
        byteArrayOutputStream.close();

        return resultBitmap;
    }


}
