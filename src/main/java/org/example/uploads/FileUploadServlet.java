package org.example.uploads;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

@WebServlet("/fileUploadServlet")
@Slf4j
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 20,
        maxRequestSize = 1024 * 1024 * 50
)

public class FileUploadServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "src/main/webapp/uploads";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            try{
                if(uploadDir.mkdirs()){
                    log.info("Successfully created directory!");
                }
            }
            catch (Exception e){
                log.error("Error creating directory: {}", e.getMessage());
            }
        }

        try {
            Part filePart = request.getPart("file");
            String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();

            if (!isValidFileType(fileName)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Unsupported file type: " + getFileExtension(fileName));
                log.warn("Unsupported file type: {}", fileName);
                return;
            }

            File file = new File(UPLOAD_DIR, fileName);
            try (InputStream input = filePart.getInputStream()) {
                Files.copy(input, file.toPath());
            }

            String downloadLink = "/files/" + fileName;
            response.getWriter().write("File uploaded successfully. Download URL: " + downloadLink);
            log.info("File uploaded: {}", fileName);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("File upload failed");
            log.error("File upload failed: {}", e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fileName = request.getPathInfo().substring(1);
        File file = new File(UPLOAD_DIR, fileName);

        if (!file.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("File not found");
            log.warn("File not found: {}", fileName);
            return;
        }

        response.setContentType(Files.probeContentType(file.toPath()));
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        try (OutputStream out = response.getOutputStream(); FileInputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }

        log.info("File served: {}", fileName);
    }

    private boolean isValidFileType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        return extension.equals("txt") || extension.equals("jpg") || extension.equals("png");
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "" : fileName.substring(lastDot + 1);
    }
}
