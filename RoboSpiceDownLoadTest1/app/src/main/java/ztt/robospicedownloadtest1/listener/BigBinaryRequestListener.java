package ztt.robospicedownloadtest1.listener;

import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.listener.RequestProgress;
import com.octo.android.robospice.request.listener.RequestProgressListener;

import java.io.InputStream;

import ztt.robospicedownloadtest1.GlobalConstants;
import ztt.robospicedownloadtest1.model.DownLoadTask;


public class BigBinaryRequestListener implements
        PendingRequestListener<InputStream>
{
    private static final String TAG="BigBinaryRequestListener";
    private Context mContext;
    private LocalBroadcastManager manager ;
    private DownLoadTask task;

    public BigBinaryRequestListener(Context mContext,DownLoadTask task) {

        this.mContext = mContext;
        this.task=task;
        manager=LocalBroadcastManager.getInstance(mContext);
    }

    @Override
    public void onRequestNotFound() {


    }
    @Override
    public void onRequestFailure(SpiceException e) {
        Toast.makeText(mContext,"download failure "+e,Toast.LENGTH_SHORT).show();
        task.setState(DownLoadTask.FAILURE);
        notifyDataChanged();
    }
    @Override
    public void onRequestSuccess(InputStream fi) {
        //如果不是pause状态
        //if(task.getState()!=DownLoadTask.PAUSE&&task.getState()!=DownLoadTask.DOWNLOADING)
        if(task.getState()!=DownLoadTask.PAUSE) {
            task.setState(DownLoadTask.ACCOMPLISH);
            if (!task.isCanGetContentLength()) {
                task.setcontentlength(task.getDownLoadedBytes());
            } else {
                task.setDownLoadedBytes(task.getcontentlength());
            }
            //Toast.makeText(mContext,"mission is completed",Toast.LENGTH_SHORT).show();

        }
       /* else {
           Toast.makeText(mContext,"mission is paused",Toast.LENGTH_SHORT).show();
        }*/
        notifyDataChanged();
    }

    private void notifyDataChanged()
    {
        Intent intent=new Intent();
        intent.setAction(GlobalConstants.ACTIONSTRING_BROADCASTRECEIVER);
        Bundle bundle=new Bundle();
        bundle.putSerializable(GlobalConstants.BUNDLEKEY_DOWNLOADTASK,task.getId());
        intent.putExtras(bundle);
        manager.sendBroadcast(intent);
    }
}
