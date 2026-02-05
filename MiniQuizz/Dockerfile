
# Seleccionamos la imagen para compilar el proyecto 
FROM maven:3.6.3-openjdk-14-slim AS build 

# Copiamos el pom.xml y el código fuente
COPY src /home/app/src
COPY pom.xml /home/app

# compilamos el proyecto
RUN mvn -f /home/app/pom.xml clean package

# Cargamos la JRE que ejecutará el proyecto
FROM openjdk:14-alpine

# Copiamos la aplicación dentro del contenedo
COPY --from=build "home/app/target/AdUd5A3_MVC_Directores-0.0.1-SNAPSHOT.jar" "app.jar"

# Puerto de escucha
EXPOSE 8080

# Programa que ejecutamos
ENTRYPOINT ["java","-jar","app.jar"]

# Para crearlo
# docker build -t jgce/spring_mvc:1.0 .