package com.skyspireinfotech.admin.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Uploads over spring.servlet.multipart.max-file-size are rejected before any
 * controller runs; send the admin back to the page they came from with a
 * readable message instead of an error page.
 */
@ControllerAdvice
public class UploadLimitAdvice {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge(HttpServletRequest request) {
        FlashMap flash = RequestContextUtils.getOutputFlashMap(request);
        flash.put("mediaError", "That file is too large. Images: max 8 MB, videos: max 50 MB.");
        flash.put("error", "That file is too large.");
        String path = request.getServletPath();
        return path.startsWith("/profile") ? "redirect:/profile" : "redirect:/home";
    }
}
