package sm.system.security.crypto;

import org.bouncycastle.asn1.gm.GMNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.crypto.engines.SM2Engine;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.ParametersWithRandom;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BouncyCastleBrowserPasswordCipherTests {

    private static final String JS_CIPHERTEXT_WITH_C1_X_STARTING_02 =
            "02f2d4045ae1e3092a891676b9b2b2382aadb743f936ca1ef08fb0292499a2e4"
                    + "96113e3d73cf957567c549362dbb9a7e82ee85f6d3093ecd3f8b40976405212abe"
                    + "bafb83c5b1d3be9d1d753d3f7549d70b6513d0625c7de28edd58958f28c5f90f2c0077";

    private final BouncyCastleBrowserPasswordCipher cipher =
            new BouncyCastleBrowserPasswordCipher(Sm2PropertiesTests.validProperties());

    @Test
    void decryptsRealSm2JsCiphertextWithoutGuessingTheMissingPointPrefix() {
        assertEquals("ABCD", cipher.decrypt(JS_CIPHERTEXT_WITH_C1_X_STARTING_02));
    }

    @Test
    void decryptsUtf8TextAndExposesNormalizedPublicKey() throws InvalidCipherTextException {
        String plaintext = "密码-SmartManage-🔐";

        assertEquals(plaintext, cipher.decrypt(encryptAsBrowserCiphertext(plaintext.getBytes(StandardCharsets.UTF_8))));
        assertEquals(Sm2PropertiesTests.PUBLIC_KEY, cipher.publicKey());
    }

    @Test
    void rejectsMalformedTamperedAndNonUtf8Ciphertext() throws InvalidCipherTextException {
        assertThrows(Sm2CiphertextException.class, () -> cipher.decrypt(null));
        assertThrows(Sm2CiphertextException.class, () -> cipher.decrypt("00"));
        assertThrows(Sm2CiphertextException.class, () -> cipher.decrypt("0".repeat(193)));
        assertThrows(Sm2CiphertextException.class, () -> cipher.decrypt("z".repeat(192)));

        String validCiphertext = encryptAsBrowserCiphertext("password".getBytes(StandardCharsets.UTF_8));
        char replacement = validCiphertext.endsWith("0") ? '1' : '0';
        String tamperedCiphertext = validCiphertext.substring(0, validCiphertext.length() - 1) + replacement;
        assertThrows(Sm2CiphertextException.class, () -> cipher.decrypt(tamperedCiphertext));

        String nonUtf8Ciphertext = encryptAsBrowserCiphertext(new byte[]{(byte) 0xC3, 0x28});
        assertThrows(Sm2CiphertextException.class, () -> cipher.decrypt(nonUtf8Ciphertext));
    }

    @Test
    void decryptsConcurrentlyWithoutSharingMutableEngineState() throws Exception {
        String ciphertext = encryptAsBrowserCiphertext("parallel-password".getBytes(StandardCharsets.UTF_8));
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try {
            List<Future<String>> results = new ArrayList<>();
            for (int index = 0; index < 64; index++) {
                results.add(executor.submit(() -> cipher.decrypt(ciphertext)));
            }
            for (Future<String> result : results) {
                assertEquals("parallel-password", result.get());
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private String encryptAsBrowserCiphertext(byte[] plaintext) throws InvalidCipherTextException {
        X9ECParameters curve = GMNamedCurves.getByName("sm2p256v1");
        ECDomainParameters domain = new ECDomainParameters(
                curve.getCurve(), curve.getG(), curve.getN(), curve.getH(), curve.getSeed());
        byte[] publicKeyBytes = HexFormat.of().parseHex(Sm2PropertiesTests.PUBLIC_KEY);
        ECPublicKeyParameters publicKey = new ECPublicKeyParameters(
                curve.getCurve().decodePoint(publicKeyBytes), domain);

        SM2Engine engine = new SM2Engine(new SM3Digest(), SM2Engine.Mode.C1C3C2);
        engine.init(true, new ParametersWithRandom(publicKey, new SecureRandom()));
        byte[] engineCiphertext = engine.processBlock(plaintext, 0, plaintext.length);
        // sm2.js 的 cipherMode=1 输出省略非压缩点的 04 前缀。
        return HexFormat.of().formatHex(engineCiphertext, 1, engineCiphertext.length);
    }
}
