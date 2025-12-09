package smtp.mail;

import java.io.FileOutputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class KeyPairGeneratorUtil {

    public static void generateAndSaveKeys() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        // Lưu private key
        try (FileOutputStream fos = new FileOutputStream("private.key")) {
            fos.write(pair.getPrivate().getEncoded());
        }

        // Lưu public key
        try (FileOutputStream fos = new FileOutputStream("public.key")) {
            fos.write(pair.getPublic().getEncoded());
        }

        System.out.println("✅ Keys generated and saved to private.key and public.key");
    }

    public static void main(String[] args) throws Exception {
        generateAndSaveKeys();
    }
}
