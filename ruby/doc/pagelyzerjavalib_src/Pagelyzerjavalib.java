/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagelyzerjavalib;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import javax.imageio.ImageIO;
/**
 *
 * @author sanojaa
 */
public class Pagelyzerjavalib {

    public BufferedImage getBufferedImage(String base64) {
             String base64enc = (String)base64;  
             BufferedImage bufferedImage=null;
            try {
                InputStream in = new ByteArrayInputStream(Base64.decode(base64enc));  
                bufferedImage = ImageIO.read(in);  
                //System.out.println(bufferedImage);
            } catch (IOException ex) {
                //Logger.getLogger(ScapeTest.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("error");
            }
            return bufferedImage;
        }
}
