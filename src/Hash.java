
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ronakshahmac
 */
public class Hash {
    static MessageDigest md_Instance;

    public Hash()  {
        try {
            md_Instance  = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Hash.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
        
    public String getHash(String value) 
    {
        md_Instance.update(value.getBytes());
        byte[] tmp = md_Instance.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tmp.length; i++) {
          sb.append(Integer.toString((tmp[i] & 0xff) + 0x100, 16).substring(1));
        }
        return new String(sb);
        
    }

}
