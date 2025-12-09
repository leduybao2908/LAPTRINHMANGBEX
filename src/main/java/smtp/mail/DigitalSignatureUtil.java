package smtp.mail;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.*;

import java.util.Base64;

public class DigitalSignatureUtil {

    private static PrivateKey loadPrivateKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }

    private static PublicKey loadPublicKey(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(filename));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    // Tạo chữ ký từ nội dung và private key
    public static String sign(String data) throws Exception {
        PrivateKey privateKey = loadPrivateKey("private.key");
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data.getBytes());
        return Base64.getEncoder().encodeToString(signature.sign());
    }

    // Kiểm tra chữ ký với public key
    public static boolean verify(String data, String sigBase64) throws Exception {
        PublicKey publicKey = loadPublicKey("public.key");
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(data.getBytes());
        byte[] sigBytes = Base64.getDecoder().decode(sigBase64);
        return signature.verify(sigBytes);
    }
}
