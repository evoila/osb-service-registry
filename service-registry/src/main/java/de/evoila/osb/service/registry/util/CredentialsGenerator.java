package de.evoila.osb.service.registry.util;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class CredentialsGenerator {

    public static final int DEFAULT_CREDENTIALS_LENGTH = 8;
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz01234567489";

    private static final Random random = new Random();

    public static int usernameLength = DEFAULT_CREDENTIALS_LENGTH;
    public static int passwordLength = DEFAULT_CREDENTIALS_LENGTH;

    public static String randomUsername(Set<String> takenUsernames) {
        return randomAlphaNumericString(usernameLength, takenUsernames);
    }

    public static  String randomPassword() {
        return randomAlphaNumericString(passwordLength, null);
    }

    private static  String randomAlphaNumericString(int length, Set<String> takenStrings) {
        length = length < DEFAULT_CREDENTIALS_LENGTH ? DEFAULT_CREDENTIALS_LENGTH : length;
        Set<String> taken = takenStrings == null ? new HashSet<>() : takenStrings;
        StringBuilder builder = new StringBuilder();
        while (builder.toString().length() < 1) {
            for (int i = 0; i < length; i++) {
                builder.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            if (taken.contains(builder.toString()))
                builder = new StringBuilder();
        }
        return builder.toString();
    }
}
