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
import java.net.URL;
import java.nio.file.Paths;
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
              
         response.set("Content-Type", contentType);
         response.set("Server", "ServerLyzer/1.0 (Simple 4.0)");
         response.setDate("Date", time);
         response.setDate("Last-Modified", time);
         String filename=path.getPath().replace("/","");
         
         //InputStream is = new FileInputStream(home+"/"+filename);
         
          // Before maven 
         //String content = new Scanner(new File(home+"/ext/js/"+filename)).useDelimiter("\\Z").next();
         String resourceUrl = getClass().getResource("/js/" + filename).getPath();
      //   Path resourcePath = (Path) Paths.get(resourceUrl.toURI());
         String content = new Scanner(new File(resourceUrl)).useDelimiter("\\Z").next();
         
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
