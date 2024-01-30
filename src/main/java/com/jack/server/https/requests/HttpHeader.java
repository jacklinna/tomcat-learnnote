package com.jack.server.https.requests;

public class HttpHeader {
    //针对 Http Header 进行抽象
//    格式：name => value

    public static final int INITIAL_NAME_SIZE = 64;
    public static final int INITIAL_VALUE_SIZE = 512;

    public static final int MAX_NAME_SIZE = 128;
    public static final int MAX_VALUE_SIZE = 1024;

    //char[] 保存每段的字符串，int 保存每段的结束位置

    public char[] name;
    public char[] value;

    public int nameEnd;
    public int valueEnd;

    protected int hashCode = 0;

    public HttpHeader(){
        this(
                new char[INITIAL_NAME_SIZE],
                0,
                new char[INITIAL_VALUE_SIZE],
                0
        );
    }


    public HttpHeader(char[] chars, int i, char[] chars1, int i1) {
        this.name = chars;
        this.nameEnd = i;

        this.value = chars1;
        this.valueEnd = i1;
    }

    public HttpHeader(String name, String value) {
        this.name = name.toLowerCase().toCharArray();
        this.nameEnd = name.length();

        this.value = value.toCharArray();
        this.valueEnd = value.length();
    }

    public void recycle(){
        nameEnd = 0;
        valueEnd = 0;
        hashCode = 0;
    }


}
