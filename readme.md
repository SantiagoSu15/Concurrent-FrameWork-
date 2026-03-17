# MicroSpringBoot — Servidor Web con IoC en Java

Un servidor web construido desde cero en Java puro, sin dependencias externas, con un framework IoC (Inversión de Control) que carga y registra automáticamente componentes web (POJOs) usando reflexión de Java.
Con concurrencia y desplgiegue en AWS
---

## Arquitectura

**Estructura del proyecto**
```
src/main/java/org/example/
├── MicroSpringBoot.java          → Núcleo: IoC, escaneo de anotaciones
├── HtppServer.java               → Revisa query, crea conexion y devuelve html
├── RequestConcurrente.java       → Crea pool de hilos para cada request
├── controller/
│   └── HelloController.java     → Componente web de ejemplo
├── anotaciones/
│   ├── RestController.java      → Marca una clase como componente web
│   ├── GetMapping.java          → Mapea un método a una ruta GET
│   └── RequestParam.java        → Inyecta parámetros de query string
└── 

```

### Flujo 
```
MicroSpringBoot.main(args)
    │
    ▼
Escanea el classpath buscando clases con @RestController
Despues del escaneo llama a HtppServer.start(port)
HttpServer crea una instancia de RequestConcurrente para cada request
    │
    ▼
Por cada request  se le asigana un hilo encontrado que realizara un metodo para buscar las querys si existen y devolver lo necesario
```

---

## Requisitos

- **Java 21**
- **Maven 3.6** o superior
- **Docker**

Verificar instalación:
```bash
java -version
mvn -version
```

---

## Instalación y ejecución

### 1. Clonar el repositorio
```bash
git clone https://github.com/SantiagoSu15/Concurrent-FrameWork-.git
```

### 2. Compilar
```bash
mvn compile
mvn clean package
```

### 3. Construir imagen
```bash
docker build -t lab7-web .
```

### 4. Levantar contenedores
```bash
docker compose up
```



---

## Uso del framework

### Definir un componente web

Anotar la clase con `@RestController` y sus métodos con `@GetMapping`:
```java
@RestController
public class HelloController {

    @GetMapping("/")
    public String index() {
        return "Greetings from Spring Boot!";
    }
}
```

### Usar parámetros de query string con `@RequestParam`
```java
@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }
}
```
### Funcionamiento 

Para manejar solicitudes concurrentes se ha decidido usar un pool de hilos para cada request.
La clase `RequestConcurrente` se encarga de crear y gestionar los hilos.
en esta se crea un `ExecutorService` con un pool de hilos que decida el http server

En httpServer el metodo que lo comienza lo llama
```

while (true) {
            Socket client = serverSocket.accept();
            requestConcurrente.executor.execute(() -> {
        try {
            handleRequest(client);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        });
}
```

Se decide usar execute ya que se devuelve un void y no se espera nada
El hilo principal se encarga de hacer las conexiones y por cada conexion el executor asigna un hilo del pool para esta, apenas esta termine su proceso el hilo regresa al pool

Para poder apagar se usa `shutdown hook` que es un hilo que se ejecuta cuando el proceso se detiene este se registra en Runtime 
dentro de este se crea un hilo que se encarga de cerrar el serverSocket y el executorService, de esta manera espera que la tarea de los hilos termine antes de cerrar el proceso

```
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Apagando sv");
            try {
                serverSocket.close();
                requestConcurrente.executor.shutdown();
                if (!requestConcurrente.executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    requestConcurrente.executor.shutdownNow();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }));
```

### Docker

Se creo un Dockerfile para poder ejecutar el proyecto en un contenedor docker

```dockerfile
FROM eclipse-temurin:21-jdk

WORKDIR /usrapp/bin
ENV PORT=6000

COPY /target/classes /usrapp/bin/classes
COPY src /usrapp/bin/src


CMD ["java","-cp","classes","org.example.MicroSpringboot"]
```
Usando el jdk 21 solo se copian la carpeta de target clases y src dado que  no hay dependencias externas

Adicionalmente se creo un docker compose con la imagen y una DB de mongo 

```yaml
version: '2'

services:
  web:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: web
    ports:
      - "8087:6000"

  db:
    image: mongo:3.6.1
    container_name: db
    volumes:
      - mongodb:/data/db
      - mongodb_config:/data/configdb
    ports:
      - "27017:27017"
    command: mongod

volumes:
  mongodb:  
  mongodb_config:
```

Se levantan los contenedores con el comando `docker compose up`
Se puede acceder a la aplicacion en el puerto `http://localhost:8087`



---
# Desplegue


### Subir imagen a Docker hub

Tras construir la imagen docker se sube a docker hub

**Revisar imagenes de docker** 
![Subir](/docs/img.png)

**Agregarle un tag**
![Tag](/docs/img_1.png)

**Hacer push al repo de docker**
![Push](/docs/img_2.png)

**Revisar en docker hub**
![Docker](/docs/img_3.png)

### Subir a AWS

Tras la creacion de un EC2 en aws se crea un grupo de seguridad para permitir el trafico desde el puerto 42000
En bash se sube la imagen a la instancia

**Hacer pull de la imagen**
![Subir](/docs/img_4.png)

**Revisar en aws**
![AWS](/docs/img_5.png)

**correr la imagen de docker**
![AWS](/docs/img_6.png)

Se corre la imagen con el comando ` docker run -p 42000:6000 --name frameworkMini santiagosu15/tdse-lab-7`
donde el puerto 42000 es el puerto que se le asigna a la instancia en aws y el nombre es el nombre que se le da a la instancia
la imagen correra en el puerto 6000 dentro de la instancia

**Video prueba del despliegue**
![AWS](/docs/pruebaAWS.mp4)

Se puede acceder a la aplicacion con el DNS publico de aws en el puerto 42000 

## Construido con

- **Java** — Lenguaje principal
- **Maven** — Gestión de dependencias y build
- **Docker** - Imagenes
- **AWS** - Servidor de aplicaciones
---

## Autor

* **Santiago Suarez** — [SantiagoSu15](https://github.com/SantiagoSu15)