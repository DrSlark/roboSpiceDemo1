package ztt.robospicedownloadtest1.request;

import android.content.Context;

import com.octo.android.robospice.request.SpiceRequest;


import java.util.List;

import ztt.robospicedownloadtest1.model.DownLoadTask;
import ztt.robospicedownloadtest1.model.DownLoadTaskList;

/**
 * Created by 123 on 2015/1/19.
 */
public class MyDeleteFileRequest extends SpiceRequest<Boolean> {
    List<DownLoadTask> tasks;
    Context mContext;
    public MyDeleteFileRequest(List<DownLoadTask> tasks,Context context) {
        super(Boolean.class);
        this.tasks=tasks;
        mContext=context;
    }

    //在这里做删除
    @Override
    public Boolean loadDataFromNetwork() throws Exception {
        boolean result=true;
        for(DownLoadTask task:tasks)
        {
            if(!task.getFile().delete())
            {
                result=false;
            }
        }
        return result;
    }
}
