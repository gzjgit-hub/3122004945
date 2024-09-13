package org.example.exceptions;

import java.io.FileNotFoundException;

/**
 * 找不到文件的文件解析异常
 */
public class NotExistFileException extends FileNotFoundException {
    public NotExistFileException(String message) {
        super(message);
    }
}