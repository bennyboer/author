package de.bennyboer.author.auth.keys;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.InputStreamReader;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.util.Optional;

public class KeyReader {

    public static Mono<KeyPair> readKeyPair(String path) {
        Security.addProvider(new BouncyCastleProvider());

        return loadKeyPair(path)
                .flatMap(KeyReader::convertKeyPair)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private static Mono<KeyPair> convertKeyPair(PEMKeyPair pemKeyPair) {
        return Mono.fromCallable(() -> {
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            java.security.KeyPair javaKeyPair = converter.getKeyPair(pemKeyPair);

            return KeyPair.of(
                    (ECPublicKey) javaKeyPair.getPublic(),
                    (ECPrivateKey) javaKeyPair.getPrivate()
            );
        });
    }

    private static Mono<PEMKeyPair> loadKeyPair(String path) {
        return Mono.fromCallable(() -> {
            try (
                    var parser = new PEMParser(new InputStreamReader(
                            Optional.ofNullable(KeyReader.class.getResourceAsStream(path)).orElseThrow()
                    ))
            ) {
                return (PEMKeyPair) parser.readObject();
            }
        });
    }

}
