# 1. Gradle 빌드 스테이지
FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /app

# Gradle 빌드에 필요한 모든 파일 복사
COPY . .

# Gradle 캐시를 저장할 디렉토리를 생성하고 권한 설정
RUN mkdir -p /home/gradle/.gradle && chmod -R 777 /home/gradle/.gradle

# Gradle Wrapper 실행하여 빌드 수행
RUN ./gradlew build -x test

# 2. 실행 스테이지 (JRE만 포함)
FROM eclipse-temurin:17-jre-focal
WORKDIR /app

# 환경 변수 설정 (MariaDB 연결 정보)
ENV SPRING_DATASOURCE_URL=jdbc:mariadb://db:3306/OnCulture?serverTimezone=Asia/Seoul
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=!123456
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

