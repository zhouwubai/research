package cs.fiu.edu.textfilter.xml;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Attribute;

import java.util.List;
import java.util.Iterator;

import org.dom4j.io.XMLWriter;

import java.io.*;

import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import cs.fiu.edu.textfilter.custom.Constants;

public class Dom4JParser {

  public void modifyDocument(File inputXml) {

    try {
      SAXReader saxReader = new SAXReader();
      Document document = saxReader.read(inputXml);

      List list = document.selectNodes("/mediawiki");
      Iterator iter = list.iterator();
      System.out.println(list.size());
      
      while(iter.hasNext()){
        Element page = (Element)iter.next();
        Element textEle = page.element("revision").element("text");
        System.out.println(textEle.getTextTrim());
      }
      
     System.out.println(list.size());
      System.out.println();
      
    }catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }
  

  public static void main(String[] argv) {

    Dom4JParser dom4jParser = new Dom4JParser();
    dom4jParser.modifyDocument(new File(Constants.PAGE_XML_PATH));

  }

}
