package ru.hh.lentareader.dataservice;

import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import ru.hh.lentareader.util.LentaParseHandler;
import android.content.Context;
import android.os.Bundle;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;

/**
 * A operation that loads selected news feed and save it to db via LentaParseHandler
 * 
 * @author GSysoev
 */
public class FeedOperation implements Operation{
	
	@Override
	public Bundle execute(Context context, Request request)
			throws ConnectionException, DataException {
		NetworkConnection networkConnection = new NetworkConnection(context,
				"http://lenta.ru/rss/" + 
				request.getString(LentaRequestFactory.FEED_PARAM_PATH));
		ConnectionResult result = networkConnection.execute();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();		       
        SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
			LentaParseHandler handler = new LentaParseHandler(context, 
					request.getString(LentaRequestFactory.FEED_PARAM_APPID));
	        saxParser.parse(new InputSource(new StringReader(result.body)), handler);
		} catch (Exception e) {
			e.printStackTrace();
			throw new DataException();
		}
		
		return null;
	}
}
