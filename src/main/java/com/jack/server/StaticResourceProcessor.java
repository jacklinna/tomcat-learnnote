package com.jack.server;

import com.jack.server.startup.BootStrap;
import com.jack.server.https.requests.HttpRequestImpl;
import com.jack.server.https.responses.HttpResponseImpl;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class StaticResourceProcessor {
    private static final int BUFFER_SIZE = 1024;    //下面的字符串是当文件没有找到时返回的404错误描述
    private static String fileNotFoundMessage = "HTTP/1.1 404 File Not Found\r\n"
            +            "Content-Type: text/html\r\n"
            +            "Content-Length: 23\r\n"
            +            "\r\n"
            +            "<h1>File Not Found</h1>";
    //下面的字符串是正常情况下返回的，根据http协议，里面包含了相应的变量。
    private static String OKMessage = "HTTP/1.1 ${StatusCode} ${StatusName}\r\n"
            +            "Content-Type: ${ContentType}\r\n"
            +            "Content-Length: ${ContentLength}\r\n"
            +            "Server: minit\r\n"
            +            "Date: ${ZonedDateTime}\r\n"
            +            "\r\n";

    //这个处理数据肚饿过程，就是 先将响应头 写入 输出流，然后从文件读取内容写入 输出流的响应体位置

    public void process(HttpRequestImpl request, HttpResponseImpl response) throws IOException {
        byte[] bytes = new byte[BUFFER_SIZE];

        FileInputStream fis = null;
        OutputStream outputStream = null;

        try{
            outputStream = response.getoutput();
            File file = new File(BootStrap.WEB_ROOT,request.getUri());

            if(file.exists()){

                String head = composeResponseHead(file);
                outputStream.write(head.getBytes("utf-8"));
                //写入了 响应头，就可以 读取文件内容
                //这一步很关键，之前漏了这一个

                //之后就是固定的输出环节
                fis = new FileInputStream(file);
                int ch = fis.read(bytes, 0, BUFFER_SIZE);
                while (ch != -1) {
                    outputStream.write(bytes, 0, ch);
                    ch = fis.read(bytes, 0, BUFFER_SIZE);
                }
                outputStream.flush();

            }else{
                outputStream.write(fileNotFoundMessage.getBytes());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(fis != null){
                fis.close();
            }
        }
    }

    private String composeResponseHead(File file) {
        long fileLength = file.length();

        Map<String,Object> valuesMap = new HashMap<String, Object>();

        valuesMap.put("StatusCode","200");
        valuesMap.put("StatusName","OK");
        valuesMap.put("ContentType","text/html;charset=utf-8");
        valuesMap.put("ContentLength",fileLength);
        valuesMap.put("ZonedDateTime", DateTimeFormatter.ISO_ZONED_DATE_TIME.format(ZonedDateTime.now()));

        //这个StrSubstitutor工具类就是使用正则将占位符替换成 对应的数据
        StrSubstitutor sub = new StrSubstitutor(valuesMap);
        String responseHead = sub.replace(OKMessage);
        return responseHead;
    }
}
