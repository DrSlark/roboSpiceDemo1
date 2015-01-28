package ztt.robospicedownloadtest1.adapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.octo.android.robospice.SpiceManager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ztt.robospicedownloadtest1.GlobalConstants;

import ztt.robospicedownloadtest1.IntentBuilder;
import ztt.robospicedownloadtest1.R;
import ztt.robospicedownloadtest1.Utils;
import ztt.robospicedownloadtest1.listener.BigBinaryRequestListener;
import ztt.robospicedownloadtest1.model.DownLoadTask;
import ztt.robospicedownloadtest1.model.DownLoadTaskList;
import ztt.robospicedownloadtest1.request.MyBinaryRequest;

import ztt.robospicedownloadtest1.service.NotificationService;


public class TaskListAdapter extends ArrayAdapter<DownLoadTask> {
    private static final String TAG = "TaskListAdapter";

    private Context mContext;
    private LayoutInflater inflater;
    private SpiceManager spiceManager;

    private Map<String,MyBinaryRequest> Task2Requests;//保存我的所有请求,便于操作下载

    private ListView listView;
    private List<DownLoadTask> list;
    private DownLoadTaskList Tasks;

    private myReceiver receiver = new myReceiver();

    static class ViewHolder
    {
        ProgressBar task_progress;
        TextView task_name;
        TextView task_total_size;
        TextView task_per;
        Button btn_ctl_state;
    }
    public Map<String,MyBinaryRequest> getTaskRequestMapping()
    {
        return Task2Requests;
    }

    public TaskListAdapter(Context context, int resource, DownLoadTaskList objects, SpiceManager spiceManager) {
        super(context, resource, objects.getTasks());
        mContext = context;
        inflater = LayoutInflater.from(context);
        this.list=objects.getTasks();
        this.spiceManager = spiceManager;
        this.Tasks=objects;
        this.Task2Requests=new HashMap<>();
    }

    public void setListView(ListView listView_task)
    {
        this.listView=listView_task;
    }
    public BroadcastReceiver getReceiver()
    {
        return receiver;
    }

    private void initWidget(ViewHolder holder,View convertView) {
        holder.task_progress = (ProgressBar) convertView.findViewById(R.id.progressbar_download);
        holder.task_name = (TextView) convertView.findViewById(R.id.textview_task_name);
        holder.task_total_size = (TextView) convertView.findViewById(R.id.textview_task_total_size);
        holder.task_per = (TextView) convertView.findViewById(R.id.textview_task_percent);
        holder.btn_ctl_state = (Button) convertView.findViewById(R.id.btn_ctl_state);
        convertView.setTag(holder);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.v(TAG,"getView");
        ViewHolder holder;
        final DownLoadTask task = getItem(position);
        if (convertView == null) {
            Log.v(TAG,"convertView == null");
            holder= new ViewHolder();
            convertView = inflater.inflate(R.layout.downloading_task_list_item, parent, false);
            initWidget(holder,convertView);
        }
        else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.task_name.setText(task.getTaskName());
        if(task.getFile().exists())
            executeDownLoadTask(task, holder);
        else
        {
            holder.task_total_size.setText(mContext.getString(R.string.filenotexist));
            holder.btn_ctl_state.setText(mContext.getString(R.string.restart));
            holder.btn_ctl_state.setOnClickListener(new reDownLoadListener(task));
        }
        return convertView;
    }

    private void taskStartAction(DownLoadTask task)
    {
        BigBinaryRequestListener bigBinaryRequestListener =
                new BigBinaryRequestListener(mContext, task);
        MyBinaryRequest request = new MyBinaryRequest(task, mContext);
        Task2Requests.put(task.getURL(),request);
        spiceManager.execute(request, bigBinaryRequestListener);

        startNotificationService(task);
    }

