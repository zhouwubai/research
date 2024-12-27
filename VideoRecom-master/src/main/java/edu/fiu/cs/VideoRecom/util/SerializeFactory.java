package edu.fiu.cs.VideoRecom.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerializeFactory {

  public static String SIM_MATRIX_PATH = "./src/test/resources/SimGraph";
  
  
  public static void serialize(String outFile, Object object)
      throws IOException {

    FileOutputStream fos = new FileOutputStream(outFile);
    ObjectOutputStream oos = new ObjectOutputStream(fos);
    oos.writeObject(object);
    oos.close();
  }
  

  public static Object deSerialize(String inFile) throws IOException,
      ClassNotFoundException {

    FileInputStream fis = new FileInputStream(inFile);
    ObjectInputStream ois = new ObjectInputStream(fis);
    Object obj = ois.readObject();
    ois.close();
    return obj;
  }

}
