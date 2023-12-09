package no.ntnu.controlpanel;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class SimpleDecryption {
    public static void main(String[] args) throws Exception {
        String receivedEncodedKey = "ReceivedEncodedKey"; // Replace with actual received key

        String receivedEncryptedMessage = "ReceivedEncryptedMessage"; // Replace with actual received message

        byte[] decodedKey = Base64.getDecoder().decode(receivedEncodedKey);
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, originalKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(receivedEncryptedMessage));

        String decryptedMessage = new String(decryptedBytes);

        System.out.println("Decrypted Message: " + decryptedMessage);
    }
}