    private void executeDownLoadTask(final DownLoadTask task, final ViewHolder holder) {
        Log.d(TAG,"executeDownLoadTask");
        switch (task.getState()) {
            case DownLoadTask.START:
                holder.btn_ctl_state.setText(mContext.getString(R.string.pause));
                taskStartAction(task);
                break;
            case DownLoadTask.DOWNLOADING:
                //正在进行的下载队列中没有这个任务的request表示他是下载中突然中断那么就转入Pause的状态
                if(Task2Requests.get(task.getURL())==null)
                {
                    Log.v(TAG,"task.getURL())==null");
                    if(task.getFile().exists()) {
                        task.setState(DownLoadTask.PAUSE);
                        startNotificationService(task);
                    }
                    else {
                        task.resetData();
                        task.setState(DownLoadTask.INTERRUPTED);
                    }
                    executeDownLoadTask(task,holder);
                    break;
                }
                holder.btn_ctl_state.setText(mContext.getString(R.string.pause));
                spiceManager.addListenerIfPending(InputStream.class,task.getURL()
                        ,new BigBinaryRequestListener(mContext,task));
                holder.btn_ctl_state.setOnClickListener(
                        new Downloading2PauseButtonListener(holder,task));
                break;
            case DownLoadTask.PAUSE:
                holder.btn_ctl_state.setText(mContext.getString(R.string.restart));
                holder.task_progress.setProgress((int)(task.getDownLoadProgress()*100));
                holder.task_per.setText(Integer.toString((int) (task.getDownLoadProgress() * 100)) + "%");
                holder.task_total_size.setText(Utils.convertStorage(task.getcontentlength()));
                holder.btn_ctl_state.setOnClickListener(
                        new Pause2StartButtonListener(holder,task));
                break;
            case DownLoadTask.INTERRUPTED:
                holder.task_total_size.setText(mContext.getString(R.string.filenotexist));
                holder.task_progress.setVisibility(View.GONE);
                holder.task_per.setVisibility(View.GONE);
                holder.btn_ctl_state.setText(mContext.getString(R.string.restart));
                holder.btn_ctl_state.setOnClickListener(new reDownLoadListener(task));
                break;
            case DownLoadTask.ACCOMPLISH:
                holder.task_per.setText("100%");
                holder.task_progress.setProgress(holder.task_progress.getMax());
                holder.task_total_size.setText(Utils.convertStorage(task.getcontentlength()));
                holder.btn_ctl_state.setText(mContext.getString(R.string.openfile));
                holder.btn_ctl_state.setOnClickListener(new openFileListener(task));
                Task2Requests.remove(task.getURL());
                break;
            case DownLoadTask.FAILURE:
                holder.task_per.setText(mContext.getString(R.string.failure));
                holder.task_progress.setProgress((int)(task.getDownLoadProgress()*100));
                holder.btn_ctl_state.setText(mContext.getString(R.string.restart));
                holder.btn_ctl_state.setOnClickListener(
                        new Pause2StartButtonListener(holder,task));
                break;
        }
    }

    private void startNotificationService(DownLoadTask task)
    {
        Intent intent=new Intent(mContext,NotificationService.class);
        Bundle bundle=new Bundle();
        bundle.putSerializable(GlobalConstants.BUNDLEKEY_DOWNLOADTASK,task.getId());
        bundle.putInt(GlobalConstants.BUNDLEKEY_NOTIFICATION_ID,
                Tasks.findTaskPositionByID(task.getId()));
        intent.putExtras(bundle);
        mContext.startService(intent);
    }

