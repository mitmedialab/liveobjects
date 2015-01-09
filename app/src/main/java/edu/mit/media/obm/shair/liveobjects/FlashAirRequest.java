package edu.mit.media.obm.shair.liveobjects;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;


public class FlashAirRequest {
    static public String getString(String command) {
        String result = "";
        try{
            URL url = new URL(command);
            URLConnection urlCon = url.openConnection();
            urlCon.connect();
            InputStream inputStream = urlCon.getInputStream();
            BufferedReader bufreader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            StringBuffer strbuf = new StringBuffer();
            String str;
            while ((str = bufreader.readLine()) != null) {
                if(strbuf.toString() != "") strbuf.append("\n");
                strbuf.append(str);
            }
            result =  strbuf.toString();
        }catch(MalformedURLException e) {
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
        }
        catch(IOException e) {
            Log.e("ERROR", "ERROR: " + e.toString());
            e.printStackTrace();
        }
        return result;
    }
}