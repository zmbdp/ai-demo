package com.zmbdp.ai.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

@Slf4j
@RestController
@RequestMapping("/sse")
public class SSEController {

    @RequestMapping("/data")
    public void data(HttpServletResponse response) throws IOException, InterruptedException {
        log.info("SSEController.data");
        response.setContentType("text/event-stream;charset=utf-8");
        PrintWriter writer = response.getWriter();
        for (int i = 0; i < 20; i++) {
            String result = "event: foo\n";
            result += "data: " + new Date() + "\n\n";
            writer.write(result);
            writer.flush();
            Thread.sleep(1000L);
        }
        writer.write("event: end\ndata: EOF\n\n");
        writer.flush();
    }
}
