package cs.fiu.edu.textfilter.examples;


import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

import cs.fiu.edu.textfilter.xml.StaffXmlHandler;

/**
 * use handler to store and extract information, but another problem is 
 * how to preserve tag.
 * @author zhouwubai
 *
 */

public class ReadXmlFile {

	public static String STAFF_FILE_PATH = "./src/test/resources/staff.xml";
	public static String PAGE_FILE_PATH = "./src/test/resources/page.xml";

	public static void main(String[] args) {

		try {

			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();

			DefaultHandler handler = new StaffXmlHandler();
			
			saxParser.parse(PAGE_FILE_PATH, handler);
			

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
