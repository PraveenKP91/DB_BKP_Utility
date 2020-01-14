package com.app.backup;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppPropertyReader {
  /**
   *
   * @author billion
   */
  public static String getProperty(String property) {
    String result = null;

    Properties prop = new Properties();
    try {
      File jarPath = new File(AppPropertyReader.class.getProtectionDomain().getCodeSource().getLocation().getPath());
      String propertiesPath = jarPath.getParentFile().getAbsolutePath();
      prop.load(new FileInputStream(propertiesPath + "/EvolveBackup.properties"));
      result = prop.getProperty(property);
    } catch (IOException e) {
      System.err.println("Exception while reading property file : "+e);
    }
    return result;
  }
}