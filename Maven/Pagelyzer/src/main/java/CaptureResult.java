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



import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author sanojaa
 */
public class CaptureResult {
     public byte[] image = null;
     public String viXML = null;
     public String srcHTML = null;
     public byte[] debug = null;
     
      public BufferedImage getBufferedImage() {
          InputStream in = new ByteArrayInputStream(this.image);
          BufferedImage bimage = null;
         try {
             bimage = ImageIO.read(in);
         } catch (IOException ex) {
             Logger.getLogger(CaptureResult.class.getName()).log(Level.SEVERE, null, ex);
         }
        return bimage;
      }
      
      public void saveDebugFile(String to) {
          if (this.debug == null) return;
          OutputStream out=null;
        try {
             out = new BufferedOutputStream(new FileOutputStream(to));
             System.out.println("Writing debug image file in "+to);
             out.write(this.debug);
             out.close();
         } catch (FileNotFoundException ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         } catch (Exception ex) {
             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
         }
      }
}
