package com.bootdo.inverst.utils;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.PostMethod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoginUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String URL_ASSET_SETCOOKIES = "https://jr.yatang.cn/Ajax/setUserCookie";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
    private static final String URL_USER_LOGIN = "https://yztapi.yatang.cn/yluser";

    /**
     * 获取用户认证
     *
     * @param tmpcookies
     * @return
     * @throws IOException
     */
    public static String getCookies(HttpClient client, String tmpcookies, String userName) throws IOException {
        NameValuePair[] nameValuePairs = {
            new NameValuePair("url", "https://jr.yatang.cn/index.php?s=/index/index/*type:1")
        };
        PostMethod postMethod = new PostMethod(URL_ASSET_SETCOOKIES);
        postMethod.setRequestHeader("cookie", tmpcookies);
        postMethod.setRequestHeader("Referer", "https://jr.yatang.cn/ytRedirect.html");
        postMethod.setRequestHeader("Host", "jr.yatang.cn");
        postMethod.setRequestHeader("User-Agent", USER_AGENT);
        postMethod.setRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        postMethod.setRequestBody(nameValuePairs);
        int stats = client.executeMethod(postMethod);
        if (stats == HttpStatus.SC_OK) {
            Cookie[] cookies = client.getState().getCookies();
            //tmpcookies = USER_COOKIES;
            for (Cookie c : cookies) {
                if (c.toString().contains("route")) {
                    continue;
                }
                tmpcookies += (c.toString() + ";");
            }
            if (tmpcookies.indexOf("itbt_auth") != -1) {
                tmpcookies = tmpcookies.replace("$Version=\"1\";", "");
                tmpcookies = tmpcookies.replace("$Domain=\".yatang.cn\";", "");
                tmpcookies = tmpcookies.replace("$Path=\"/\"; ", "");
                printMsg("用户 [" + userName + "] 认证成功");
                return tmpcookies;
            }
        }
        return "";
    }

    /**
     * 开始登陆
     *
     * @throws Exception
     */
    public static String gotoLogin(String userName, String password) throws Exception {
        String nowTime = sdf.format(new Date());
        HttpClient client = new HttpClient();
        NameValuePair[] nameValuePairs = {
            new NameValuePair("format", "json"),
            new NameValuePair("appKey", "00001"),
            new NameValuePair("v", "1.0"),
            new NameValuePair("timestamp", nowTime),
            new NameValuePair("method", "sso.login"),
            new NameValuePair("origin", "1"),
            new NameValuePair("source", "1"),
            new NameValuePair("synUrl", "https://jr.yatang.cn/index.php?s=/index/index/*type:1"),
            new NameValuePair("u", userName),
            new NameValuePair("p", EncryptUtils.aesEncrypt(nowTime.concat(password))),
            new NameValuePair("vcode", ""),
            new NameValuePair("cookieAge", "600"),
        };
        PostMethod postMethod = new PostMethod(URL_USER_LOGIN);
        postMethod.setRequestBody(nameValuePairs);
        int stats = client.executeMethod(postMethod);
        if (stats == HttpStatus.SC_OK) {
            StringBuffer response = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.getProperty("line.separator"));
            }
            reader.close();
            // 取出登陆成功后，服务器返回的cookies信息，里面保存了服务器端给的“临时证”
            CookieSpec cookiespec = CookiePolicy.getDefaultSpec();
            Cookie[] cookies = cookiespec.match("https://jr.yatang.cn", 443, "/", false,client.getState().getCookies());
            StringBuffer tmpcookies = new StringBuffer();
            for (Cookie c : cookies) {
                tmpcookies.append(c.toString() + ";");
            }
            //获取认证
            return getCookies(client, tmpcookies.toString(), userName);
        }
        return null;
    }

    public static void printMsg(String msg) {
        System.out.println(sdf.format(new Date()) + ":-------" + msg);
    }

}
