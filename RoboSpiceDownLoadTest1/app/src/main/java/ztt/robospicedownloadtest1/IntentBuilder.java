/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package ztt.robospicedownloadtest1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ztt.robospicedownloadtest1.model.DownLoadTask;

/**
 * 职责：提供静态方法，根据文件类型构造Intent和启动相应Activity
 * */
public class IntentBuilder {

    public static void viewFile(final Context context, final String filePath) {
        String type = getMimeType(filePath);
        if (!TextUtils.isEmpty(type) && !TextUtils.equals(type, "*/*")) {
            /* 设置intent的file与MimeType */
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(new File(filePath)), type);
            context.startActivity(intent);
        } else {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
            dialogBuilder.setTitle(R.string.dialog_select_type);

            CharSequence[] menuItemArray = new CharSequence[] {
                    context.getString(R.string.dialog_type_text),
                    context.getString(R.string.dialog_type_audio),
                    context.getString(R.string.dialog_type_video),
                    context.getString(R.string.dialog_type_image),
                    context.getString(R.string.dialog_type_html)};
            // 给四种替补方案
            dialogBuilder.setItems(menuItemArray,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String selectType = "*/*";
                            switch (which) {
                            case 0:
                                selectType = "text/plain";
                                break;
                            case 1:
                                selectType = "audio/*";
                                break;
                            case 2:
                                selectType = "video/*";
                                break;
                            case 3:
                                selectType = "image/*";
                                break;
                            case 4:
                                 selectType="text/html";
                            }
                            Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.fromFile(new File(filePath)), selectType);
                            context.startActivity(intent);
                        }
                    });
            dialogBuilder.show();
        }
    }

    /**
     * 作用：通过FileInfo list构造Intent，主要是设置Uri和MIME type
     * */
    public static Intent buildSendFile(List<DownLoadTask> tasks) {
        ArrayList<Uri> uris = new ArrayList<Uri>();

        String mimeType = "*/*";
        for (DownLoadTask task : tasks) {
            File fileIn = task.getFile();
            mimeType = getMimeType(task.getTaskName());
            // 构造Uri
            Uri u = Uri.fromFile(fileIn);
            uris.add(u);
        }

        if (uris.size() == 0)
            return null;

        boolean multiple = uris.size() > 1;
        Intent intent = new Intent(multiple ? Intent.ACTION_SEND_MULTIPLE
                : Intent.ACTION_SEND);

        if (multiple) {
        	// 多条Uri
            intent.setType("*/*");
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        } else {
        	// 单条Uri
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, uris.get(0));
        }

        return intent;
    }

    /**
     * 作用：通过文件路径得到MIME type
     * */
    private static String getMimeType(String filePath) {
        int dotPosition = filePath.lastIndexOf('.');
        // 没有扩展名，属于任意类型
        if (dotPosition == -1)
            return "*/*";
        // 根据扩展名，得打MIME type
        String ext = filePath.substring(dotPosition + 1, filePath.length()).toLowerCase();
        String mimeType = MimeUtils.guessMimeTypeFromExtension(ext);
        if (ext.equals("mtz")) {
            mimeType = "application/miui-mtz";
        }
        
        return mimeType != null ? mimeType : "*/*";
    }
}
