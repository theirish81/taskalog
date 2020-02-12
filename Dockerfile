FROM adoptopenjdk/openjdk11-openj9:jdk-11.0.1.13-alpine-slim
RUN mkdir /opt/taskalog
WORKDIR /opt/taskalog
COPY build/libs/taskalog-*-all.jar taskalog.jar
EXPOSE 8080
CMD java -Dcom.sun.management.jmxremote -noverify -Dmicronaut.config.files=etc/application.yml ${JAVA_OPTS} -jar taskalog.jar