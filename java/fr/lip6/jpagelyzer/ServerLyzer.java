/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.lip6.jpagelyzer;

/**
 *
 * @author sanojaa
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.simpleframework.http.Path;

import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.Server;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;

public class ServerLyzer implements Container {
    Connection connection;
    int port;
   public void handle(Request request, Response response) {
      try {
         PrintStream body = response.getPrintStream();
         long time = System.currentTimeMillis();
         String msg="";
         String home = System.getProperty("user.dir");
         String contentType = "text/plain";
         Path path=request.getPath();
         if(path.getPath().substring(path.getPath().length()-3).equals(".js")) contentType = "application/javascript";
         if(path.getPath().substring(path.getPath().length()-3).equals("css")) contentType = "text/css";
              
         response.setValue("Content-Type", contentType);
         response.setValue("Server", "ServerLyzer/1.0 (Simple 4.0)");
         response.setDate("Date", time);
         response.setDate("Last-Modified", time);
         String filename=path.getPath().replace("/","");
         
         //InputStream is = new FileInputStream(home+"/"+filename);
         
         String content = new Scanner(new File(home+"/ext/js/"+filename)).useDelimiter("\\Z").next();
         body.print(content);
         
         
         
         body.println(msg);
         body.close();
      } catch(Exception e) {
         e.printStackTrace();
      }
   } 

   public void start(int port) throws Exception {
      this.port = port;
      Container container = new ServerLyzer();
      Server server = new ContainerServer(container);
      this.connection = new SocketConnection(server);
      SocketAddress address = new InetSocketAddress(port);
      System.out.println("Starting server on port "+port);
      this.connection.connect(address);
   }
   public void stop() {
       System.out.println("Shutting down server on port "+this.port);
        try {
            this.connection.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerLyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
}
