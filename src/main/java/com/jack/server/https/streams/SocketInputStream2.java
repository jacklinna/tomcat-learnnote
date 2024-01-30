package com.jack.server.https.streams;

import com.jack.server.https.requests.HttpHeader;
import com.jack.server.https.requests.HttpRequestLine;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SocketInputStream2 extends ServletInputStream {
    //int这个类的作用就是从输入流中读出 request line 和 header 信息来
    private static final byte CR = (byte) '\r';
    private static final byte LF = (byte) '\n';
    private static final byte SP = (byte) ' ';
    private static final byte HT = (byte) '\t';
    private static final byte COLON = (byte) ':';
    private static final byte LC_OFFSET = 'A' - 'a';

    protected int pos;
    protected int count;

    protected byte[] buf;
    protected InputStream is;

    //接收处理 post参数
    //value是字符串数组  存在多个值与之相对，options，checkbox
    //目前只考虑文本类型，其实可以支持 文本，二进制，压缩包等，都是通过 Content-Type 定义指定。
    //常见的 Content-Type 有：application/json,applicaition/xml;
    //这样就支持 json xml
    //post 也可以混合，使用 multipart/form-data,一部分二进制，一部分文本

    public SocketInputStream2(InputStream is, int bufferSize){
        this.is = is;
        buf = new byte[bufferSize];
    }

    public void readRequestLiness(HttpRequestLine requestLine) throws IOException{
        int chr = 0;
        //跳过空行

        do{
            try {
                chr = read();
            } finally {

            }
        }while ((chr == CR) || (chr == LF));

        //第一个非空位置
        pos--;
        int maxRead = requestLine.method.length;

        int readStart = pos;
        int readCount = 0;

        boolean space = false;

        //解析第一段的 method 以空格结束

        while(!space){
            if(pos>= count){
                int val = read();

                if (val == -1) {
                    throw new IOException("requestStream.readLine.error");
                }

                pos = 0;
                readStart = 0;
            }

            if(buf[pos] ==SP){
                space = true;
            }

            requestLine.method[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }

        requestLine.methodEnd = readCount-1;
        //这就是上个 Method结束的位置

        //接下来是uri,基本逻辑与上无异
        maxRead = requestLine.uri.length;

        readStart = pos;
        readCount = 0;
        space = false;


        //解析空格间隔的第二段
        while(!space){
            if(pos>= count){
                int val = read();

                if (val == -1) {
                    throw new IOException("requestStream.readLine.error");
                }

                pos = 0;
                readStart = 0;
            }

            if(buf[pos] ==SP){
                space = true;
            }

            requestLine.uri[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }
        requestLine.uriEnd = readCount-1;
        //最后是procotol
        maxRead = requestLine.protocol.length;

        readStart = pos;
        readCount = 0;

        //解析空格间隔的第三段，是以eol 结束
        boolean eol = false;
        while(!eol){
            if(pos>= count){
                int val = read();

                if (val == -1) {
                    throw new IOException("requestStream.readLine.error");
                }

                pos = 0;
                readStart = 0;
            }

            if(buf[pos] ==CR){

            }else if(buf[pos] ==LF){
                eol = true;
            }else{
                requestLine.protocol[readCount] = (char) buf[pos];
                readCount++;
            }

            pos++;
        }
        requestLine.protocolEnd = readCount-1;

    }
    //tips:Tomcat 就是采用最简单的扫描方式，从InputStream中读取一个字节，然后放到buf,buf有长度限制，到头就从新开始。
    //然后针对 buf 一个个字节进行判断
    //pos是代表当前取得的位置
    //根据协议规定的分隔符 解析内容到line header
    public void readHeaderss(HttpHeader httpHeader) throws IOException{
        //读取 name=>value
        int chr = read();

        if((chr == CR) || (chr == LF)){
            //跳过 CR
            if((chr == CR)){
                read();
            }else{
                httpHeader.nameEnd=0;
                httpHeader.valueEnd=0;
                return;
            }
        }else{
            pos--;
        }

        //正在读取 header name
        int maxRead = httpHeader.name.length;

        int readStart = pos;
        int readCount = 0;

        //解析每一行，是以 sp 结束
        boolean colon = false;
        while(!colon){
            //当 处于 你内部缓存区 末尾
            if(pos>= count){
                //继续读取新的
                int val = read();

                if (val == -1) {
                    throw new IOException("requestStream.readLine.error");
                }

                pos = 0;
                readStart = 0;
            }

            if(buf[pos] ==COLON){
                colon = true;
            }

            char val = (char) buf[pos];

            if((val >= 'A') && (val <= 'Z')){
                //大写转为小写
                val = (char) (val - LC_OFFSET);
            }
            httpHeader.name[readCount] = val;
            readCount++;

            pos++;
        }
        httpHeader.nameEnd = readCount-1;
        //读取 这一行 name 对应的 value
        maxRead = httpHeader.value.length;

        readStart = pos;
        readCount = 0;

        int crPos = -2;


        //解析每一行，是以 sp 结束
        boolean eol = false;
        boolean validLine = true;
        while(validLine){

            boolean space = true;
            //跳过空格
            while(space){
                //当 处于 你内部缓存区 末尾
                if(pos>= count){
                    //继续读取新的
                    int val = read();

                    if (val == -1) {
                        throw new IOException("requestStream.readLine.error");
                    }

                    pos = 0;
                    readStart = 0;
                }

                if((buf[pos] ==SP) || (buf[pos] ==HT)){
                    pos++;
                }else{
                    space = false;
                }
            }

            while(!eol){
                //当 处于 你内部缓存区 末尾
                if(pos>= count){
                    //不再 读新的，而是讲一部分 复制到 行 缓冲区
                    int val = read();

                    if (val == -1) {
                        throw new IOException("requestStream.readLine.error");
                    }

                    pos = 0;
                    readStart = 0;
                }

                if((buf[pos] ==CR)){

                }else if((buf[pos] ==LF)){
                    eol = true;
                }else{
                    //fixme:检查二进制是否正常转换
                    int ch = buf[pos] & 0xff;
                    httpHeader.value[readCount] = (char) ch;
                    readCount++;
                }
                pos++;
            }

            int nextChar = read();

            if((nextChar != SP) && (nextChar != HT)){
                //
                pos--;
                validLine = false;
            }else{
                eol = false;
                httpHeader.value[readCount]=' ';
                readCount++;
            }
        }
        httpHeader.valueEnd = readCount-1;
    }

    //从输入流中解析出request line
    public void readRequestLine(HttpRequestLine requestLine)
            throws IOException {
        int chr = 0;
        //跳过空行
        do {
            try {
                chr = read();
            } catch (IOException e) {
            }
        } while ((chr == CR) || (chr == LF));
        //第一个非空位置
        pos--;
        int maxRead = requestLine.method.length;
        int readStart = pos;
        int readCount = 0;
        boolean space = false;
        //解析第一段method，以空格结束
        while (!space) {
            if (pos >= count) {
                int val = read();
                if (val == -1) {
                    throw new IOException("requestStream.readline.error");
                }
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == SP) {
                space = true;
            }
            requestLine.method[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }
        requestLine.methodEnd = readCount - 1; //method段的结束位置

        maxRead = requestLine.uri.length;
        readStart = pos;
        readCount = 0;
        space = false;
        boolean eol = false;
        //解析第二段uri，以空格结束
        while (!space) {
            if (pos >= count) {
                int val = read();
                if (val == -1)
                    throw new IOException("requestStream.readline.error");
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == SP) {
                space = true;
            }
            requestLine.uri[readCount] = (char) buf[pos];
            readCount++;
            pos++;
        }
        requestLine.uriEnd = readCount - 1; //uri结束位置

        maxRead = requestLine.protocol.length;
        readStart = pos;
        readCount = 0;
        //解析第三段protocol，以eol结尾
        while (!eol) {
            if (pos >= count) {
                int val = read();
                if (val == -1)
                    throw new IOException("requestStream.readline.error");
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == CR) {
                // Skip CR.
            } else if (buf[pos] == LF) {
                eol = true;
            } else {
                requestLine.protocol[readCount] = (char) buf[pos];
                readCount++;
            }
            pos++;
        }
        requestLine.protocolEnd = readCount;
    }
    public void readHeader(HttpHeader header)
            throws IOException {
        int chr = read();
        if ((chr == CR) || (chr == LF)) { // Skipping CR
            if (chr == CR)
                read(); // Skipping LF
            header.nameEnd = 0;
            header.valueEnd = 0;
            return;
        } else {
            pos--;
        }
        // 正在读取 header name
        int maxRead = header.name.length;
        int readStart = pos;
        int readCount = 0;
        boolean colon = false;
        while (!colon) {
            // 我们处于内部缓冲区的末尾
            if (pos >= count) {
                int val = read();
                if (val == -1) {
                    throw new IOException("requestStream.readline.error");
                }
                pos = 0;
                readStart = 0;
            }
            if (buf[pos] == COLON) {
                colon = true;
            }
            char val = (char) buf[pos];
            if ((val >= 'A') && (val <= 'Z')) {
                val = (char) (val - LC_OFFSET);
            }
            header.name[readCount] = val;
            readCount++;
            pos++;
        }
        header.nameEnd = readCount - 1;
        // 读取 header 值（可以跨越多行）
        maxRead = header.value.length;
        readStart = pos;
        readCount = 0;
        int crPos = -2;
        boolean eol = false;
        boolean validLine = true;
        while (validLine) {
            boolean space = true;
            // 跳过空格
            // 注意：仅删除前面的空格，后面的不删。
            while (space) {
                // 我们已经到了内部缓冲区的尽头
                if (pos >= count) {
                    // 将内部缓冲区的一部分（或全部）复制到行缓冲区
                    int val = read();
                    if (val == -1)
                        throw new IOException("requestStream.readline.error");
                    pos = 0;
                    readStart = 0;
                }
                if ((buf[pos] == SP) || (buf[pos] == HT)) {
                    pos++;
                } else {
                    space = false;
                }
            }
            while (!eol) {
                // 我们已经到了内部缓冲区的尽头
                if (pos >= count) {
                    // 将内部缓冲区的一部分（或全部）复制到行缓冲区
                    int val = read();
                    if (val == -1)
                        throw new IOException("requestStream.readline.error");
                    pos = 0;
                    readStart = 0;
                }
                //换行表示结束
                if (buf[pos] == CR) {
                } else if (buf[pos] == LF) {
                    eol = true;
                } else {
                    // FIXME：检查二进制转换是否正常
                    int ch = buf[pos] & 0xff;
                    header.value[readCount] = (char) ch;
                    readCount++;
                }
                pos++;
            }
            //再往前读一个字符，如果是空格，或者制表符，就继续处理多行的情况
            int nextChr = read();
            if ((nextChr != SP) && (nextChr != HT)) {
                pos--;
                validLine = false;
            } else {
                eol = false;
                header.value[readCount] = ' ';
                readCount++;
            }
        }
        header.valueEnd = readCount;
    }

    protected void fill() throws IOException{
        pos = 0;
        count = 0;

        int nRead = is.read(buf,0,buf.length);

        if(nRead>0){
            count = nRead;
        }
    }

    @Override
    public int read() throws IOException {
        if(pos>=count){
            fill();
            if(pos>= count){
                return -1;
            }
        }
        return buf[pos++] & 0xff;
    }
    public int available() throws IOException{
        return (count-pos) + is.available();
    }

    public void close() throws IOException{
        if(is ==null){
            return;
        }else{
            is.close();
            is = null;
            buf = null;

        }
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
}
