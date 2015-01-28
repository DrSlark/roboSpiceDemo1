package ztt.robospicedownloadtest1.service;


import android.app.Notification;
import android.app.NotificationManager;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import java.util.UUID;

import ztt.robospicedownloadtest1.GlobalConstants;
import ztt.robospicedownloadtest1.MainActivity;
import ztt.robospicedownloadtest1.R;
import ztt.robospicedownloadtest1.model.DownLoadTask;
import ztt.robospicedownloadtest1.model.DownLoadTaskList;

public  class NotificationService extends Service {
    private static final int DEFAULT_ROBOSPICE_NOTIFICATION_ID = 70;
    private LocalBroadcastManager manager;


    private int notificationId = DEFAULT_ROBOSPICE_NOTIFICATION_ID;

    private NotificationManager notificationManager;
    private DownLoadTaskList Tasks;
    private NotificationReceiver receiver;
    private UUID who;


    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes", "deprecation"})
    @Override
    public final void onStart(final Intent intent, final int startId) {
        super.onStart(intent, startId);
        manager = LocalBroadcastManager.getInstance(this);
        receiver = new NotificationReceiver();
        manager.registerReceiver(receiver, new IntentFilter(GlobalConstants.ACTIONSTRING_BROADCASTRECEIVER));
        notificationId = intent.getExtras().getInt(GlobalConstants.BUNDLEKEY_NOTIFICATION_ID,
                DEFAULT_ROBOSPICE_NOTIFICATION_ID);
        who= (UUID) intent.getExtras().getSerializable(GlobalConstants.BUNDLEKEY_DOWNLOADTASK);
        Tasks = DownLoadTaskList.getInstance(this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    @Override
    public void onDestroy() {
        manager.unregisterReceiver(receiver);
        super.onDestroy();
    }

    private Notification buildNotificationByTask(DownLoadTask task)
    {
        Notification notification=null;

        if(task.getState()==DownLoadTask.DOWNLOADING||task.getState()==DownLoadTask.START||task.getState()==DownLoadTask.PAUSE)
        {
             String message=(task.getState()==DownLoadTask.PAUSE?"pause":"downloading");
             Notification.Builder builder= new Notification.Builder(this)
                     .setSmallIcon(R.drawable.robospice_logo)
                     .setContentTitle(task.getTaskName())
                     .setContentText(message)
                     .setTicker(null);
            //不能获取请求内容长度的话那么进度条设置为无限进度条
            if(!task.isCanGetContentLength())
            {
                builder.setProgress(100,0,true);
            }
            else
            {
                builder.setProgress(100,(int)(task.getDownLoadProgress()*100),false);
            }
            notification=builder.build();
            notification.priority = Notification.PRIORITY_MIN;
        }
        if(task.getState()==DownLoadTask.ACCOMPLISH||task.getState()==DownLoadTask.FAILURE)
        {
            String message=(task.getState()==DownLoadTask.ACCOMPLISH?"download success":"download failed");
            notification = new Notification.Builder(this)
                    .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.robospice_logo))
                    .setContentTitle(task.getTaskName())
                    .setProgress(100,100,false)
                    .setContentText(message)
                    .build();
            notification.priority = Notification.PRIORITY_MIN;
        }
        Intent intent=new Intent(this, MainActivity.class);
        Bundle data=new Bundle();
        data.putSerializable(GlobalConstants.BUNDLEKEY_NOTIFICATION2ACTIVITY_UUID,task.getId());
        intent.putExtras(data);
        if(notification!=null) {
            notification.contentIntent =
                    PendingIntent.getActivity(
                            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT, data);
        }
        return notification;
    }
    private class NotificationReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            UUID id= (UUID) intent.getExtras().get(GlobalConstants.BUNDLEKEY_DOWNLOADTASK);
            if(!who.equals(id))
            {
                return;
            }
            DownLoadTask task=Tasks.findTaskByID(id);
            if(task==null)
            {
                stopSelf();
                return;
            }
            final  Notification notification=buildNotificationByTask(task);
            notificationManager.notify(notificationId,notification);
            if(task.getState()==DownLoadTask.ACCOMPLISH||task.getState()==DownLoadTask.FAILURE)
            {
                stopSelf();
            }
        }
    }

}
