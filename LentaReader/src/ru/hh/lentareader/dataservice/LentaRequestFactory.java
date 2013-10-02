

package ru.hh.lentareader.dataservice;

import com.foxykeep.datadroid.requestmanager.Request;

/**
 * Class used to create the {@link Request}s.
 *
 * @author GSysoev
 */
public final class LentaRequestFactory {

    // Request types
    public static final int REQUEST_TYPE_FEED = 0;

    public static final String BUNDLE_EXTRA_FEED_RESULT =
            "ru.hh.lentareader.feedResult";
    
    public static final String FEED_PARAM_PATH =
            "ru.hh.lentareader.feedParamPath";
    public static final String FEED_PARAM_APPID =
            "ru.hh.lentareader.feedParamAppid";

    private LentaRequestFactory() {
        // no public constructor
    }
    
    public static Request feedRequest(String path, String appId) {
        Request request = new Request(REQUEST_TYPE_FEED);
        request.put(LentaRequestFactory.FEED_PARAM_PATH, path);
        request.put(LentaRequestFactory.FEED_PARAM_APPID, path);
        return request;
    }
    

}
