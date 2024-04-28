package utc.k61.cntt2.class_management.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/api/files")
public class FileController {

    @GetMapping(value = "/sample/{fileName}")
    public void getSampleCsv(HttpServletResponse response, @PathVariable String fileName) throws Exception {
        String filePath = "temp/" + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            // If the file doesn't exist, return a 404 error response
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        FileInputStream fileIn = new FileInputStream(file);
        response.setContentType("application/octet-stream");
        response.setHeader("content-disposition", "attachment; filename=" + file.getName());
        response.setContentLength((int) file.length());
        ServletOutputStream out = response.getOutputStream();

        int bytesRead;
        byte[] buffer = new byte[4096];
        while ((bytesRead = fileIn.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        fileIn.close();
        out.flush();
        out.close();
    }
}
