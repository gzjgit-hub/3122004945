package org.example.exceptions;

import java.security.NoSuchAlgorithmException;

/**
 * MD5算法hash异常
 */
public class HashException extends NoSuchAlgorithmException {
    public HashException(String message) {
        super(message);
    }
}