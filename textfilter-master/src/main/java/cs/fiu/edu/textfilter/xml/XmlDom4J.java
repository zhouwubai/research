package cs.fiu.edu.textfilter.xml;

import org.apache.hadoop.io.Text;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.io.*;

import org.dom4j.io.SAXReader;

import cs.fiu.edu.textfilter.custom.Constants;

public class XmlDom4J {

  public void modifyDocument(File inputXml) {

    try {
      SAXReader saxReader = new SAXReader();
      Document document = saxReader.read(inputXml);
      
      Map<String, String> namespaceUris = new HashMap<String, String>();  
      namespaceUris.put("mediawiki", "http://www.mediawiki.org/xml/export-0.3/");
      
      XPath xPath = DocumentHelper.createXPath("//mediawiki:page");
      xPath.setNamespaceURIs(namespaceUris);
      
      List list = xPath.selectNodes(document);
      Iterator iter = list.iterator();
      System.out.println(list.size());
      String[] keywords = {"Authority","disaster"};
      
      while(iter.hasNext()){
        Element page = (Element) iter.next();
        Element textEle = page.element("revision").element("text");
        Element timeEle = page.element("revision").element("timestamp");
        for (String keyword : keywords) {
          if (textEle.getTextTrim().contains(keyword)) {
            System.out.println(page.asXML());
            break;
          }
        }
      }
      
      
    }catch (Exception e) {
      e.printStackTrace();
    }
  }
  

  public static void main(String[] argv) {

    XmlDom4J dom4jParser = new XmlDom4J();
    dom4jParser.modifyDocument(new File(Constants.PAGE_XML_PATH));

  }

}
