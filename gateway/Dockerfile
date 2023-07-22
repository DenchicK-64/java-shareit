FROM amazoncorretto:11
#ENV TZ=Europe/Samara
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8081
COPY target/*.jar shareit-gateway.jar
ENTRYPOINT ["java","-jar","/shareit-gateway.jar"]