    private void updateView(UUID id)
    {
        DownLoadTask task=Tasks.findTaskByID(id);
        int index=list.indexOf(task);
        int firstVisiblePos=listView.getFirstVisiblePosition();
        int offset=index-firstVisiblePos;
        //在可见的位置的上方那么不用更新
        if(offset<0) return ;

        View view=listView.getChildAt(offset);
        ViewHolder holder= (ViewHolder) view.getTag();
        if(task.getState()==DownLoadTask.FAILURE)
        {
            holder.task_per.setText(mContext.getString(R.string.failure));
            holder.task_progress.setProgress((int) (task.getDownLoadProgress() * 100));
            holder.btn_ctl_state.setText(mContext.getString(R.string.restart));
            holder.btn_ctl_state.setOnClickListener(
                    new Pause2StartButtonListener(holder,task));
        }

        if(task.getState()==DownLoadTask.PAUSE)
        {
            holder.btn_ctl_state.setText(mContext.getString(R.string.restart));
            holder.btn_ctl_state.setOnClickListener(
                    new Pause2StartButtonListener(holder,task));
        }
        if(task.getState()==DownLoadTask.ACCOMPLISH)
        {
            holder.task_per.setText("100%");
            holder.task_progress.setIndeterminate(false);
            holder.task_progress.setProgress(holder.task_progress.getMax());
            holder.task_total_size.setText(Utils.convertStorage(task.getcontentlength()));
            holder.btn_ctl_state.setText(mContext.getString(R.string.openfile));
            holder.btn_ctl_state.setOnClickListener(new openFileListener(task));
        }
        if(task.getState()==DownLoadTask.DOWNLOADING)
        {
            //如果能得到资源长度那么显示百分比进度
            if(task.isCanGetContentLength()) {
                holder.task_per.setText(Integer.toString((int) (task.getDownLoadProgress() * 100)) + "%");
                holder.task_progress.setProgress((int) (task.getDownLoadProgress() * holder.task_progress.getMax()));
                holder.task_total_size.setText(Utils.convertStorage(task.getcontentlength()));
            }
            //如果不能得到那么进度百分比那里显示下载的字节数 总长度那显示UNknown size
            else {
                holder.task_per.setText(Utils.convertStorage(task.getDownLoadedBytes()));
                holder.task_progress.setIndeterminate(true);
                holder.task_total_size.setText(Utils.convertStorage(task.getcontentlength()));
            }
            holder.btn_ctl_state.setText(mContext.getString(R.string.pause));
            holder.btn_ctl_state.setOnClickListener(new Downloading2PauseButtonListener(holder,task));
        }
    }

    private  class myReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            UUID id= (UUID) intent.getExtras().get(GlobalConstants.BUNDLEKEY_DOWNLOADTASK);
            if(mContext!=null)
                updateView(id);
        }
    }

    //任务在下载状态下button点击之后暂停
    private class Downloading2PauseButtonListener implements View.OnClickListener{
        ViewHolder holder;
        DownLoadTask task;
        Downloading2PauseButtonListener(ViewHolder viewHolder,DownLoadTask task)
        {
            holder=viewHolder;
            this.task=task;
        }
        @Override
        public void onClick(View v) {
            MyBinaryRequest request2=Task2Requests.get(task.getURL());
            request2.Pause();
            //task.setWhat2do(DownLoadTask.ctlTaskPause);
            holder.btn_ctl_state.setText(mContext.getString(R.string.pausing));
        }
    }
    //任务在暂停状态下button点击之后进行断点续传
    private class Pause2StartButtonListener implements View.OnClickListener{
        ViewHolder holder;
        DownLoadTask task;
        Pause2StartButtonListener(ViewHolder viewHolder,DownLoadTask task)
        {
            holder=viewHolder;
            this.task=task;
        }
        @Override
        public void onClick(View v) {
            holder.btn_ctl_state.setText(mContext.getString(R.string.starting));
            holder.btn_ctl_state.setClickable(false);
            MyBinaryRequest request1=Task2Requests.get(task.getURL());
            //request1.Pause();
            if(request1!=null)
                request1.Continue();
            else
            {
                request1=new MyBinaryRequest(task,mContext,true);
                Task2Requests.put(task.getURL(),request1);
            }
           // task.setWhat2do(DownLoadTask.ctlTaskRestart);
           // MyBinaryRequest request1=new MyBinaryRequest(task,mContext,true);
            spiceManager.execute(request1, new BigBinaryRequestListener(mContext,task));
        }
    }

    private class openFileListener implements View.OnClickListener
    {
        DownLoadTask task;
        openFileListener(DownLoadTask task)
        {
            this.task=task;
        }

        @Override
        public void onClick(View v) {
            IntentBuilder.viewFile(mContext,task.getFile().getPath());
        }
    }

    private class reDownLoadListener implements View.OnClickListener{

        private  DownLoadTask task;
        reDownLoadListener(DownLoadTask task) {
            this.task=task;
        }

        @Override
        public void onClick(View v) {
            TaskListAdapter.this.taskStartAction(task);
        }
    }
}
