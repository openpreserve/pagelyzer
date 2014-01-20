/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.lip6.jpagelyzer;

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
