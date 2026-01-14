# 1. 选择基础镜像
FROM eclipse-temurin:17.0.17_10-jre-ubi10-minimal

# 2. 维护者信息（可选）
LABEL maintainer="SaltyFish <contact@contract-file.com>"

# 3. 设置容器内的工作目录
WORKDIR /app

# 4. 复制本地打包好的JAR包到容器的/app目录下（注意JAR包名要和target里的一致）
# 若JAR包在target目录，这里用相对路径；如果是自定义路径，调整COPY的源路径
COPY file-storage-service/target/file-storage-service-0.0.1.jar /app/file-storage-service-0.0.1.jar

# 5. 暴露服务端口（必须和SpringBoot配置的server.port一致，比如8080）
EXPOSE 10000

# 6. （关键）创建文件存储目录（避免容器启动后目录权限问题）
RUN mkdir -p /app/file-storage && chmod 777 /app/file-storage

# 7. 启动命令（添加JVM参数优化，适配文件服务的内存需求）
ENTRYPOINT ["java", "-jar", "/app/file-storage-service-0.0.1.jar"]