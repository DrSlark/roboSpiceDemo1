package ztt.robospicedownloadtest1.model;

import java.util.List;

/**
 * Created by 123 on 2015/1/1.
 */
public interface DownLoadTaskSerializer {

    public void saveTasks(List<DownLoadTask> tasks) throws Exception;
    public List<DownLoadTask> loadTasks() throws Exception;
}
