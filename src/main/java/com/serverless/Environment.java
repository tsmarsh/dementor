package com.serverless;

public class Environment {
    public static String getVariable(String envar) {
        var foundVar = System.getenv(envar); // or whatever
        if (foundVar == null || foundVar.trim().isEmpty()) {
            throw new IllegalStateException("%s must be set".formatted(envar));
        }
        return foundVar;
    }
}
