package com.jack.server.https.streams;

import com.jack.server.https.requests.HttpHeader;
import com.jack.server.https.requests.HttpRequestLine;

import java.io.IOException;
import java.io.InputStream;

public class TestSocketInputStream extends InputStream {
        private static final byte CR = (byte) '\r';
        private static final byte LF = (byte) '\n';
        private static final byte SP = (byte) ' ';
        private static final byte HT = (byte) '\t';
        private static final byte COLON = (byte) ':';
        private static final int LC_OFFSET = 'A' - 'a';
        protected byte buf[];
        protected int count;
        protected int pos;
        protected InputStream is;
        public TestSocketInputStream(InputStream is, int bufferSize) {
            this.is = is;
            buf = new byte[bufferSize];
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
        @Override
        public int read() throws IOException {
            if (pos >= count) {
                fill();
                if (pos >= count) {
                    return -1;
                }
            }
            return buf[pos++] & 0xff;
        }
        public int available() throws IOException {
            return (count - pos) + is.available();
        }
        public void close() throws IOException {
            if (is == null) {
                return;
            }
            is.close();
            is = null;
            buf = null;
        }
        protected void fill() throws IOException {
            pos = 0;
            count = 0;
            int nRead = is.read(buf, 0, buf.length);
            if (nRead > 0) {
                count = nRead;
            }
        }
}
