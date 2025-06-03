package org.web.codefm.api.controller;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

  private final ErrorAttributes errorAttributes;

  public CustomErrorController(ErrorAttributes errorAttributes) {
    this.errorAttributes = errorAttributes;
  }
  
  @RequestMapping("/error")
  public ResponseEntity<Map<String, Object>> handleError(WebRequest webRequest) {
    Throwable error = errorAttributes.getError(webRequest);
    Map<String, Object> errorDetails = new HashMap<>();
    errorDetails.put("message", error.getMessage());
    HttpStatus status = HttpStatus.valueOf((int) errorDetails.getOrDefault("status", 500));
    
    return new ResponseEntity<>(errorDetails, status);
  }


  
  
}