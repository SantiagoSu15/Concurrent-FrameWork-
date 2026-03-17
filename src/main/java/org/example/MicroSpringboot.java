package org.example;

import com.sun.tools.javac.Main;
import org.example.Anotaciones.GetMapping;
import org.example.Anotaciones.RequestParam;
import org.example.Anotaciones.RestController;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class MicroSpringboot {

    public static Map<String, Method> controlerMethods = new HashMap<>();
    public static Map<String, Object> controllerInstances = new HashMap<>();


    public static void main(String[] args) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, IOException, NoSuchMethodException, InstantiationException {
        System.out.println("Loading rest controllers and their methods:");


        Path root = Paths.get(System.getProperty("user.dir"));
        Path srcFolder = Files.list(root)
                .filter(p -> p.getFileName().toString().equals("src"))
                .findFirst()
                .orElse(null);

        if (srcFolder != null) {
            Path mainJavaPath = srcFolder.resolve("main/java/org");
            if (Files.exists(mainJavaPath)) {
                Stream<Path> stream = Files.walk(mainJavaPath);
                List<Path> todosLosArchivos = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".java"))
                        .toList();

                List<String> stringFiles = todosLosArchivos.stream().map(Path :: toString)
                        .map(sPath -> {
                            int index = sPath.indexOf("org");
                            if (index != -1) {
                                String nuevo = sPath.substring(index);
                                return nuevo.replace("\\", ".")
                                        .replace("/", ".")
                                        .substring(0, nuevo.length() - 5);
                            }
                            return sPath;
                        }).toList();

                //stringFiles.forEach(System.out::println);

                for(String claseString :stringFiles ){
                    Class<?> c = Class.forName(claseString); //instancia de la clase

                    if(c.isAnnotationPresent(RestController.class)){
                        System.out.println("Found a rest controller");
                        Object instance = c.getDeclaredConstructor().newInstance();
                        for (Method m : c.getMethods()) {
                            if (m.isAnnotationPresent(GetMapping.class)) {
                                GetMapping a = m.getAnnotation(GetMapping.class);
                                String path = a.value();
                                controlerMethods.put(path, m);
                                controllerInstances.put(path, instance);
                            }
                        }
                    }
                }



                HttpServer server = new HttpServer(getPort(), controlerMethods, controllerInstances);
                server.start();

            }
        }
    }

    private static int getPort() {
        if (System.getenv("PORT") != null) {
            return Integer.parseInt(System.getenv("PORT"));
        }
        return 35000;
    }
}
