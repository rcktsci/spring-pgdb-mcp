FROM rcktsci/java-tooling:25

WORKDIR /application

COPY target/*.jar /application/spring-pgdb-mcp.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "spring-pgdb-mcp.jar"]
