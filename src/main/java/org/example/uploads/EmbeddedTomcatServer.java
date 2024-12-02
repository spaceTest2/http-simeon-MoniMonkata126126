package org.example.uploads;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import java.io.File;

@Slf4j
public class EmbeddedTomcatServer {

    public static void main(String[] args) throws LifecycleException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        tomcat.setHostname("localhost");
        tomcat.getConnector().setMaxPostSize(1024 * 1024 * 20);
        tomcat.getConnector().setMaxSavePostSize(1024 * 1024 * 20);

        String webAppDir = new File("src/main/webapp").getAbsolutePath();
        Context context = tomcat.addWebapp("/", webAppDir);
        context.setAllowCasualMultipartParsing(true);

        Tomcat.addServlet(context, "fileUploadServlet", new FileUploadServlet());
        context.addServletMappingDecoded("/upload", "fileUploadServlet");
        context.addServletMappingDecoded("/files/*", "fileUploadServlet");

        try {
            log.info("Starting Tomcat...");
            tomcat.start();
            log.info("Tomcat started!");
            tomcat.getServer().await();
        }
        catch (Exception e){
            log.error("Error starting Tomcat: {}", e.getMessage());
        }
    }
}