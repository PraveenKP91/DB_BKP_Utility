package com.app.backup;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DB2CSV {

  /**
   * @param args
   */
  public static void main(String[] args) {

    String url = AppPropertyReader.getProperty("DB_URL");
    String db = AppPropertyReader.getProperty("DB_NAME");
    String userName = AppPropertyReader.getProperty("DB_USERNAME");
    String password = AppPropertyReader.getProperty("DB_PASSWORD");
    try {
      // Step 1: Allocate a database "Connection" object
      Connection con = DriverManager.getConnection(
          url + ";databaseName=" + db + ";user=" + userName + ";password=" + password);
      Statement st = con.createStatement();

      // list of tables to be backup
      String tables = AppPropertyReader.getProperty("TABLE_LIST");
      List<String> tableListToBackup = Arrays.asList(tables.split(","));
      tableListToBackup =
          tableListToBackup.stream().map(String::toLowerCase).collect(Collectors.toList());

      System.out.println("Number of Tables to be backup : " + tableListToBackup.size());

      // this query gets all the tables in your database
      ResultSet res = st.executeQuery("SELECT table_name FROM INFORMATION_SCHEMA.TABLES ");

      String table = null;

      // Preparing List of table Names
      List<String> tableNameList = new ArrayList<String>();
      while (res.next()) {
        table = res.getString(1);
        if (tableListToBackup.contains(table.toLowerCase())) {
          tableNameList.add(table);
          System.out.println("Table added to List : " + table);
        }
      }

      System.out.println("Number of tables found : " + tableNameList.size());

      FileWriter fw;

      // path to the folder where you will save your csv files
      String ExportPath = AppPropertyReader.getProperty("EXPORT_PATH");

      DateFormat df = new SimpleDateFormat("dd-MM-yy HH:mm:ss");
      Date dateobj = new Date();
      String time = null;

      // star iterating on each table to fetch its data and save in a .csv file
      for (String tableName : tableNameList) {

        time = df.format(dateobj);
        System.out.println("Export Start for Table '" + tableName + "' at " + time);

        // select all data from table
        res = st.executeQuery("select * from dbo." + tableName + "(nolock) order by 1");

        // colunm count is necessay as the tables are dynamic and we need to figure out
        // the numbers of columns
        int colunmCount = getColumnCount(res);

        try {
          fw = new FileWriter(ExportPath + "" + tableName + " " + time + ".csv");

          // this loop is used to add column names at the top of file , if you do not need
          // it just comment this loop
          for (int i = 1; i <= colunmCount; i++) {
            fw.append(res.getMetaData().getColumnName(i));
            fw.append(",");

          }

          fw.append(System.getProperty("line.separator"));

          while (res.next()) {
            for (int i = 1; i <= colunmCount; i++) {
              // everything to string first and then saving
              if (res.getObject(i) != null) {
                String data = res.getObject(i).toString();
                fw.append(data);
                if(i!=colunmCount)
                fw.append(",");
              } else {
                String data = "null";
                fw.append(data);
                if(i!=colunmCount)
                fw.append(",");
              }
            }
            // new line entered after each row
            fw.append(System.getProperty("line.separator"));
          }

          fw.flush();
          fw.close();

        } catch (IOException e) {
          System.err.println("Exception while exporting data : " + e);
        }

      }

      con.close();
    } catch (Exception e) {
      System.err.println("Could not load JDBC driver");
      e.printStackTrace();
    }
  }

  // to get no of columns in result set
  public static int getColumnCount(ResultSet res) throws SQLException {
    return res.getMetaData().getColumnCount();
  }

}