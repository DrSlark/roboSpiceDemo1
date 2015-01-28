package ztt.robospicedownloadtest1.model;

import android.content.Context;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import ztt.robospicedownloadtest1.GlobalConstants;


public class DownLoadTaskList implements Serializable{
    private static final String TAG="DownLoadTaskList";

    private Context mAppContext;
    List<DownLoadTask> tasks;
    private static DownLoadTaskList sDownLoadTaskList;
    private DownLoadTaskSerializer mSerializer;


    private DownLoadTaskList(Context context) {

        mAppContext=context;
        mSerializer=new DownLoadTaskJSONSerializer(mAppContext, GlobalConstants.JSON_FILENAME);
        try {
            tasks=mSerializer.loadTasks();
            Log.d(TAG,"loading success");
        } catch (Exception e) {
            tasks=new ArrayList<>();
            Log.e(TAG," error load tasks"+e);
        }

    }


    public static  DownLoadTaskList getInstance(Context context) {
        if (sDownLoadTaskList == null)
            sDownLoadTaskList = new DownLoadTaskList(context.getApplicationContext());
        return sDownLoadTaskList;
    }

    public boolean containTask(String url) {
        for (DownLoadTask t : tasks) {
            if (t.getURL().equals(url))
                return true;
        }
        return false;

    }

    public DownLoadTask findTaskByID(UUID uuid)
    {
        for(DownLoadTask t:tasks)
        {
            if (t.getId().equals(uuid))
                return  t;
        }
        return null;
    }
    public int findTaskPositionByID(UUID id)
    {
        int pos=0;
        for(DownLoadTask t:tasks)
        {
            if(t.getId().equals(id))
            {
                return pos;
            }
            pos++;
        }
        return pos;

    }
    public void addTask(DownLoadTask task) {
        tasks.add(task);
    }

    public void deleteTask(DownLoadTask task)
    {
        tasks.remove(task);
    }

    public List<DownLoadTask> getTasks() {
        return tasks;
    }

    public int getSize()
    {
        return tasks.size();
    }


    public boolean saveTasks()
    {
        try {
            mSerializer.saveTasks(tasks);
            Log.d(TAG,"tasks save success");
            return true;
        } catch (Exception e) {
            Log.e(TAG,"error save tasks"+e);
            return false;
        }
    }


}
