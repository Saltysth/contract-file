package com.contractreview.filestorage.domain.model.valueobject;

import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * 文件UUID值对象
 * 格式：{timestamp}-{randomString}
 * 示例：20240921143022-a8b9c1d2
 * 
 * @author ContractReview Team
 */
@Value
public class AttachmentUuid {
    
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String RANDOM_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static final int RANDOM_LENGTH = 8;
    private static final Random RANDOM = new Random();

    String value;

    private AttachmentUuid(String value) {
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("UUID不能为空");
        }
        if (!isValidFormat(value)) {
            throw new IllegalArgumentException("UUID格式错误，正确格式：yyyyMMddHHmmss-xxxxxxxx");
        }
        this.value = value;
    }

    /**
     * 创建UUID值对象
     */
    public static AttachmentUuid of(String value) {
        return new AttachmentUuid(value);
    }

    /**
     * 生成新的UUID
     */
    public static AttachmentUuid generate() {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        String randomString = generateRandomString();
        return new AttachmentUuid(timestamp + "-" + randomString);
    }

    /**
     * 验证UUID格式
     */
    private static boolean isValidFormat(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            return false;
        }
        
        String[] parts = uuid.split("-");
        if (parts.length != 2) {
            return false;
        }
        
        // 验证时间戳部分（14位数字）
        String timestamp = parts[0];
        if (timestamp.length() != 14 || !timestamp.matches("\\d{14}")) {
            return false;
        }
        
        // 验证随机字符串部分（8位字母数字）
        String randomPart = parts[1];
        return randomPart.length() == RANDOM_LENGTH && randomPart.matches("[a-z0-9]{8}");
    }

    /**
     * 生成随机字符串
     */
    private static String generateRandomString() {
        StringBuilder sb = new StringBuilder(RANDOM_LENGTH);
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            sb.append(RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 解析UUID获取时间戳
     */
    public LocalDateTime getTimestamp() {
        String timestampStr = value.split("-")[0];
        return LocalDateTime.parse(timestampStr, TIMESTAMP_FORMATTER);
    }

    /**
     * 获取随机字符串部分
     */
    public String getRandomPart() {
        return value.split("-")[1];
    }

    @Override
    public String toString() {
        return value;
    }
}