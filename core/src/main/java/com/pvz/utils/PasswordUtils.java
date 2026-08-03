package com.pvz.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class PasswordUtils {
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] encodedHash = digest.digest(password.getBytes());

            return HexFormat.of().formatHex(encodedHash);
        } catch (NoSuchAlgorithmException e){
            throw new RuntimeException("SHA-256 algorithm not found!", e);
        }
    }

    public static boolean verifyPassword(String inputPassword, String storedHash) {
        String inputHash = hashPassword(inputPassword);
        return inputHash.equals(storedHash);
    }

}
