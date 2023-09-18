package com.preciouso;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    @Parameter(names={"--plain_text", "--plaintext", "-p"}, required = true, description = "The plain text.")
    private String plainText;
    private byte[] plainTextBytes;

    @Parameter(names={"--cipher_text", "--cipher_text_hex", "--ciphertext", "-c"}, required = true, description = "A hex representation of the ciphertext.")
    private String cipherText;
    private byte[] cipherTextHex;

    @Parameter(names={"--iv", "--iv_hex"}, required = true, description = "The IV of the cipher.")
    String iv;
    private byte[] ivHex;

    @Parameter(names={"--pad", "--padding"}, description = "String to pad the key with if it's too short.")
    private String padString = "#";

    private static final ArrayList<String> wordlist = new ArrayList<>();
    private final Cipher cipher;

    public Main(String[] args) {
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
        JCommander parser = JCommander.newBuilder()
                .addObject(this)
                .build();

        try {
            parser.parse(args);
        } catch (ParameterException ex) {
            parser.usage();
            System.exit(0);
        }

        System.out.println("Given: ");

        System.out.println("Plaintext:\t\t" + plainText);
        plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);

        System.out.println("Ciphertext:\t\t" + cipherText);
        cipherTextHex = HexFormat.of().parseHex(cipherText);

        System.out.println("iv:\t\t\t" + iv);
        ivHex = HexFormat.of().parseHex(iv);

        System.out.println("Padding:\t\t" + padString);
        System.out.println();

        System.out.println("Trying both regular and title case of each dictionary word");
        System.out.println("Dictionary size: " + wordlist.size());
        System.out.println();

        cipherTextHex = HexFormat.of().parseHex(cipherText);
    }

    public static void loadWordList() throws IOException {
        // Wordlist from: https://github.com/dwyl/english-words
        InputStream is = Main.class.getResourceAsStream("/words_alpha.txt");
        assert is != null;

        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(streamReader);
        for (String line; (line = reader.readLine()) != null; ) {
            wordlist.add(line);
        }
    }

    public static void main(String[] args) throws IOException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        loadWordList();

        Main main = new Main(args);
        main.run();
    }

    private void run() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        int index = 0;
        int skipped = 0;

        for (String s: wordlist) {
            String keyAttempt = padWord(s);
            String keyTitled = keyAttempt.substring(0, 1).toUpperCase() + keyAttempt.substring(1);

            if (keyAttempt.length() <= 16) {
                if (tryKey(keyAttempt)) {
                    done(keyAttempt, index, skipped);
                    break;
                }
                if (tryKey(keyTitled)) {
                    done(keyTitled, index, skipped);
                    break;
                }
            } else {
                skipped += 1;
            }
            index += 1;
        }

        System.out.println("Done: " + index);
    }

    private void done(String key, int index, int skipped) {
        System.out.println("Found key: " + key);
        System.out.println("Dictionary index: " + index);
        System.out.println("Skipped keys for being > 16: " + skipped);
    }
    private boolean tryKey(String key) throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        SecretKeySpec aesKeySpec = new SecretKeySpec(keyBytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, new IvParameterSpec(ivHex));

        byte[] enc = cipher.doFinal(plainTextBytes);

        // System.out.println(Arrays.toString(enc));
        return Arrays.equals(cipherTextHex, enc);
    }

    private String padWord(String inString) {
        while (inString.length() < 16) {
            inString = inString.concat(padString);
        }

        return inString;
    }
}
