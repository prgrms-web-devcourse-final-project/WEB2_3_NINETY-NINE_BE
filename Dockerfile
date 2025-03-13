# 1. JDK 17 기반 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 컨테이너 내부 작업 디렉토리 설정
WORKDIR /app

# 3. 환경 변수 설정 (MariaDB 연결 정보)
ENV SPRING_DATASOURCE_URL=jdbc:mariadb://db:3306/OnCulture?serverTimezone=Asia/Seoul
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=!123456
ENV SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.mariadb.jdbc.Driver

# 4. 로컬의 JAR 파일을 컨테이너 내부로 복사 (Gradle 빌드된 파일 적용)
COPY build/libs/*.jar app.jar

# 5. 컨테이너 실행 시 JAR 파일 실행
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
