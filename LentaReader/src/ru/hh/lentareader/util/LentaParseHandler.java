package ru.hh.lentareader.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ru.hh.lentareader.data.NewsDBHelper;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * News feed -> SQLite db parser
 * 
 * @author GSysoev
 */
public class LentaParseHandler extends DefaultHandler {
 
    // We have a local reference to an object which is constructed while parser is working on an item tag
    // Used to reference item while parsing
    private LentaItem currentItem;
    
    // We have two indicators which are used to differentiate whether a tag title or link is being processed by the parser
    // Parsing title indicator
    private LentaItemType parsingItem;
    
    private NewsDBHelper dbHelper;
    private SQLiteDatabase db;
    private String appId;
    
    private StringBuffer buffer;
 
    public LentaParseHandler(Context con, String appId) {
    	dbHelper = new NewsDBHelper(con);
    	this.appId = appId;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    	if (qName.equals("rss")) {
    		db = dbHelper.getWritableDatabase();
    		db.delete(NewsDBHelper.DB_NAME, NewsDBHelper.KEY_APP_ID + "='" + appId + "'", null);
    	} else if (qName.equals("item")) {
            currentItem = new LentaItem();
        } else if (qName.equals(LentaItemType.IMAGE.rssName)){
        	currentItem.image = attributes.getValue("url");
        } else if (qName.equals(LentaItemType.DESCR.rssName)) {
        	//CDATA PROBLEM
        	 buffer = new StringBuffer();
        } else {
        	for (LentaItemType item : LentaItemType.values()) {
        		if (qName.equals(item.rssName)) {
        			parsingItem = item;
        			break;
        		}
        	}
        }
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
    	if (qName.equals("rss")) {
    		dbHelper.close();
    	} else if (qName.equals("item")) {
    		addItemToDb();
            currentItem = null;
        } else if (qName.equals(LentaItemType.DESCR.rssName)) {
        	if (currentItem != null) {
        		currentItem.desc = buffer.toString().trim();
        	}
        	buffer = null;
        } else {
        	parsingItem = null;
        }
    }

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String s = new String(ch, start, length);
		if (buffer != null) {
			buffer.append(s);
		} else if (currentItem != null) {

			if (parsingItem != null) {
				switch (parsingItem) {
				case GUID:
					currentItem.guid = s;
					break;
				case TITLE:
					currentItem.title = s;
					break;
				case LINK:
					currentItem.link = s;
					break;
				case DESCR:
					currentItem.desc = s;
					break;
				case DATE:
					currentItem.date = s;
					break;
				case IMAGE:
					currentItem.image = s;
					break;
				case CATEGORY:
					currentItem.category = s;
					break;
				}
			}
		}
	}
    
    private void addItemToDb() {
    	ContentValues cv = currentItem.toContentValues();
    	cv.put(NewsDBHelper.KEY_APP_ID, appId);
		db.insert(NewsDBHelper.DB_NAME, null, cv);
	}
    
    private static class LentaItem {
    	String guid;
    	String title;
    	String link;
    	String desc;
    	String date;
        String image;
    	String category;
    	
    	@SuppressWarnings("deprecation")
		ContentValues toContentValues() {
    		ContentValues cv = new ContentValues();
    		cv.put(NewsDBHelper.KEY_GUID, guid);
    		cv.put(NewsDBHelper.KEY_TITLE, title);
    		cv.put(NewsDBHelper.KEY_LINK, link);
    		cv.put(NewsDBHelper.KEY_DESCRIPTION, desc);
    		SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzzz", Locale.ENGLISH);
    		Date date = null;
    		String dateRes = "";
			try {
				date = formatter.parse(this.date);
				dateRes = date.getDate() + " ";
				String month = "";
				switch (date.getMonth() + 1) {
				case 1:
					month = "янв";
					break;
				case 2:
					month = "фев";
					break;
				case 3:
					month = "мар";
					break;
				case 4:
					month = "апр";
					break;
				case 5:
					month = "май";
					break;
				case 6:
					month = "июн";
					break;
				case 7:
					month = "июл";
					break;
				case 8:
					month = "авг";
					break;
				case 9:
					month = "сен";
					break;
				case 10:
					month = "окт";
					break;
				case 11:
					month = "ноя";
					break;
				case 12:
					month = "дек";
					break;
				}
				dateRes += month + " " + (date.getYear() - 100) + ", ";
				dateRes += (date.getHours() > 9 ? date.getHours() : "0"
						+ date.getHours())
						+ ":"
						+ (date.getMinutes() > 9 ? date.getMinutes() : "0"
								+ date.getMinutes());
			} catch (ParseException e) {
			}
    		cv.put(NewsDBHelper.KEY_DATE, dateRes);
    		if (image == null) {
    			cv.put(NewsDBHelper.KEY_IMAGE, "-");
    		} else {
    			cv.put(NewsDBHelper.KEY_IMAGE, image);
    		}
    		cv.put(NewsDBHelper.KEY_CATEGORY, category);
    		return cv;
    	}
    }
    
    static enum LentaItemType {
    	GUID("guid"), 
    	TITLE("title"), 
    	LINK("link"), 
    	DESCR("description"),
    	DATE("pubDate"), 
    	IMAGE("enclosure"), 
    	CATEGORY("category");
    	
    	private String rssName;
    	
    	private LentaItemType(String rssName) {
    		this.rssName = rssName;
    	}
    	
    	public String getRssName() {
    		return rssName;
    	}
    	
    }
}
