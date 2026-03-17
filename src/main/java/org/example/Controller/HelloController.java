package org.example.Controller;

import org.example.Anotaciones.GetMapping;
import org.example.Anotaciones.RequestParam;
import org.example.Anotaciones.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public static String index() {
        return "Greetings from Spring Boot!";
    }

    @GetMapping("/pi")
    public static String getPi() {
        return "pi" + Math.PI;
    }

    @GetMapping("/hello")
    public static String hellow() {
        return "Hello World!";
    }

    @GetMapping("/hellow")
    public static String hellow2(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hellow" + name;
    }
}