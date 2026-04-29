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

        // نشوف الـ WAR في نفس مكان الـ JAR
        String warPath = new File("target/FrudokoGame.war").getAbsolutePath();
        Context ctx = tomcat.addWebapp("", warPath);

        tomcat.start();
        tomcat.getServer().await();
    }
}