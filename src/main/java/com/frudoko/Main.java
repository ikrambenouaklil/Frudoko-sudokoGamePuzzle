package com.frudoko;

import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.Context;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import java.io.File;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws Exception {
        String port = System.getenv("PORT");
        if (port == null) port = "8080";

        Tomcat tomcat = new Tomcat();
        tomcat.setPort(Integer.parseInt(port));
        tomcat.getConnector();

        // نشوف وين الـ JAR موجود
        String jarDir;
        try {
            jarDir = new File(Main.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            jarDir = "target";
        }

        // نستخدم مجلد مؤقت كـ docBase
        File docBase = new File(System.getProperty("java.io.tmpdir"));
        Context ctx = tomcat.addWebapp("", docBase.getAbsolutePath());

        // نحمّل الـ resources من داخل الـ JAR
        WebResourceRoot resources = new StandardRoot(ctx);
        resources.addPreResources(new DirResourceSet(
                resources,
                "/",
                new File(jarDir).getAbsolutePath(),
                "/"
        ));
        ctx.setResources(resources);

        tomcat.start();
        tomcat.getServer().await();
    }
}