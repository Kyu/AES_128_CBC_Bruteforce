# AES_128_CBC_Bruteforce
Takes plaintext, ciphertext, and iv, returns a key from a dictionary padded by '#'  
Dictionary source: https://github.com/dwyl/english-words/blob/master/words_alpha.txt

## Build  
`./gradlew jar`

## Run
Example: 
```sh
echo -n "764aa26b55a4da654df6b19e4bce00f4ed05e09346fb0e762583cb7da2ac93a2" | read cipher_text_hex
echo -n "aabbccddeeff00998877665544332211" | read iv_hex
echo -n "This is a top secret." | read plain_text
java -jar AES_128_CBC_Bruteforce-1.0_SNAPSHOT.jar --plaintext "$plain_text" --cipher_text_hex "$cipher_text_hex" --iv_hex "$iv_hex"
```

`--pad "#"` is an optional flag
