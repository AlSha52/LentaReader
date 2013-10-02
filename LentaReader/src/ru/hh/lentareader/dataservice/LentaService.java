package ru.hh.lentareader.dataservice;

import com.foxykeep.datadroid.service.RequestService;

import android.content.Intent;

/**
 * This class is called by the {@link LentaRequestManager} through the {@link Intent} system.
 *
 * @author GSysoev
 */
public final class LentaService extends RequestService {
	

	@Override
	protected int getMaximumNumberOfThreads() {
		return 3;
	}
	
    @Override
    public Operation getOperationForType(int requestType) {
        switch (requestType) {
        case LentaRequestFactory.REQUEST_TYPE_FEED:
            return new FeedOperation();
        }
        	
        return null;
    }

	
}
