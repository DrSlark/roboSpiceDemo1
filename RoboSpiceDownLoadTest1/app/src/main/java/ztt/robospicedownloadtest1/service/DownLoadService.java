package ztt.robospicedownloadtest1.service;

import android.app.Application;
import android.content.Intent;
import android.util.Log;


import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.binary.InFileBigInputStreamObjectPersister;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.request.RequestProcessor;
import com.octo.android.robospice.request.RequestProgressManager;
import com.octo.android.robospice.request.RequestRunner;

import java.io.File;

import ztt.robospicedownloadtest1.Utils;
import ztt.robospicedownloadtest1.model.DownLoadTask;
import ztt.robospicedownloadtest1.model.DownLoadTaskList;


public class DownLoadService extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        File cacheFile=new File(application.getExternalCacheDir(),"cache");
        CacheManager cacheManager=new CacheManager();
        InFileBigInputStreamObjectPersister inFileBigInputStreamObjectPersister=new InFileBigInputStreamObjectPersister(application,cacheFile);
        cacheManager.addPersister(inFileBigInputStreamObjectPersister);
        return cacheManager;
    }

    @Override
    public int getThreadCount() {
        return 3;
    }



}
