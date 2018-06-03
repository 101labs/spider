package org.decaywood;

import org.decaywood.utils.FileLoader;
import org.decaywood.utils.RequestParaBuilder;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author: decaywood
 * @date: 2016/04/10 20:01
 */
public interface CookieProcessor {


    default void updateCookie(String website) throws Exception {

        GlobalSystemConfigLoader.loadConfig();

        String areacode = System.getProperty("areaCode");
        //String userID = "15057155235";
        //String passwd = "zzx627546245";
        String userID=null;
        String passwd=null;
        //System.out.println(""+userID+":"+passwd);
        boolean rememberMe = Boolean.parseBoolean(System.getProperty("rememberMe"));

        HttpURLConnection connection = null;
        if (userID != null && passwd != null) {
            connection = login(areacode, userID, passwd, rememberMe);
        }
        try {
            connection = connection == null ?
                    (HttpURLConnection) new URL(website).openConnection() : connection;
            connection.connect();
            
            String cookie = connection.getHeaderFields().get("Set-Cookie")
                    .stream()
                    .map(x -> x.split(";")[0].concat(";"))
                    .filter(x -> x.contains("token=") || x.contains("s="))
                    .reduce("", String::concat);
                   
            // cookie="xq_a_token=02a16c8dd2d87980d1b3ddced673bd6a74288bde; xq_r_token=024b1e233fea42dd2e0a74832bde2c914ed30e79;";
            FileLoader.updateCookie(cookie, website);
            System.out.println(cookie);
            System.out.println(website);
        } finally {
            if (connection != null) connection.disconnect();
        }

    }

    default HttpURLConnection login(String areacode,
                                    String userID,
                                    String passwd,
                                    boolean rememberMe) throws Exception {

        areacode = areacode == null ? "86" : areacode;
        if (userID == null || passwd == null) {
            throw new IllegalArgumentException("null parameter: userID or password");
        }

        RequestParaBuilder builder = new RequestParaBuilder("http://xueqiu.com/user/login")
                .addParameter("areacode", areacode)
                .addParameter("telephone", userID)
                .addParameter("password", passwd)
                .addParameter("remember_me", rememberMe ? "on" : "off");

        URL url = new URL(builder.build());
        return (HttpURLConnection) url.openConnection();
    }


}
