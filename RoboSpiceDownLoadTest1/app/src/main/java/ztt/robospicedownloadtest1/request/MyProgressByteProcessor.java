package ztt.robospicedownloadtest1.request;

import java.io.IOException;
import java.io.RandomAccessFile;

import ztt.robospicedownloadtest1.model.DownLoadTask;


public class MyProgressByteProcessor {
    private final RandomAccessFile raf;
    //初始化为已经task.getDownLoadedBytes()的值;
    private long progress;

    private MyBinaryRequest request;
    private DownLoadTask task;


    public MyProgressByteProcessor(MyBinaryRequest request, final RandomAccessFile raf,
                                  DownLoadTask task) {
        this.raf = raf;
        this.request = request;
        this.task=task;
        progress=task.getDownLoadedBytes();
    }


    public boolean processBytes(final byte[] buffer, final int offset, final int length) throws IOException {

        raf.write(buffer, offset, length);
        progress += length - offset;
        task.setState(DownLoadTask.DOWNLOADING);
        task.setDownLoadedBytes(progress);
        task.setDownLoadProgress((float) progress / task.getcontentlength());
       // request.updateProgress((float) progress / task.getcontentlength());

        request.notifyDataChanged();

        return !Thread.interrupted();
    }
}
