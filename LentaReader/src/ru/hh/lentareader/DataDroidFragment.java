/**
 *
 */
package ru.hh.lentareader;

import java.util.ArrayList;

import ru.hh.lentareader.dataservice.LentaRequestManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;


/**
 * A fragment that could send requests.
 * 
 * @author GSysoev
 */
public abstract class DataDroidFragment extends Fragment implements RequestListener {

    protected static final String SAVED_STATE_REQUEST_LIST = "savedStateRequestList";

    protected LentaRequestManager mRequestManager;
    protected ArrayList<Request> mRequestList;

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRequestManager = LentaRequestManager.from(getActivity());
        if (savedInstanceState != null) {
            mRequestList = savedInstanceState.getParcelableArrayList(SAVED_STATE_REQUEST_LIST);
        } else {
            mRequestList = new ArrayList<Request>();
        }
    }


	@Override
	public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_STATE_REQUEST_LIST, mRequestList);
        super.onSaveInstanceState(outState);
    }
	
	@Override
	public void onPause() {
		super.onPause();
		if (!mRequestList.isEmpty()) {
			mRequestManager.removeRequestListener(this);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		for (int i = 0; i < mRequestList.size(); i++) {
			Request request = mRequestList.get(i);

			if (mRequestManager.isRequestInProgress(request)) {
				mRequestManager.addRequestListener(this, request);
			} else {
				mRequestManager.callListenerWithCachedData(this, request);
				i--;
				mRequestList.remove(request);
			}
		}
	}
	
	@Override
	public void onRequestConnectionError(Request request, int statusCode) {
		requestError(request);
	}

	@Override
	public void onRequestDataError(Request request) {
		requestError(request);
	}

	@Override
	public void onRequestCustomError(Request request, Bundle resultData) {
		requestError(request);
	}
	
	public abstract void requestError(Request request);
}
