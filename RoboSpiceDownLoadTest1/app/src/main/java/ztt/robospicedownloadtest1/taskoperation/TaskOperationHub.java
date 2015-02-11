package ztt.robospicedownloadtest1.taskoperation;

import android.content.Context;
import android.content.Intent;

import com.octo.android.robospice.SpiceManager;

import java.util.List;

import ztt.robospicedownloadtest1.IntentBuilder;
import ztt.robospicedownloadtest1.listener.DeleteListener;
import ztt.robospicedownloadtest1.model.DownLoadTask;
import ztt.robospicedownloadtest1.model.DownLoadTaskList;
import ztt.robospicedownloadtest1.request.MyDeleteFileRequest;

/**
 * Created by 123 on 2015/1/19.
 */
public class TaskOperationHub {
    private Context mContext;

    public TaskOperationHub(Context mContext) {

        this.mContext = mContext;
    }

    public void DeleteTasksWithoutFile(List<DownLoadTask> tasks)
    {
        for(DownLoadTask task:tasks)
        {
            DownLoadTaskList.getInstance(mContext).deleteTask(task);
        }
    }
    public void DeleteTasksWithFile(SpiceManager spiceManager,List<DownLoadTask> tasks)
    {
        MyDeleteFileRequest request=new MyDeleteFileRequest(tasks,mContext);
        DeleteListener listener=new DeleteListener(mContext);
        spiceManager.execute(request,listener);
    }

    public void ShareResult(List<DownLoadTask> tasks)
    {
        Intent intent=IntentBuilder.buildSendFile(tasks);
        mContext.startActivity(intent);
    }


}
