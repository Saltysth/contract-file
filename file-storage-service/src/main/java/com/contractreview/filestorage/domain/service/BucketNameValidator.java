package com.contractreview.filestorage.domain.service;

import java.util.regex.Pattern;

/**
 * S3 Bucket名称校验器
 * 
 * @author ContractReview Team
 * @version 1.0.0
 */
public class BucketNameValidator {
    
    // S3 bucket名称规则的正则表达式
    private static final Pattern BUCKET_NAME_PATTERN = Pattern.compile(
        "^[a-z0-9][a-z0-9\\-]*[a-z0-9]$"
    );
    
    // IP地址格式的正则表达式（不允许）
    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
        "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}$"
    );
    
    /**
     * 校验bucket名称是否符合Amazon S3标准
     * 
     * @param bucketName bucket名称
     * @return 校验结果
     */
    public static ValidationResult validate(String bucketName) {
        if (bucketName == null || bucketName.trim().isEmpty()) {
            return ValidationResult.error("Bucket名称不能为空");
        }
        
        String name = bucketName.trim();
        
        // 1. 长度检查：3-63个字符
        if (name.length() < 3 || name.length() > 63) {
            return ValidationResult.error("Bucket名称长度必须在3-63个字符之间，当前长度: " + name.length());
        }
        
        // 2. 大写字母检查：不能包含大写字母
        if (!name.equals(name.toLowerCase())) {
            return ValidationResult.error("Bucket名称只能包含小写字母、数字和连字符");
        }
        
        // 3. 连续连字符检查
        if (name.contains("--")) {
            return ValidationResult.error("Bucket名称不能包含连续的连字符");
        }
        
        // 4. 字符检查：只能包含小写字母、数字和连字符
        if (!BUCKET_NAME_PATTERN.matcher(name).matches()) {
            return ValidationResult.error("Bucket名称只能包含小写字母、数字和连字符，且必须以字母或数字开头和结尾");
        }
        
        // 5. IP地址格式检查
        if (IP_ADDRESS_PATTERN.matcher(name).matches()) {
            return ValidationResult.error("Bucket名称不能是IP地址格式");
        }
        
        // 6. 特殊前缀检查
        if (name.startsWith("xn--")) {
            return ValidationResult.error("Bucket名称不能以'xn--'开头");
        }
        
        // 7. 特殊后缀检查
        if (name.endsWith("-s3alias") || name.endsWith("--ol-s3")) {
            return ValidationResult.error("Bucket名称不能以'-s3alias'或'--ol-s3'结尾");
        }
        
        return ValidationResult.success("Bucket名称校验通过", name);
    }
    
    /**
     * 标准化bucket名称（转为小写）
     */
    private static String normalizeBucketName(String bucketName) {
        return bucketName.toLowerCase();
    }
    
    /**
     * 校验结果类
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String normalizedName;
        
        private ValidationResult(boolean valid, String message, String normalizedName) {
            this.valid = valid;
            this.message = message;
            this.normalizedName = normalizedName;
        }
        
        public static ValidationResult success(String message, String normalizedName) {
            return new ValidationResult(true, message, normalizedName);
        }
        
        public static ValidationResult error(String message) {
            return new ValidationResult(false, message, null);
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        public String getNormalizedName() {
            return normalizedName;
        }
    }
}