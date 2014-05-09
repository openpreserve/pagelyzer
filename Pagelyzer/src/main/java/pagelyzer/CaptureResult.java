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

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

/**
 * Data structure to hold capture results
 * @author sanojaa
 */
public class CaptureResult {
   /**
   * byte array to hold an image
   */
    public byte[] image;
   /**
   * String to hold the segmentation results
   */
    public String viXML;
       /**
   * String to hold the rendered source code
   */
    public String srcHTML;
       /**
   * Used for debugging. Intermediate images.
   */
   // public byte[] debug;

    /**
     * Constructor
     */
    public CaptureResult() {
      //  this.debug = null;
        this.srcHTML = null;
        this.image = null;
        this.viXML = null;
    }

    /**
     * Get bufferImage from byte array
     * @return BufferedImage
     */
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

    /**
     * Writes debug image to disk
     * @param to file to save to
     */
    public void saveDebugFile(String to) {
        if (this.image != null)
        {
	          OutputStream out=null;
	        try {
	             out = new BufferedOutputStream(new FileOutputStream(to));
	             System.out.println("Writing debug image file in "+to);
	             out.write(this.image);
	             out.close();
	         } catch (FileNotFoundException ex) {
	             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
	         } catch (IOException ex) {
	             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
	         } catch (Exception ex) {
	             Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
	         }
        }
        if (this.viXML != null)
        {
        	 FileWriter fileWriter = null;
             try {

                 File newTextFile = new File(to + ".xml");
                 fileWriter = new FileWriter(newTextFile);
                 fileWriter.write(viXML);
                 fileWriter.close();
             } catch (IOException ex) {
                 Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
             } finally {
                 try {
                     fileWriter.close();
                 } catch (IOException ex) {
                     Logger.getLogger(JPagelyzer.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
        }
        
    }
      
}
