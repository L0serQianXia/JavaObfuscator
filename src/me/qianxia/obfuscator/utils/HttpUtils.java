package me.qianxia.obfuscator.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author: QianXia
 * @create: 2021-02-25 15:06
 **/
public class HttpUtils {
    public static String get(String url) {
        try {
            String result = "";
            URL webURL = new URL(url);
            URLConnection connection = webURL.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(15000);
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.connect();
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
            if (in != null) {
                in.close();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
