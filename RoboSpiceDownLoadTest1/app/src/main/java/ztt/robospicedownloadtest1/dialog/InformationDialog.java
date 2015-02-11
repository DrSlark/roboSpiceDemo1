

package ztt.robospicedownloadtest1.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;



import ztt.robospicedownloadtest1.R;
import ztt.robospicedownloadtest1.Utils;
import ztt.robospicedownloadtest1.model.DownLoadTask;

public class InformationDialog extends AlertDialog {

    private DownLoadTask mTask;
    private Context mContext;
    private View mView;

    public InformationDialog(Context context, DownLoadTask task) {
        super(context);
        mTask=task;
        mContext = context;
    }

    protected void onCreate(Bundle savedInstanceState) {
        mView = getLayoutInflater().inflate(R.layout.taskinfo_dialog, null);
        ((TextView) mView.findViewById(R.id.information_name))
                .setText(mTask.getTaskName());
        ((TextView) mView.findViewById(R.id.information_type))
                .setText(mTask.getMimeType());
        ((TextView) mView.findViewById(R.id.information_content_length))
                .setText(Utils.convertStorage(mTask.getcontentlength()));
        ((TextView) mView.findViewById(R.id.information_downloaded))
                .setText(Utils.convertStorage(mTask.getDownLoadedBytes()));
        ((TextView) mView.findViewById(R.id.information_location))
                .setText(mTask.getFile().getPath());
        ((TextView) mView.findViewById(R.id.information_state))
                .setText(Utils.convertState2String(mTask.getState()));
        setView(mView);
        setButton(BUTTON_NEGATIVE, "OK", (OnClickListener) null);

        super.onCreate(savedInstanceState);
    }


}
