package com.jack.server.tools;


import javax.servlet.http.Cookie;

public class CookieTools {
    public static String getCookieHeaderName(Cookie cookie){
        return "Set-Cookie";
    }

    public static void getCookieHeaderValue(Cookie cookie,StringBuffer buf){
        String name = cookie.getName();

        if(name==null){
            name="";
        }
        String value = cookie.getValue();

        if(value == null){
            value="";
        }
    //这样简单构建一下 key=value的字符串
        buf.append(name);
        buf.append("=");
        buf.append(value);
    }

    static void maybeQuote(int version,StringBuffer buf,String value){
        if(version ==0 || isToken(value)){
            buf.append(value);
        }else{
            buf.append('"');
            buf.append(value);
            buf.append('"');
        }
    }

    private static final String tspecials = "()<>@,;:\\\"/[]?={}\t";

    private static boolean isToken(String value) {
        //没有特殊符号就是token
        //tspecials 就是特殊符号，没有显式调用遍历，使用字符串查找API
        int len = value.length();

        for(int i=0;i<len;i++){
            char c=  value.charAt(i);

            if(c < 0x20 || c>=0x7f || tspecials.indexOf(c) != -1){
                return false;
            }

        }
        return true;
    }
}
