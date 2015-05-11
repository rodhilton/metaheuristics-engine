package com.rodhilton.metaheuristics.util;

import java.util.HashSet;

public class PropUtil {
    static HashSet<String> printedProperties = new HashSet<String>();

    public static String get(String name, String def) {
        String value = System.getProperty(name);
        boolean defaulted = false;
        if(value == null) {
            defaulted = true;
            value = def;
        }

        if(!printedProperties.contains(name)) {
            printedProperties.add(name);
            System.out.println("[Looked up "+name+", using "+value+" ("+(defaulted ? "default" : "read")+")]");
        }

        return value;
    }
}
