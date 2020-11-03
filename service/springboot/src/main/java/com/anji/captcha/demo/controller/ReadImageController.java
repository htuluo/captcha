package com.anji.captcha.demo.controller;

import com.sun.deploy.net.HttpResponse;
import org.springframework.http.HttpRequest;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

/**
 * @author ： luoleiming
 * @date ：Created in 2020/10/30
 * @description： //TODO
 */
@RestController
public class ReadImageController {

    @RequestMapping(value = "get",produces = {MediaType.IMAGE_PNG_VALUE,MediaType.IMAGE_JPEG_VALUE})
    @ResponseBody()
    public void readImage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        File file = new File("D:/srcImg.png");
        byte[] bytes = FileCopyUtils.copyToByteArray(file);

//        response=((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        response.setContentType("image/png");
        OutputStream stream=response.getOutputStream();
        stream.write(null);
        stream.flush();
        stream.close();

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

    }
}
