package com.contractreview.fileapi.exception;

/**
 * 文件未找到异常
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
public class FileNotFoundException extends FileStorageException {
    
    public FileNotFoundException(String message) {
        super("FILE_NOT_FOUND", message);
    }
    
    public FileNotFoundException(String message, Throwable cause) {
        super("FILE_NOT_FOUND", message, cause);
    }
}