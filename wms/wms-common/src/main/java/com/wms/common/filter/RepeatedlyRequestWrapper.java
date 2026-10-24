package com.wms.common.filter;

import cn.hutool.core.io.IoUtil;
import com.wms.common.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 构建可重复读取inputStream的request
 *
 * @author ruoyi
 */
@Slf4j
public class RepeatedlyRequestWrapper extends HttpServletRequestWrapper {
    private byte[] body = new byte[0];

    public RepeatedlyRequestWrapper(HttpServletRequest request, ServletResponse response) throws IOException {
        super(request);
        request.setCharacterEncoding(Constants.UTF8);
        response.setCharacterEncoding(Constants.UTF8);
        body = IoUtil.readBytes(request.getInputStream(), false);
//        try {
//            // 添加连接状态检查
//            if (request.getInputStream() == null || request.getContentLength() <= 0) {
//                body = new byte[0];
//            } else {
//                // 添加超时控制和异常处理
//                body = IoUtil.readBytes(request.getInputStream(), false);
//            }
//        } catch (ClientAbortException e) {
//            log.info("ContentLength: {}", request.getContentLength());
//            // 记录已读取的部分内容
//            String partialBody = body != null ? new String(body, StandardCharsets.UTF_8) : "";
//            log.error("Client aborted connection. Partial request body: {}", partialBody);
//            // 记录日志并返回空body
//            log.error("Client aborted connection", e);
//            throw new RuntimeException(e);
//        }
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream bais = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                return bais.read();
            }

            @Override
            public int available() throws IOException {
                return body.length;
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }
        };
    }
}
