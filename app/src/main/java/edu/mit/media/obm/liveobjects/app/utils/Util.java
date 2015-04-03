package edu.mit.media.obm.liveobjects.app.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class Util {


    public static Bitmap getBitmap(InputStream inputStream) throws IOException{


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

    public static JSONObject getJSON(InputStream inputStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        String jsonConfigString = builder.toString();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(jsonConfigString);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    public static Bitmap cropImage(Bitmap bitmap, ImageView destImageView) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int crop = (width - height) / 2;

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, crop, 0,
                destImageView.getHeight(), destImageView.getHeight());
        return  croppedBitmap;
    }
}
