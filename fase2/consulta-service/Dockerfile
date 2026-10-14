# Etapa de compilación (multi-stage build)
FROM eclipse-temurin:21-jdk AS build
WORKDIR /source

# Copiar el proyecto y otorgar permisos de ejecución
COPY mvnw .
COPY pom.xml .
RUN chmod +x mvnw

# Copiar el código fuente (excluyendo archivos innecesarios)
COPY src ./src

# Compilar sin ejecutar tests
RUN ./mvnw -q package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el JAR generado desde la etapa de compilación
COPY --from=build /source/target/consulta-service-1.0.0.jar app.jar

# Exponer el puerto del microservicio
EXPOSE 8081

# Comando de entrada
ENTRYPOINT ["java", "-jar", "app.jar"]