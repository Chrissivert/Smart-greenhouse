package no.ntnu.tools;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncrypterDecrypter {

    private static PublicKey publicKey;
    private static PrivateKey privateKey;

    static {

        try {

            String publicKeyB64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAipZ/MjCzjT+YzTh0x62NulXUUGwI8kUtKVuqbOPj46bUzFMFyK3ruq/Bx7ZYJq6Wp7kZ8JueCh07MkSx4GB8vBjExAvyS52tPa9cN1PTQxz/ounv5JtWnZj2tRodj8/7LcM6eqqJQgbrq8wkzbMQfcN+uWxvP53s0cFEuBD/8GJzOyAgUu0kFL0F9/M+m2LEpr/uVGQCTu7da3jIMVZojNekyJQs4VEzYXhQA13imXJbMTCvt5uANZ+qwwgTccrH+7k4U7wjpM8X3sIbtfuScpMSxi7J3kP1TelJbvbRfeXqDiT/WmUJFuuQMB5wna5dR0K32Cbqh2eFHYOVY3j1sQIDAQAB";
            String privateKeyB64 = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCKln8yMLONP5jNOHTHrY26VdRQbAjyRS0pW6ps4+PjptTMUwXIreu6r8HHtlgmrpanuRnwm54KHTsyRLHgYHy8GMTEC/JLna09r1w3U9NDHP+i6e/km1admPa1Gh2Pz/stwzp6qolCBuurzCTNsxB9w365bG8/nezRwUS4EP/wYnM7ICBS7SQUvQX38z6bYsSmv+5UZAJO7t1reMgxVmiM16TIlCzhUTNheFADXeKZclsxMK+3m4A1n6rDCBNxysf7uThTvCOkzxfewhu1+5JykxLGLsneQ/VN6Ulu9tF95eoOJP9aZQkW65AwHnCdrl1HQrfYJuqHZ4Udg5VjePWxAgMBAAECggEAdwTWiekPWl/iv2Qbzpx7Giq54rNVX5MPPPSPQWLZhNny6OLoFbdfuf1VI0mzHM1VbwdlgqBysmb2Pq6GQJc8qGFxIpjL5iqs7Evcm1tvFLUjyeq0bhUF5uTGAzRBbQ+FvIBsiYpJxJ2i6fCxLsL4h7lL7Lx2MiFTj7Q2awmWc/8cpEaYeeGHjFBA4uKf75CyGACq81Uoi5dLqIECbAZRbSNxhNfhTbFXvtWLfps/Mt7qsXdpPiuD6CfT7ZZA/yf+B2Iopvh2/yO0Rzew7ljJinxGVvjxpilUoFyXPYvZltoFNt4ahsnfPxfdutpnh1qrC+8t5jZY5G3A6TPv4EdcjQKBgQDtL3TSBLhI0G0fhCjKScRm6QkrSbkf96C8aNP1nIeGPOMPPVCtYbh6xa00C16OmebcPY+MzITIIab95uR4QcXMTH5nWtHH2t0NcXNkjBcBb/swySrsoVj6unUFKptsP6iUM2PDQ9uDk4XFW1LkvxE1we/3PWX5LeWL9smVGQwy7wKBgQCVlNAO9QjDD40TauaegA4rH3Fd0z6G80f29NKYCcJGERLjL0KwEGgtm3uXhZ5t0bbaiGdrAQJALrxSUj9nVDPO+w5VFfdA56FFqo++b7nys0mYhCcP0PAdbp6xGf4OZ9Y9dQlbin94JUM2GAi8HbMr68Mi04te6LQReNTJW7LhXwKBgQDCai7QY2wGy0lXFwY0YejqDcQlRmXHLTwEk8yBu49e2hOoDzNNGxeTEutZCKS2MLKJ/q/m0lFYljUCU7scU5VlU6Ic59WiguTEuyTB2w6UcAyuvYZAtjRwOFvpIzaIsVlmOQViLgIFxOtWjPfSZkceEFy5BQvMBvHsGxq66vAT4wKBgEysvYINRPKINC6x64lv5tNgCMKpmDGg31DN6m2ZAwbblazy+uI5Zs2KF+5xxeZRS8P1i92j5L11t38TPgD/fpcoxg4DdnzEvzxw08Iwj0bDdIRDqapH1e2gWKQ7yKklvQI/zg4ojnLN1wgRhdi3+LIY5iwh+B0sd7FFGP4mjSlNAoGALUDgM91GEDq9EcxTVeH7d44sw+ubnL+Aza96ibnN6ByUwnU9pLXawmgy8SBCtk8iTB6vsCMBQT2pGoJ6KTqp969F3A6Zko3SA/INPa/EvDgUXzgHKYuxXtBB2Rjg15TcXRmn2NdF9rNiF8aiMC2+5uNcE9TzBbNpAtrFo0oYNT0=";

            byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyB64);
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyB64);

            publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));
            privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKeyBytes));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

    }


    public static String encryptCommand(String command) {
        String encryptedMessage = null;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] encryptedBytes = cipher.doFinal(command.getBytes());

            encryptedMessage = Base64.getEncoder().encodeToString(encryptedBytes);

            //Logger.info("Encrypted Message: " + encryptedMessage);

            return encryptedMessage;
        } catch (Exception e) {
            Logger.error("Error encrypting the command: " + e.getMessage());
            return encryptedMessage;
        }
    }



    public static String decryptMessage(String commandToDecrypt) {
        String decryptedMessage = null;

        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(commandToDecrypt));

            decryptedMessage = new String(decryptedBytes);

            //Logger.info("Decrypted Message: " + decryptedMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return decryptedMessage;
    }

}
