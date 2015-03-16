package edu.mit.media.obm.liveobjects.storage.wifi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class WifiStorageDriver implements RemoteStorageDriver {

    private static final String LOG_TAG = WifiStorageDriver.class.getSimpleName();

    private Context mContext;
    private final String base_path;
    public WifiStorageDriver(Context context) throws RemoteException {
        mContext = context;
        base_path = WifiStorageConfig.getBaseFolderPath(context)+ "/";
    }

    @Override
    public void writeNewRawFileFromString(final String fileName, final String folderName, final String bodyString) {

        new AsyncTask<String, Void, Void>(){
            @Override
            protected Void doInBackground(String... params) {
                String foldername = params[0];
                try {
                    setUploadDirectory(foldername);
                    writeNewFileFromString(fileName, bodyString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(folderName);

    }

    private void setUploadDirectory(String folderName) throws IOException{
        String path = null;
        try {
            path = WifiStorageConfig.getBasePath(mContext) + "upload.cgi?WRITEPROTECT=ON&UPDIR=/" + folderName + "&FTIME=" + getDateTime16();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        URL url = new URL(path);
        URLConnection urlCon = url.openConnection();
        urlCon.connect();
        Log.d(LOG_TAG, "connecting to path " + path);

    }

    private String getDateTime16() {
        Calendar calendar = Calendar.getInstance();
        int year = (calendar.get(Calendar.YEAR) - 1980) << 9;
        int month = (calendar.get(Calendar.MONTH) + 1) << 5;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hours = calendar.get(Calendar.HOUR_OF_DAY) << 11;
        int minites = calendar.get(Calendar.MINUTE) << 5;
        int seconds = calendar.get(Calendar.SECOND) / 2;
        String rtnStr = "0x" +  Integer.toHexString(year + month + day)
                + Integer.toHexString(hours + minites + seconds);
        return rtnStr;
    }

    private void writeNewFileFromString(String fileName, String bodyString) {
        String result = "";
        final String boundary = "========================";

        try {
            String command = WifiStorageConfig.getBasePath(mContext) + "upload.cgi";
            Log.d(LOG_TAG, "command = " + command);
            URL url = new URL(command);
            HttpURLConnection httpUrlCon = (HttpURLConnection) url.openConnection();
            httpUrlCon.setDoInput(true);
            httpUrlCon.setDoOutput(true);
            httpUrlCon.setUseCaches(false);
            httpUrlCon.setRequestMethod("POST");
            httpUrlCon.setRequestProperty("Charset", "UTF-8");
            httpUrlCon.setRequestProperty("Content-Type",
                    "multipart/form-data;boundary=" + boundary);
            DataOutputStream ds = new DataOutputStream(httpUrlCon.getOutputStream());
            ds.writeBytes("--" + boundary + "\r\n");
            ds.writeBytes("Content-Disposition: form-data; name=\"upload.cgi\";" +
                    " filename=\"" + fileName + "\"" + "\r\n");
            ds.writeBytes("\r\n");
            ds.write(bodyString.getBytes("UTF-8"));
            ds.writeBytes("\r\n");
            ds.writeBytes("--" + boundary + "--" + "\r\n");
            ds.flush();
            ds.close();
            Log.d(LOG_TAG, "writing filename =  " + fileName  +
                            " with bodyString = " + bodyString);

            if(httpUrlCon.getResponseCode() == HttpURLConnection.HTTP_OK){
                StringBuffer sb = new StringBuffer();
                InputStream is = httpUrlCon.getInputStream();
                byte[] data = new byte[1024];
                int leng = -1;
                while((leng = is.read(data)) != -1) {
                    sb.append(new String(data, 0, leng));
                }
                result = sb.toString();
            }
            Log.d(LOG_TAG,"result = " + result);

        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void writeNewRawFileFromStream(String fileName, OutputStream stream) throws IOException {
        // TODO
        throw new UnsupportedOperationException();

    }

    @Override
    public InputStream getInputStreamFromFile(String fileName) throws IOException {
        String path = base_path + fileName;
        Log.d(LOG_TAG, "PATH = " + path);
        URL url = new URL(path);
        URLConnection urlCon = url.openConnection();
        urlCon.connect();
        InputStream inputStream = urlCon.getInputStream();
        return inputStream;
    }

    @Override
    public byte[] getByteArrayFromFile(String filename) throws IOException {
        InputStream inputStream = getInputStreamFromFile(filename);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] byteChunk = new byte[1024];
        int bytesRead = 0;
        while( (bytesRead = inputStream.read(byteChunk)) != -1) {
            byteArrayOutputStream.write(byteChunk, 0, bytesRead);
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public int getNumberOfFiles() {
        //TODO
        throw  new UnsupportedOperationException();


    }

    @Override
    public boolean isFileExisting(String filename) {
        //TODO
        throw  new UnsupportedOperationException();
    }
}
