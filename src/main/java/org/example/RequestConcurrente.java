package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RequestConcurrente {
    private final int hilos;
    public ExecutorService executor;

    public RequestConcurrente(int hilos) {
        this.hilos = hilos;
        this.executor = Executors.newFixedThreadPool(hilos);
    }


}
