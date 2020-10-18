package homework;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;

public class MyClassLoader extends ClassLoader {
    public static void main(String[] args) {
        try {
            // 初始化class
            Class<?> clz = new MyClassLoader().findClass("Hello", "src/homework/hello/Hello.xlass");

            // 获取要调用的方法
            Method hello = clz.getDeclaredMethod("hello");
            hello.setAccessible(true);

            // 调用指定实例的方法
            hello.invoke(clz.newInstance());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public byte[] getBytes(String filepath) {
        File file = new File(filepath);
        InputStream is = null;
        ByteArrayOutputStream bos = null;
        try {
            is = new FileInputStream(file);
            bos = new ByteArrayOutputStream();
            byte[] b = new byte[1];
            int len = -1;
            while ((len = is.read(b)) != -1) {
                // 每次读取一个字节，就取0转化
                b[0] = (byte)(255 - b[0]);
                bos.write(b, 0, len);
            }
            return bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    protected Class<?> findClass(String name, String filepath) throws ClassNotFoundException {
        byte[] bytes = getBytes(filepath);
        return defineClass(name, bytes, 0, bytes.length);
    }

    public byte[] decode(String base64) {
        return Base64.getDecoder().decode(base64);
    }
}
