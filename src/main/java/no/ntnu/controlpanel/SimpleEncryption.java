//package no.ntnu.controlpanel;
//
//import javax.crypto.Cipher;
//import javax.crypto.SecretKey;
//import javax.crypto.KeyGenerator;
//import java.security.Key;
//import java.util.Base64;
//
//public class SimpleEncryption {
//    public static void main(String[] args) throws Exception {
//        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
//        keyGenerator.init(256);
//        SecretKey secretKey = keyGenerator.generateKey();
//        String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
//
//        // Message to be encrypted
//        String message = "Hello, this is a secret message!";
//
//        // Encryption
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
//        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
//
//        String encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);
//
//        System.out.println("Encoded Key: " + encodedKey);
//        System.out.println("Encrypted Message: " + encryptedMessage);
//    }
//}
