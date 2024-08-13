package com.pratyush.docsearch.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.pratyush.docsearch.PropertiesLoader;

public class DatabaseDriver {

   Connection c = null;
   Statement stmt = null;
   private String base_query = "select document.id, document.document from document where document.id in (%s)";

   public DatabaseDriver() {
      try {
         Properties config;
         config = PropertiesLoader.loadProperties();

         String url = config.getProperty("pg_url");
         String username = config.getProperty("pg_user");
         String password = config.getProperty("pg_password");

         Class.forName("org.postgresql.Driver");
         this.c = DriverManager.getConnection(
            url,
            username,
            password);
         c.setAutoCommit(false);
         this.stmt = this.c.createStatement();
      } catch(Exception e) {
         System.err.println(e.getClass().getName() + ": " + e.getMessage());
      }
   }

   private ResultSet executeStatement(String query) {
      ResultSet rs = null;
      try {
         rs = this.stmt.executeQuery(query);
      } catch (SQLException e) {
         e.printStackTrace();
      }
      return rs;
   }

   public Map<Long, String> getDocumentIdToDocument(List<Long> documentIds) {
      String args = "";
      for(Long documentId: documentIds) {
         args += documentId.toString() + ",";
      }
      args = args.substring(0, args.length() - 1);

      System.out.println(String.format(this.base_query,args));
      ResultSet rs = executeStatement(String.format(this.base_query,args));
      Map<Long, String> results = new HashMap<>();
      try {
         while(rs.next()) {
            Long id = rs.getLong("id");
            String doc = rs.getString("document");
            results.put(id, results.getOrDefault(id, "") + doc);
         }
         rs.close();

         return results;
      } catch (SQLException e) {
            e.printStackTrace();
            return results;
         }
   }

   public static void main( String args[] ) {
      try {
        DatabaseDriver databaseDriver = new DatabaseDriver();
        System.out.println(databaseDriver.getDocumentIdToDocument(Arrays.asList(151l)));
      } catch ( Exception e ) {
         System.err.println( e.getClass().getName()+": "+ e.getMessage() );
         System.exit(0);
      }
      System.out.println("Operation done successfully");
   }
}