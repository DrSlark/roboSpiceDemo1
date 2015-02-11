package ztt.robospicedownloadtest1.request;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.SpiceRequest;


import org.apache.http.HttpStatus;

import java.io.File;
import java.io.FileInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import roboguice.util.temp.Ln;
import ztt.robospicedownloadtest1.GlobalConstants;
import ztt.robospicedownloadtest1.model.DownLoadTask;


public class MyBinaryRequest extends SpiceRequest<InputStream> {
    private static final String TAG="MyBinaryRequest";
    private static final int BUF_SIZE = 4096;
    protected String url;
    private File cacheFile;
    private DownLoadTask task;
    private LocalBroadcastManager manager;
    private HttpURLConnection httpURLConnection;

    private boolean isPaused;
    private boolean isContinue;

    public MyBinaryRequest(final DownLoadTask task,Context context) {
        super(InputStream.class);
        this.task=task;
        this.url = task.getURL();
        this.cacheFile=task.getFile();
        manager=LocalBroadcastManager.getInstance(context);
        isContinue=false;
    }

    //断点续传的构造函数
    public MyBinaryRequest(final DownLoadTask task,Context context,boolean isContinue) {
        this(task,context);
        this.isContinue=isContinue;
    }

    public void Continue()
    {
        this.isContinue=true;
        this.isPaused=false;
    }
    public void Pause(){
        this.isPaused=true;
    }

    @Override
    public final InputStream loadDataFromNetwork() throws Exception {
        Log.d(TAG,"loadDataFromNetwork");
        try {
            httpURLConnection = (HttpURLConnection) new URL(
                    url).openConnection();
            //防止服务器以gzip方式压缩
            //httpURLConnection.setRequestProperty("Accept-Encoding", "identity");
            //if(!isContinue ||task.getWhat2do()!=DownLoadTask.ctlTaskRestart) {
            if(!isContinue ) {
                //如果不是断点续传 表示是新任务 那么就要去获取长度
                getContentLengthandType();
            }
            else {
                long restartPoint=task.getDownLoadedBytes();
                httpURLConnection.setRequestProperty("Range", "bytes=" + restartPoint+"-");
                if(!isHttpUrlConnectedSuccess(httpURLConnection))
                {
                    throw new Exception("network down");
                }
            }

            printResponseHeader();
            InputStream inputStream=httpURLConnection.getInputStream();
            return processStream(inputStream);
        } catch (final MalformedURLException e) {
            Ln.e(e, "Unable to create URL");
            return null;
        } catch (final IOException e) {
            Ln.e(e, "Unable to download binary");
            return null;
        }
    }


    public InputStream processStream(InputStream inputStream)
            throws IOException
    {
        RandomAccessFile randomAccessFile=null;
        try {
            // touch
            boolean isTouchedNow = cacheFile.setLastModified(System
                    .currentTimeMillis());
            if (!isTouchedNow) {
                Ln.d(
                        "Modification time of file %s could not be changed normally ",
                        cacheFile.getAbsolutePath());
            }
            randomAccessFile=new RandomAccessFile(cacheFile,"rwd");
            randomAccessFile.seek(task.getDownLoadedBytes());

            readBytes(inputStream,new MyProgressByteProcessor(this,
                    randomAccessFile, task));
            return new FileInputStream(cacheFile);
        } finally {
            if(randomAccessFile!=null)
                randomAccessFile.close();
        }
    }

    protected void readBytes(final InputStream in,
                             final MyProgressByteProcessor processor) throws IOException {
        task.setState(DownLoadTask.DOWNLOADING);
        notifyDataChanged();
        final byte[] buf = new byte[BUF_SIZE];
        try {
            int amt;
            do {
                amt = in.read(buf);
                //读到文件末尾或者请求被暂停那么结束
                if (amt == -1) {
                    break;
                }
                //if(task.getWhat2do()==DownLoadTask.ctlTaskPause)
                if(isPaused)
                {
                    task.setState(DownLoadTask.PAUSE);
                    break;
                }
            } while (processor.processBytes(buf, 0, amt));
        } finally {
            if(in!=null)
                in.close();
        }
    }
    /*package private*/
    void notifyDataChanged()
    {
        Intent intent=new Intent();
        intent.setAction(GlobalConstants.ACTIONSTRING_BROADCASTRECEIVER);
        Bundle bundle=new Bundle();
        bundle.putSerializable(GlobalConstants.BUNDLEKEY_DOWNLOADTASK,task.getId());
        intent.putExtras(bundle);
        manager.sendBroadcast(intent);
    }


    private boolean isHttpUrlConnectedSuccess(HttpURLConnection httpURLConnection)
    {
        try {
            httpURLConnection.connect();
            if(httpURLConnection.getResponseCode()/100!=2)
            {
                return false;
            }
        } catch (IOException e) {
            Log.e(TAG, "isHttpUrlConnectedSuccess exception" + e);
            return false;
        }
        return true;
    }
    private void getContentLengthandType() throws Exception {
        if(!isHttpUrlConnectedSuccess(httpURLConnection))
        {
            throw new Exception("network down");
        }
        String mimeType = httpURLConnection.getContentType();
        int contentLength = httpURLConnection.getContentLength();
        task.setCanGetContentLength(contentLength >0);
        task.setState(DownLoadTask.DOWNLOADING);
        task.setMimeType(mimeType);
        task.setcontentlength(contentLength);
        notifyDataChanged();
    }

    public static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
        Map<String, String> header = new LinkedHashMap<String, String>();
        for (int i = 0;; i++) {
            String mine = http.getHeaderField(i);
            if (mine == null) break;
            header.put(http.getHeaderFieldKey(i), mine);
        }
        return header;
    }

    public  void printResponseHeader(){
        Log.d(TAG,"printResponseHeader");
        Map<String, String> header = getHttpResponseHeader(httpURLConnection);
        for(Map.Entry<String, String> entry : header.entrySet()){
            String key = entry.getKey()!=null ? entry.getKey()+ ":" : "";
            print(key+ entry.getValue());
        }
    }

    private static void print(String msg){
        Log.i(TAG, msg);
    }


}
