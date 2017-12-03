package com.bootdo.inverst.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bootdo.inverst.utils.LoginUtils;
import com.bootdo.inverst.utils.Xxtea;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author yuefeng.liu
 */
public class WelfareInvest implements Runnable {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    private static final String URL_ASSET_CHECKPAY = "https://jr.yatang.cn/Invest/checkppay";
    private static final String URL_ASSET_TENDERINFO = "https://jr.yatang.cn/Public/tenderinfo";
    private static final String URL_ASSET_WELFARE = "https://jr.yatang.cn/Financial/welfare";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String userName;
    private String password;
    private String payPassword;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPayPassword() {
        return payPassword;
    }

    public void setPayPassword(String payPassword) {
        this.payPassword = payPassword;
    }

    public WelfareInvest(String userName, String password, String payPassword) {
        this.userName = userName;
        this.password = password;
        this.payPassword = payPassword;
    }

    /**
     * 开始登陆
     *
     * @throws Exception
     */
    private void startInvest() throws Exception {
        String cert = LoginUtils.gotoLogin(getUserName(), getPassword());
        if (cert != null){
            checkWelfare(cert);
        }

    }

    /**
     * 检查是否有秒
     * @param cert
     */
    public void checkWelfare(String cert){
        Document doc = null;
        try {
            HttpClient client = new HttpClient();
            doc = gotoAssetDetail(client, cert);
            client = new HttpClient();
            if (doc != null) {
                checkPay(client, doc, cert);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * 获取符合标的详情页
     *
     * @param client
     * @param tmpcookies
     * @throws IOException
     */
    public Document gotoAssetDetail(HttpClient client, String tmpcookies) throws IOException {
        GetMethod getMethod = new GetMethod(URL_ASSET_WELFARE);
        getMethod.setRequestHeader("Referer", URL_ASSET_WELFARE);
        getMethod.setRequestHeader("Host", "jr.yatang.cn");
        getMethod.setRequestHeader("User-Agent", USER_AGENT);
        getMethod.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        getMethod.setRequestHeader("Upgrade-Insecure-Requests", "1");
        getMethod.setRequestHeader("cookie", tmpcookies);
        getMethod.setRequestHeader("Connection", "keep-alive");
        int stats = client.executeMethod(getMethod);
        if (stats == HttpStatus.SC_OK) {
            StringBuffer response = new StringBuffer();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(getMethod.getResponseBodyAsStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.getProperty("line.separator"));
            }
            reader.close();
            Document doc = Jsoup.parse(response.toString());
            return doc;
        }
        return null;
    }

    /**
     * 提交投资请求
     *
     */
    public String checkPay(HttpClient client1, Document doc, String tmpcookies)
        throws IOException, InterruptedException {
        Element body = doc.body();
        String ibid = body.getElementsByClass("button_confirm_invest").attr("dataid");
        ibid = body.getElementById("ibid_" + ibid).val();
        String uniqKey = body.getElementById("uniqKey").val();
        String ppay = Xxtea.encryptToBase64String(getPayPassword(), uniqKey);
        String ibnum = body.getElementById("iborrownumid_"+ibid).val();
        String amount = body.getElementById("hxjk_"+ibid).val().split("\\.")[0];
        String lunchid = body.getElementById("savehbid_"+ibid).val();
        String hash = doc.body().getElementsByAttributeValue("name","__hash__").val();
        NameValuePair[] nameValuePairs = {
            new NameValuePair("p_pay", ppay),
            new NameValuePair("ibnum", ibnum),
            new NameValuePair("lunchId", "0"),
            new NameValuePair("amount", amount),
            new NameValuePair("__hash__", hash),
            new NameValuePair("vcode", "")
        };
        printMsg("提交信息："+ JSONObject.toJSONString(nameValuePairs));
        PostMethod postMethod = new PostMethod(URL_ASSET_CHECKPAY);
        postMethod.setRequestHeader("cookie", tmpcookies);
        postMethod.setRequestBody(nameValuePairs);
        HttpClient client = new HttpClient();
        int status = client.executeMethod(postMethod);
        if (status == HttpStatus.SC_OK) {
            StringBuffer response = new StringBuffer();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.getProperty("line.separator"));
            }
            reader.close();
            Map<String, Object> resultMap = (Map<String, Object>) JSON.parse(response.toString());
            Integer resStatus = (Integer)resultMap.get("status");
            String info = (String)resultMap.get("info");
            if (resStatus.equals(119)) {
                printMsg("进入投资列表...请稍候...");
                String tnum = (String)resultMap.get("tnum");
                checkIsPaySuccess(tmpcookies, ibnum, tnum);
            } else {
                printMsg("投资失败：" + info);
                Thread.sleep(1000);
                checkWelfare(tmpcookies);
            }
        }
        return "";
    }

    public void checkIsPaySuccess(String tmpcookies, String bNum, String tNUm)
        throws IOException, InterruptedException {
        NameValuePair[] nameValuePairs = {
            new NameValuePair("borrow_num", bNum),
            new NameValuePair("tnum", tNUm)
        };
        PostMethod postMethod = new PostMethod(URL_ASSET_TENDERINFO);
        postMethod.setRequestHeader("cookie", tmpcookies);
        postMethod.setRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        postMethod.setRequestHeader("Origin", "https://jr.yatang.cn");
        postMethod.setRequestHeader("Referer", "https://jr.yatang.cn/Invest/ViewBorrow/ibid/");
        postMethod.setRequestHeader("User-Agent", USER_AGENT);
        postMethod.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        postMethod.setRequestBody(nameValuePairs);
        HttpClient client = new HttpClient();
        int status = client.executeMethod(postMethod);
        if (status == HttpStatus.SC_OK) {
            StringBuffer response = new StringBuffer();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.getProperty("line.separator"));
            }
            reader.close();
            Map<String, Object> resultMap = (Map<String, Object>) JSON.parse(response.toString());
            Boolean resStatus = (Boolean)resultMap.get("status");
            String info = (String)resultMap.get("info");
            if (resStatus) {
                printMsg("抢秒成功");
            } else {
                if (StringUtils.isNoneBlank(info) && !info.equals("null")){
                    printMsg("没投上，死心吧 ^_^ ");
                } else {
                    printMsg("等待中..." + (info == null ? "" : info));
                    Thread.sleep(1000);
                    checkIsPaySuccess(tmpcookies, bNum, tNUm);
                }
            }
        }
    }

    private void printMsg(String msg){
        System.out.println(sdf.format(new Date()) + ":用户[" + getUserName() + "]-------" + msg);
    }

    @Override
    public void run() {
        try {
            startInvest();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
