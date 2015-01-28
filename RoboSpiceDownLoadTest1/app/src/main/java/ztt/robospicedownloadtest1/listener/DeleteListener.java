package ztt.robospicedownloadtest1.listener;

import android.content.Context;
import android.widget.Toast;

import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.PendingRequestListener;

/**
 * Created by 123 on 2015/1/19.
 */
public class DeleteListener implements PendingRequestListener<Boolean> {
    private Context mContext;

    public DeleteListener(Context mContext) {

        this.mContext = mContext;
    }

    @Override
    public void onRequestNotFound() {

    }

    @Override
    public void onRequestFailure(SpiceException e) {

    }

    @Override
    public void onRequestSuccess(Boolean result) {
        Toast.makeText(mContext, "tasks has been removed", Toast.LENGTH_SHORT).show();

    }
}
