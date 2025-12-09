package smtp.mail;

public class VerifySignature {
    public static void main(String[] args) throws Exception {
        // Dán lại nội dung và chữ ký từ email bạn nhận được
        String message = "leduyba";
        String signature = "E2Jhb8L4fgcWudr85NSi9VFKTrBKOjmHsXQJ2vSsxm2LV6l9O8Oh3WDvdUtkJidVY9ijzMi+/aI37vWr7bjzSj7KUc1oVbx1HZQrFoD5oQDRdSmciJuuFcFlx5dZpmRf0xMc6fr+CKGqsAR43hKd32A58NvKeYQvLdCDMzX8gKxd8X8+V1mz7FkVF/inbi1RhqAb2YvCCH530a/Pb1z1oBTksPlSFeiaSaI+snCHmrwFKRV8a1xKg21vSCr+UwxdRQcuVMdkCXZI2BsMTKX72bzy60l+cyvM9WH1g5/G5v74Lk9pVIO8Ilonu72gqYVms2HlyObgYSDiVfB8sInv5Q==";

        boolean valid = DigitalSignatureUtil.verify(message, signature);
        System.out.println(valid ? "✅ Signature valid - email authentic" : "❌ Signature invalid or altered");
    }
}
