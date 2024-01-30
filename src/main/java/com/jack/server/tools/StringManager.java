package com.jack.server.tools;


import java.lang.reflect.Member;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StringManager {
    //私有构造，实现单例
    private StringManager(String packageName){}

    public String getString(String key){
        if(key == null){
            String msg = "Jean : Key is null";
            throw new NullPointerException(msg);
        }
        String str = null;
        str = key;
        return str;
    }

    private static Map<String,StringManager> managers = new ConcurrentHashMap<>();
    //每个包都有自己的StringManager
    public synchronized static StringManager getManager(String packageName){
        StringManager stringManager = (StringManager)managers.get(packageName);

        if(stringManager == null){
            stringManager = new StringManager(packageName);
            managers.put(packageName,stringManager);
        }
        return stringManager;
    }

    //使用参数拼接
    public String getString(String key,Object[] args){
        String iString = null;
        String value = getString(key);
        try{
            //消除null
            Object nonNullArgs[] = args;
            for(int i=0;i<args.length;i++){
                if(args[i] ==null){
                    if(nonNullArgs ==args){
                        nonNullArgs = (Object[])args.clone();
                        nonNullArgs[i] = "null";
                    }
                }
            }

            //拼接
            iString = MessageFormat.format(value,nonNullArgs);

        }catch (IllegalArgumentException e){
            //针对异常，写入日志处理
            StringBuffer buf = new StringBuffer();
            buf.append(value);
            for(int i=0;i<args.length;i++){
                buf.append("arg[" +i+ "]=" + args[i]);
            }
            iString = buf.toString();

        }
        return iString;
    }

    public String getString(String key,Object arg){
        Object[] args = new Object[]{arg};
        return getString(key,args);
    }

    public String getString(String key,Object arg1,Object arg2){
        Object[] args = new Object[]{arg1,arg2};
        return getString(key,args);
    }

    public String getString(String key,Object arg1,Object arg2,Object arg3){
        Object[] args = new Object[]{arg1,arg2,arg3};
        return getString(key,args);
    }

    public String getString(String key,Object arg1,Object arg2,Object arg3,Object arg4){
        Object[] args = new Object[]{arg1,arg2,arg3,arg4};
        return getString(key,args);
    }
}
