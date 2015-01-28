package ztt.robospicedownloadtest1.model;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import ztt.robospicedownloadtest1.Utils;


public class DownLoadTask implements Serializable{

    private static final String TAG = "DownLoadTask";

    private static final String JSON_ID = "id";
    private static final String JSON_TASKNAME = "taskname";
    private static final String JSON_URL = "url";
    private static final String JSON_CONTENTLENGTH = "contentlength";
    private static final String JSON_STARTTIME = "starttime";
    private static final String JSON_FILE = "file";
    private static final String JSON_STATE = "state";
    private static final String JSON_DOWNLOADED_LENGTH="downloadbytes";
    private static final String JSON_MIMETYPE="mimettype";
    private static final String JSON_DOWNLOADED_PROGRESS="downloadprogress";
    private static final String JSON_CANGETCONTENTLENGTH="cangetcontentlength";
    /**
     * Status
     */

    public static final int PAUSE = 0x0001;
    public static final int CANCEL = 0x0002;
    public static final int ACCOMPLISH = 0x0003;
    public static final int FAILURE = 0x0004;
    public static final int START = 0x0005;
    public static final int INTERRUPTED=0x0006;
    public static final int WAITING=0x0007;
    public static final int DOWNLOADING = 0x0008;

    /**
     * 控制将要做的事情是暂停还是恢复
     * */

    public static final int ctlTaskPause=0x0010;
    public static final int ctlTaskRestart=0x0011;

     //默认的下载目录
    public static String sAppRootDirName;


    private UUID id;
    private String mTaskName;
    private String mURL;
    //表示是否能正确得到服务器返回的内容的长度
    private boolean canGetContentLength;
    // Bit
    private long mContentLength;
    private long mDownLoadedLength;

    private float mDownLoadProgress;

    private Date mStartTime;

    private File mFile;

    private String mimeType;

    //state
    private int mState;
    //

    private int what2do;

    public DownLoadTask() {
        id = UUID.randomUUID();
        mStartTime = new Date();
    }

    public DownLoadTask(JSONObject jsonObject) throws JSONException {
        this.id = UUID.fromString(jsonObject.getString(JSON_ID));
        this.mTaskName = jsonObject.getString(JSON_TASKNAME);
        this.mURL = jsonObject.getString(JSON_URL);
        this.mFile = new File(jsonObject.getString(JSON_FILE));
        if(this.mFile.exists())
        {
            this.mDownLoadedLength=this.mFile.length();
        }
        else
        {
            this.mDownLoadedLength=0;
        }
        this.mState = jsonObject.getInt(JSON_STATE);
        this.mContentLength = jsonObject.getLong(JSON_CONTENTLENGTH);
        //this.mDownLoadedLength=jsonObject.getLong(JSON_DOWNLOADED_LENGTH);
        this.mimeType=jsonObject.getString(JSON_MIMETYPE);
        this.mDownLoadProgress=(float)this.mDownLoadedLength/this.mContentLength;
        this.canGetContentLength=jsonObject.getBoolean(JSON_CANGETCONTENTLENGTH);
        if (jsonObject.has(JSON_STARTTIME))
            this.mStartTime = new Date(jsonObject.getLong(JSON_STARTTIME));
    }
    public File getFile() {
        return mFile;
    }

    public void setFile(File mFile) {
        this.mFile = mFile;
    }

    public int getState() {
        return mState;
    }

    public void setState(int mState) {
        this.mState = mState;
    }

    public UUID getId() {
        return id;
    }

    public String getURL() {
        return mURL;
    }

    public void setURL(String mURL) {
        this.mURL = mURL;
    }

    public String getTaskName() {
        return mTaskName;
    }

    public void setTaskName(String mTaskName) {
        this.mTaskName = mTaskName;
    }

    public long getcontentlength() {
        return mContentLength;
    }

    public void setcontentlength(long mContentLength) {
        this.mContentLength = mContentLength;
    }

    public Date getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Date mStartTime) {
        this.mStartTime = mStartTime;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public boolean isCanGetContentLength() {
        return canGetContentLength;
    }

    public void setCanGetContentLength(boolean canGetContentLength) {
        this.canGetContentLength = canGetContentLength;
    }

    public long getDownLoadedBytes() {
        return mDownLoadedLength;
    }

    public void setDownLoadedBytes(long mDownLoadedLength) {
        this.mDownLoadedLength = mDownLoadedLength;
    }

    public float getDownLoadProgress() {
        return mDownLoadProgress;
    }

    public void setDownLoadProgress(float downLoadProgress) {
        mDownLoadProgress = downLoadProgress;
    }
    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_ID, this.id);
        jsonObject.put(JSON_TASKNAME, this.mTaskName);
        jsonObject.put(JSON_STARTTIME, this.mStartTime.getTime());
        jsonObject.put(JSON_URL, this.mURL);
        jsonObject.put(JSON_FILE, this.mFile.getAbsolutePath());
        jsonObject.put(JSON_CONTENTLENGTH, this.mContentLength);
        jsonObject.put(JSON_STATE,this.mState);
        jsonObject.put(JSON_DOWNLOADED_LENGTH,this.mDownLoadedLength);
        jsonObject.put(JSON_DOWNLOADED_PROGRESS,this.mDownLoadProgress);
        jsonObject.put(JSON_MIMETYPE,this.mimeType);
        jsonObject.put(JSON_CANGETCONTENTLENGTH,this.canGetContentLength);
        Log.d(TAG, jsonObject.toString());
        return jsonObject;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof DownLoadTask
                && (this.getURL().equals(((DownLoadTask) o).getURL())
                ||this.getId().equals(((DownLoadTask) o).getId()));
    }

    public void resetData() {

        this.mStartTime=new Date();
        this.mDownLoadedLength=0;
        this.mDownLoadProgress=0;
        this.mContentLength=0;
        this.mimeType=null;
        this.mState=START;
        this.mFile= Utils.makeEmptyFile(sAppRootDirName,this.mTaskName);
    }

    public int getWhat2do() {
        return what2do;
    }

    public void setWhat2do(int what2do) {
        this.what2do = what2do;
    }
}
