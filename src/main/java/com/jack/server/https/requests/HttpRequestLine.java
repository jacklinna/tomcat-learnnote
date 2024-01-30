package com.jack.server.https.requests;

public class HttpRequestLine {
    //针对 Http Request 进行抽象
//    格式：method uri protocol
    //Get /hello.txt HTTP/1.1

    //所以，这里面都是根据HTTP协议来定义的
    //目前使用 char[],而不是byte[];直接从inputStream中读取，不经过String转换
    //为了准确知道Char,因此也需要 几个属性来标识 最后一个字符的位置，复杂度提高了一些。

    public static final int INITIAL_METHOD_SIZE = 8;
    public static final int INITIAL_URI_SIZE = 128;
    public static final int INITIAL_PROTOCOL_SIZE = 8;
    public static final int MAX_METHOD_SIZE = 32;
    public static final int MAX_URI_SIZE = 2048;
    public static final int MAX_PROTOCOL_SIZE = 32;

    //char[] 保存每段的字符串，int 保存每段的结束位置

    public char[] method;
    public char[] uri;
    public char[] protocol;
    public int methodEnd;
    public int uriEnd;
    public int protocolEnd;

    public HttpRequestLine(){
        this(
                new char[INITIAL_METHOD_SIZE],
                0,
                new char[INITIAL_URI_SIZE],
                0,
                new char[INITIAL_PROTOCOL_SIZE],
                0
        );
    }


    public HttpRequestLine(char[] chars, int i, char[] chars1, int i1, char[] chars2, int i2) {
        this.method = chars;
        this.methodEnd = i;

        this.uri = chars1;
        this.uriEnd = i1;

        this.protocol = chars2;
        this.protocolEnd = i2;
    }

    public void recycle(){
        methodEnd = 0;
        uriEnd = 0;
        protocolEnd = 0;
    }

    public int indexOf(char[] buf){
        return  indexOf(buf,buf.length);
    }

    public int indexOf(char c,int start){
        for(int i= start;i< uriEnd;i++){
            if(uri[i] == c){
                return i;
            }
        }
        return -1;
    }

    public int indexOf(String str){

        return indexOf(str.toCharArray(),str.length());
    }

    public int indexOf(char[] buf,int end){
        char firstChar = buf[0];

        int pos = 0;
        //pos是查找 字符串char[](String) 在uri[]中的开始位置

        while(pos<uriEnd){
            pos = indexOf(firstChar,pos);
            //首字符 定位开始位置
            if(pos == -1){
                return -1;
            }
            if((uriEnd - pos) < end){
                return -1;
            }

            for(int i=0;i<end;i++){
                //从开始位置 逐个比对
                if(uri[i + pos] != buf[i]){
                    break;
                }
                //每个字符都一样，那么就返回开始位置
                if(i == (end-1)){
                    return pos;
                }
            }
            pos++;

        }
        return -1;
    }
}
