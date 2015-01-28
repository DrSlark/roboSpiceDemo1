package ztt.robospicedownloadtest1;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import android.widget.Toast;

import com.octo.android.robospice.SpiceManager;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ztt.robospicedownloadtest1.adapter.TaskListAdapter;
import ztt.robospicedownloadtest1.dialog.InformationDialog;
import ztt.robospicedownloadtest1.model.DownLoadTask;
import ztt.robospicedownloadtest1.model.DownLoadTaskList;
import ztt.robospicedownloadtest1.request.MyBinaryRequest;
import ztt.robospicedownloadtest1.service.DownLoadService;
import ztt.robospicedownloadtest1.service.NotificationService;
import ztt.robospicedownloadtest1.taskoperation.TaskOperationHub;


public class MainActivity extends ActionBarActivity {

    public static final String TAG="MainActivity";

    SpiceManager spiceManager=new SpiceManager(DownLoadService.class);

    private DownLoadTaskList Tasks;

    private LocalBroadcastManager manager;
    private EditText text_url;
    private View layout_input;
    private boolean isShowInputUrl=false;


    private ListView listView_task;
    private TaskListAdapter taskListAdapter;
    private BroadcastReceiver receiver;



    @Override
    protected void onStart() {
        Log.v(TAG,"activity_start");
        spiceManager.start(this);
        manager=LocalBroadcastManager.getInstance(this);
        receiver=taskListAdapter.getReceiver();
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(GlobalConstants.ACTIONSTRING_BROADCASTRECEIVER);
        manager.registerReceiver(receiver, intentFilter);
        super.onStart();
    }


    @Override
    protected void onStop() {
        spiceManager.shouldStop();
        Tasks.saveTasks();
        manager.unregisterReceiver(receiver);
        super.onStop();
        Log.v(TAG,"activity_stop");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar bar=getSupportActionBar();
        if(bar!=null) {
            bar.setDisplayUseLogoEnabled(false);
            bar.setDisplayShowTitleEnabled(false);
        }
        Tasks= DownLoadTaskList.getInstance(this);
        taskListAdapter= new TaskListAdapter(this,R.layout.downloading_task_list_item,
                Tasks,spiceManager);
        initDir();
        initWidget();
        taskListAdapter.setListView(listView_task);

        //ListViewScroll2SpecificTaskItem();

    }


    private void initDir()
    {
        if(Utils.isSDCardReady()) {
            File requestDirFile = new File(GlobalConstants.AppRootDir);
            DownLoadTask.sAppRootDirName=GlobalConstants.AppRootDir;
            if (!requestDirFile.exists()) {
                if (requestDirFile.mkdir()) {
                    Log.v(TAG,"mkDir success");
                }
            }
        }
    }

    private void initWidget() {
        text_url= (EditText) findViewById(R.id.edittext_url);
        Button btn_url = (Button) findViewById(R.id.btn_url);
        layout_input=findViewById(R.id.layout_input_url);
        listView_task= (ListView) findViewById(R.id.listview_tasks);
        listView_task.setAdapter(taskListAdapter);

        listView_task.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new InformationDialog(MainActivity.this, Tasks.getTasks().get(position))
                        .show();
            }
        });

        listView_task.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView_task.setMultiChoiceModeListener(new myMultiChoiceModeListener());
        btn_url.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = text_url.getText().toString();
                Log.v(TAG, "url:" + url);
                text_url.setText("");
                if (Utils.isHttpUrl(url)) {
                    addNewTask(url);
                } else {
                    showTips(getString(R.string.wrong_url));
                }
            }
        });
    }


    private void showTips(String tip)
    {
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    private void addNewTask(String requestURL)
    {
        if(!Tasks.containTask(requestURL))
        {
            DownLoadTask task=new DownLoadTask();
            task.setURL(requestURL);
            task.setTaskName(Utils.getFileName(task));
            task.setState(DownLoadTask.START);
            File storeFile = Utils.makeEmptyFile(GlobalConstants.AppRootDir, task.getTaskName());
            task.setFile(storeFile);
            Tasks.addTask(task);
            taskListAdapter.notifyDataSetChanged();
        }
        else
        {
           showTips(getString(R.string.tips_already_have_this_task));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id==R.id.action_add_url)
        {
            if (!isShowInputUrl) {
                isShowInputUrl=true;
                layout_input.setVisibility(View.VISIBLE);
            }
            else
            {
                isShowInputUrl=false;
                layout_input.setVisibility(View.GONE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class myMultiChoiceModeListener implements AbsListView.MultiChoiceModeListener
    {

        @Override
        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

        }

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater=mode.getMenuInflater();
            inflater.inflate(R.menu.operation_menu,menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            TaskOperationHub operationHub=new TaskOperationHub(MainActivity.this);
            switch (item.getItemId())
            {
                case R.id.action_delete:
                    List<DownLoadTask> delete_tasks=new ArrayList<>();
                    Map<String,MyBinaryRequest> task2request=taskListAdapter.getTaskRequestMapping();
                    for(int i=taskListAdapter.getCount()-1;i>=0;i--)
                    {
                        if(listView_task.isItemChecked(i))
                        {
                            DownLoadTask task=taskListAdapter.getItem(i);
                            MyBinaryRequest request=task2request.get(task.getURL());
                            //下载中的删除请求
                            if(task.getState()!=DownLoadTask.ACCOMPLISH&&task.getState()!=DownLoadTask.FAILURE
                                    &&request!=null)
                                request.Pause();
                            //下载结束后的删除请求
                            if(task.getState()==DownLoadTask.ACCOMPLISH||task.getState()==DownLoadTask.FAILURE)
                            {
                                //String message=MainActivity.this.getString(R.string)
                            }
                            //task.setWhat2do(DownLoadTask.ctlTaskPause);
                            delete_tasks.add(task);
                            Tasks.deleteTask(task);
                        }
                    }
                    operationHub.DeleteTasksWithFile(spiceManager,delete_tasks);
                    taskListAdapter.notifyDataSetChanged();
                    mode.finish();
                    return true;
                case R.id.action_cancel:
                    mode.finish();
                    return true;
                case R.id.action_send:
                    Log.v(TAG,"action_send");
                    List<DownLoadTask> sendTasks=new ArrayList<>();
                    for(int i=taskListAdapter.getCount()-1;i>=0;i--)
                    {
                        if(listView_task.isItemChecked(i)) {
                            DownLoadTask task = taskListAdapter.getItem(i);
                            if (task.getState() == DownLoadTask.ACCOMPLISH) {
                                sendTasks.add(task);
                            } else {
                                MainActivity.this.showTips("can not send a not uncompleted task");
                                mode.finish();
                                return true;
                            }
                        }
                    }
                    operationHub.ShareResult(sendTasks);
                    mode.finish();
                    return true;

            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {

        }
    }

}