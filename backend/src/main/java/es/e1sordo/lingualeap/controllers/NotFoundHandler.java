package es.e1sordo.lingualeap.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.nio.charset.Charset;

@Slf4j
@ControllerAdvice
public class NotFoundHandler {
    @ExceptionHandler({ NoHandlerFoundException.class, NoResourceFoundException.class })
    public ResponseEntity<String> renderDefaultPage() {
        try {
            final Resource indexResource = new ClassPathResource("public/index.html");
            final String body = StreamUtils.copyToString(indexResource.getInputStream(), Charset.defaultCharset());
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(body);
        } catch (IOException e) {
            log.error("Could not load index.html", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("There was an error completing the action.");
        }
    }
}
