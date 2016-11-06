package com.strater.jenn

import groovy.util.logging.Slf4j
import org.bouncycastle.jcajce.provider.digest.SHA3
import org.bouncycastle.util.encoders.Hex
import java.security.SecureRandom

@Slf4j
class Crypto {
    static SHA3.DigestSHA3 md = new SHA3.DigestSHA3(256)

    static String encrypt(String message) {
        md.update(message.getBytes('UTF-8'))
        String encrypted = Hex.toHexString(md.digest())
        return encrypted
    }

    static String encrypt(String salt, String password) {
        String message = salt + password
        encrypt(message)
    }

    static Boolean compare(String salt, String password, String hash) {
        String message = salt + password
        md.update(message.getBytes('UTF-8'))
        hash == Hex.toHexString(md.digest())
    }

    static String generateSessionId() {
        SecureRandom random = new SecureRandom()
        byte[] bytes = new byte[16]
        random.nextBytes(bytes)
        encrypt(Hex.toHexString(bytes))
    }
}
