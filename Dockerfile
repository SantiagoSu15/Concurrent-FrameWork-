# Version de la vm
FROM eclipse-temurin:21-jdk

WORKDIR /usrapp/bin
ENV PORT=6000

# Copiar archivos de x a y
COPY /target/classes /usrapp/bin/classes
COPY src /usrapp/bin/src

#COPY /target/dependency /usrapp/bin/dependency

# comando para levantar
CMD ["java","-cp","classes","org.example.MicroSpringboot"]