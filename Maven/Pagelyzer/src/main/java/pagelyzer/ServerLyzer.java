/*
# Andrés Sanoja
# UPMC - LIP6
# pagelyzer 
#
# Copyright (C) 2011, 2012, 2013, 2014 Andrés Sanoja, Université Pierre et Marie Curie -
# Laboratoire d'informatique de Paris 6 (LIP6)
#
# Authors
# Andrés Sanoja andres.sanoja@lip6.fr
# Alexis Lechervy alexis.lechervy@lip6.fr
# Zeynep Pehlivan zeynep.pehlivan@lip6.fr
# Myriam Ben Saad myriam.ben-saad@lip6.fr
# Marc Law marc.law@lip6.fr
# Carlos Sureda carlos.sureda@lip6.fr
# Jordi Creus jordi.creus@lip6.fr
# LIP6 / Université Pierre et Marie Curie

# Responsables WP
# Matthieu CORD/UPMC
# Stéphane GANÇARSKI/UPMC
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with this program. If not, see <http://www.gnu.org/licenses/>.
#
# Some parts of this package are adapted from the BrowserShot proyect developed by IM, France.
# https://github.com/sbarton/browser-shot-tool-mapred
 */

package pagelyzer;


import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
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

/**
 * Internal web server. Used to work with javascript injections in the capture of a web page
 * @author sanojaa
 */
public class ServerLyzer implements Container {
    /**
     * the socket connection
     **/
    Connection connection;
    /**
     * the socket port
     */
    static int port;
    /**
     * the custom www root folder
     */
    static String wwwroot=null;
    
    /**
     * handle a http request and serve a file
     * @param request the http request
     * @param response the http response
     */
    @Override
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
              
         response.set("Content-Type", contentType);
         response.set("Server", "ServerLyzer/1.0 (Simple 4.0)");
         response.setDate("Date", time);
         response.setDate("Last-Modified", time);
         String filename="/js/"+path.getPath().replace("/","");
         
         String content;
         if (ServerLyzer.wwwroot == null){
            URL resourceUrl = getClass().getResource(filename);
            content = new Scanner(new File(resourceUrl.toURI().getPath().toString())).useDelimiter("\\Z").next();
         } else {
            content = new Scanner(new File(ServerLyzer.wwwroot+filename)).useDelimiter("\\Z").next();
         }
         body.print(content);

         body.println(msg);
         body.close();
      } catch(IOException | URISyntaxException e) {
         e.printStackTrace();
      }
   }

    /**
     * Starts the server
     * @param port port of the server
     * @param wwwroot custom www root folder
     * @throws Exception
     */
    public void start(int port,String wwwroot) throws Exception {
       ServerLyzer.wwwroot = Utils.checkLastSlash(wwwroot);
       start(port);
   }
    
   /**
     * Starts the server
     * @param port port of the server
     * @throws Exception
     */ 
   public void start(int port) throws Exception {
      ServerLyzer.port = port;
      Container container = new ServerLyzer();
      Server server = new ContainerServer(container);
      this.connection = new SocketConnection(server);
      SocketAddress address = new InetSocketAddress(port);
      System.out.println("Starting server on port "+port);
      this.connection.connect(address);
   }
   
   /**
     * Stops the server
     */
   public void stop() {
       System.out.println("Shutting down server on port "+ServerLyzer.port);
        try {
            this.connection.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerLyzer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ServerLyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
}
