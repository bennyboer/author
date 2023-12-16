package de.bennyboer.author.auth.keys;

public class KeyPairs {

    public static KeyPair read(String path) {
        return KeyReader.readKeyPair(path).block();
    }

}
