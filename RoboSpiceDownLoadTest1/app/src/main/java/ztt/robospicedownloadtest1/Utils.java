package ztt.robospicedownloadtest1;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import ztt.robospicedownloadtest1.model.DownLoadTask;
import ztt.robospicedownloadtest1.model.DownLoadTaskList;


public class Utils {

    private static final String TAG = "Utils";

    public static String getSdDirectory() {
        return Environment.getExternalStorageDirectory().getPath();
    }

    public static String makePath(String path1, String path2) {
        // path1必须是dir
        // path1是xxx/格式
        if (path1.endsWith(File.separator))
            return path1 + path2;
        // path1是xxx格式，加上文件分隔符
        return path1 + File.separator + path2;
    }

    public static boolean isSDCardReady() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }

    public static class SDCardInfo {
        public long total;

        public long free;
    }

    @SuppressWarnings("deprecation")
    public static SDCardInfo getSDCardInfo() {
        String sDcString = Environment.getExternalStorageState();

        if (sDcString.equals(Environment.MEDIA_MOUNTED)) {
            // sd卡已挂载
            File pathFile = Environment
                    .getExternalStorageDirectory();

            try {
                android.os.StatFs statfs = new android.os.StatFs(
                        pathFile.getPath());


                // 获取SDCard上BLOCK总数
                long nTotalBlocks = statfs.getBlockCount();

                // 获取SDCard上每个block的SIZE
                long nBlocSize = statfs.getBlockSize();

                // 获取可供程序使用的Block的数量
                long nAvailaBlock = statfs.getAvailableBlocks();

                // 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
                long nFreeBlock = statfs.getFreeBlocks();

                SDCardInfo info = new SDCardInfo();

                // 计算SDCard 总容量大小MB
                info.total = nTotalBlocks * nBlocSize;

                // 计算 SDCard 剩余大小MB
                info.free = nAvailaBlock * nBlocSize;

                return info;
            } catch (IllegalArgumentException e) {
                Log.e(TAG, e.toString());
            }
        }
        return null;
    }

    public static void copyFile(InputStream is,String destPath)
    {
        FileOutputStream fo = null;
        try {
            File destFile = new File(destPath);
            // 创建文件
            if(!destFile.exists()) {
                if (!destFile.createNewFile())
                    Log.e(TAG,"createNewFile failure"+destPath);
                   // Toast.makeText(MainActivity.this, "createNewFile failure", Toast.LENGTH_SHORT).show();
            }
            else
            {
                for(int i=0;i< 10000;i++)
                {
                    destPath=destPath+i;
                    destFile=new File(destPath);
                    if(!destFile.exists())
                    {
                        if (!destFile.createNewFile())
                            Log.e(TAG,"createNewFile failure"+destPath);
                        break;
                    }
                }
            }
            fo = new FileOutputStream(destFile);
            int count = 102400; // 100K
            byte[] buffer = new byte[count];
            int read = 0;
            while ((read = is.read(buffer, 0, count)) != -1) {
                fo.write(buffer, 0, read);
            }

        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyFile: file not found, " );
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "copyFile: " + e.toString());
        } finally {
            try {
                if (is != null)
                    is.close();
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public static String copyFile(String src, String dest) {
        File file = new File(src);
        if (!file.exists() || file.isDirectory()) {
            Log.v(TAG, "copyFile: file not exist or is directory, " + src);
            return null;
        }
        FileInputStream fi = null;
        FileOutputStream fo = null;
        try {
            fi = new FileInputStream(file);
            File destPlace = new File(dest);
            // 如果目录不存在，用mkdirs创建相应的目录
            if (!destPlace.exists()) {
                if (!destPlace.mkdirs())
                    return null;
            }
            // 路径+文件名
            String destPath = Utils.makePath(dest, file.getName()+"copy");
            File destFile = new File(destPath);
            // 创建文件
            if (!destFile.createNewFile())
                return null;
            // io
            fo = new FileOutputStream(destFile);
            int count = 102400; // 100K
            byte[] buffer = new byte[count];
            int read = 0;
            while ((read = fi.read(buffer, 0, count)) != -1) {
                fo.write(buffer, 0, read);
            }
            return destPath;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "copyFile: file not found, " + src);
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "copyFile: " + e.toString());
        } finally {
            try {
                if (fi != null)
                    fi.close();
                if (fo != null)
                    fo.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // storage, G M K B
    public static String convertStorage(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if(size==-1)
        {
            return "UnKnown Size";
        }
        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        } else
            return String.format("%d B", size);
    }

    public static File makeEmptyFile(String FileDir,String FileName)
    {
        File requestFile = new File(FileDir, FileName);
        try {
            if(!requestFile.exists()) {
                if (!requestFile.createNewFile()) {
                    Log.v(TAG,"mkFile success"+requestFile.getName());
                }
            }
        } catch (IOException e) {
            Log.e(TAG,e.toString());
        }
        return requestFile;
    }

    public static String getFileName(DownLoadTask task) {
        String filename = task.getURL().substring(task.getURL().lastIndexOf('/') + 1);
        if("".equals(filename.trim())){//如果获取不到文件名称
            filename = task.getId().toString();
        }
        return filename;
    }

    public static String convertState2String(int State)
    {
        switch (State)
        {
            case DownLoadTask.ACCOMPLISH:return "Accomplish";
            case DownLoadTask.CANCEL:return "Canceled";
            case DownLoadTask.FAILURE:return "Failure";
            case DownLoadTask.START:return "Start";
            case DownLoadTask.PAUSE:return "Pause";
            case DownLoadTask.DOWNLOADING:return "Downloading";
            case DownLoadTask.INTERRUPTED:return "Interrupted";
            default:return "Unknown";
        }

    }
    public static boolean isHttpUrl(String url)
    {
        try {
            URL u=new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static void changeStateFromDownloading2Interrupted(DownLoadTaskList taskList)
    {
        Log.v("DownLoadTask","changeStateFromDownloading2Interrupted");
        for(DownLoadTask t:taskList.getTasks())
        {
            if(t.getState()==DownLoadTask.DOWNLOADING)
            {
                t.setState(DownLoadTask.INTERRUPTED);
            }
        }
    }


}
