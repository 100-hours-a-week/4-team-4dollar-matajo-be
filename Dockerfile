# JDK 17 베이스 이미지 사용
FROM eclipse-temurin:17-jre-alpine

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY build/libs/*.jar app.jar

# 애플리케이션 실행 시 사용할 프로파일 설정을 위한 환경변수
ENV SPRING_PROFILES_ACTIVE=prod,aws

# 메모리 설정 최적화
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 컨테이너 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar --spring.profiles.active=$SPRING_PROFILES_ACTIVE"]