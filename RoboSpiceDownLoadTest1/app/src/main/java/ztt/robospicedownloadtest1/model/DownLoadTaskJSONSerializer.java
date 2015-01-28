package ztt.robospicedownloadtest1.model;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by 123 on 2015/1/1.
 */
public class DownLoadTaskJSONSerializer implements DownLoadTaskSerializer{

    private final String TAG="DownLoadTaskJSONSerializer";
    private Context mContext;
    private String mFileName;

    public DownLoadTaskJSONSerializer(Context mContext, String mFileName) {
        this.mContext = mContext;
        this.mFileName = mFileName;
    }

    @Override
    public void saveTasks(List<DownLoadTask> tasks) throws Exception{
        JSONArray array=new JSONArray();
        for(DownLoadTask t:tasks)
        {
           array.put(t.toJSON());
        }
        Log.d(TAG,"saveTasks"+array.toString());
        Writer writer=null;
        try {
            OutputStream outputStream=mContext.openFileOutput(mFileName,Context.MODE_PRIVATE);
            writer=new OutputStreamWriter(outputStream);
            writer.write(array.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        finally {
            if(writer!=null)
                writer.close();
        }
    }

    @Override
    public List<DownLoadTask> loadTasks() throws Exception  {
        ArrayList<DownLoadTask> tasks=new ArrayList<DownLoadTask>();
        BufferedReader reader=null;
        try {
            InputStream in=mContext.openFileInput(mFileName);
            reader=new BufferedReader(new InputStreamReader(in));
            StringBuilder jsonString=new StringBuilder();
            String line=null;
            while ((line=reader.readLine())!=null)
                jsonString.append(line);
            JSONArray array=(JSONArray)new JSONTokener(jsonString.toString()).nextValue();
            for(int i=0;i<array.length();i++)
            {
                DownLoadTask temp=new DownLoadTask(array.getJSONObject(i));
                Log.v(TAG,temp.toString());
                tasks.add(temp);
            }
        }
        catch (FileNotFoundException ignored) {
        }
        finally {
            if(reader!=null)
                reader.close();
        }
        return  tasks;
    }

}
