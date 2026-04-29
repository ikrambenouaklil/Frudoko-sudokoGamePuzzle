package com.frudoko;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import java.io.File;

public class Main {
    public static void main(String[] args) throws Exception {
        String port = System.getenv("PORT");
        if (port == null) port = "8080";

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.parseInt(port));
        tomcat.getConnector();

        // على Railway الكود في /app/target/FrudokoGame
        String webappPath = new File("target/FrudokoGame").getAbsolutePath();
        Context ctx = tomcat.addWebapp("", webappPath);

        tomcat.start();
        tomcat.getServer().await();
    }
}