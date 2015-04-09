package edu.mit.media.obm.liveobjects.storage.wifi;

import android.content.Context;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import edu.mit.media.obm.liveobjects.middleware.storage.RemoteStorageDriver;

/**
 * @author Valerio Panzica La Manna <vpanzica@mit.edu>
 */
public class WifiStorageDriver implements RemoteStorageDriver {

    private static final String LOG_TAG = WifiStorageDriver.class.getSimpleName();

    private Context mContext;

    public WifiStorageDriver(Context context) {
        mContext = context;
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
        HttpURLConnection urlCon = (HttpURLConnection) url.openConnection();
        urlCon.connect();
        Log.d(LOG_TAG, "connecting to path " + path);

//        Log.d(LOG_TAG, "status: " + urlCon.getResponseCode() + "response: " + urlCon.getResponseMessage());
//        if(urlCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
//            Log.d(LOG_TAG, "status: " + urlCon.getResponseCode() + "response: " + urlCon.getResponseMessage());
//        }
        //wait request to be completed
        urlCon.getResponseCode();



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
    public InputStream getInputStreamFromFile(String fileName, String folder) throws IOException, RemoteException {
        String basePath = WifiStorageConfig.getBasePath(mContext);
        String path = basePath + folder + "/" + fileName;
        Log.v(getClass().getSimpleName(), "base_path = " + basePath + ", fileName = " + fileName);
        Log.d(LOG_TAG, "PATH = " + path);
        URL url = new URL(path);
        URLConnection urlCon = url.openConnection();
        urlCon.connect();
        InputStream inputStream = urlCon.getInputStream();
        return inputStream;
    }

    @Override
    public byte[] getByteArrayFromFile(String filename) throws IOException, RemoteException {
        InputStream inputStream = getInputStreamFromFile(filename, "DCIM");
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

    @Override
    public List<String> getFileNamesOfADirectory(String directoryName) {
        String dir = directoryName;
        List<String> fileNames = new ArrayList<>();
        try {
            String basePath = WifiStorageConfig.getBasePath(mContext);
            String requestUrl = basePath + "command.cgi?op=100&DIR=/" + dir;
            String files = getStringFromRequest(requestUrl);
            String[] allFiles = files.split("([,\n])"); // split by newline or comma
            for(int i = 2; i < allFiles.length; i= i + 6) {
                if(allFiles[i].contains(".")) {
                    // File
                    fileNames.add(allFiles[i]);
                }
                else { // Directory, append "/"
                    fileNames.add(allFiles[i] + "/");
                }
            }

        } catch (RemoteException e) {
            Log.e(LOG_TAG, "RemoteException", e);
        }

        return fileNames;
    }

    @Override
    public int getFileSize(String fileName, String folder) throws IOException, RemoteException {
        String basePath = WifiStorageConfig.getBasePath(mContext);
        String requestUrl = basePath + "command.cgi?op=100&DIR=/" + folder;
        String fileListString = getStringFromRequest(requestUrl);
        String[] files = fileListString.split("([\n])");

        int fileSize = -1;
        for (int i = 1; i < files.length; i++) {
            String[] fields = files[i].split("([,])");
            String fieldName = fields[1];
            int fieldSize = Integer.valueOf(fields[2]);

            if (fieldName.equals(fileName)) {
                fileSize = fieldSize;
            }
        }

        if (fileSize < 0) {
            throw new IOException(String.format("no such file '%s/%s'", folder, fileName));
        }

        return fileSize;
    }

    private String getStringFromRequest(String request) {
        String result = "";
        try{
            URL url = new URL(request);
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
