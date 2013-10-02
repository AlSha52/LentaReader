package ru.hh.lentareader.dataservice;

import com.foxykeep.datadroid.requestmanager.RequestManager;

import android.content.Context;

/**
 * This class is used as a proxy to call the Service. It provides easy-to-use methods to call the
 * service and manages the Intent creation. It also assures that a request will not be sent again if
 * an exactly identical one is already in progress.
 *
 */
public final class LentaRequestManager extends RequestManager {

    // Singleton management
    private static LentaRequestManager sInstance;

    public synchronized static LentaRequestManager from(Context context) {
        if (sInstance == null) {
            sInstance = new LentaRequestManager(context);
        }

        return sInstance;
    }

    private LentaRequestManager(Context context) {
        super(context, LentaService.class);
    }
}
