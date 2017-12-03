package com.bootdo.inverst.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author yuefeng.liu
 */
public class CommonInvest {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";
    /**
     * 获取标列表
     */
    private static final String URL_ASSET_LIST = "https://jr.yatang.cn/Financial/getAssetList";
    private static final String URL_ASSET_DETAIL = "https://jr.yatang.cn/Invest/ViewBorrow/ibid/";
    private static final String URL_ASSET_COUPON = "https://jr.yatang.cn/Ajax/getUserCoupon";
    private static final String URL_ASSET_CHECKPAY = "https://jr.yatang.cn/Invest/checkppay";
    private static final String URL_ASSET_TENDERINFO = "https://jr.yatang.cn/Public/tenderinfo";

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private String userName;
    private String password;
    private String payPassword;
    private Long redBag;
    private Long keepMoney;

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

    public Long getRedBag() {
        return redBag;
    }

    public void setRedBag(Long redBag) {
        this.redBag = redBag;
    }

    public Long getKeepMoney() {
        return keepMoney;
    }

    public void setKeepMoney(Long keepMoney) {
        this.keepMoney = keepMoney;
    }

    public CommonInvest(String userName, String password, String payPassword, Long redBag, Long keepMoney) {
        this.userName = userName;
        this.password = password;
        this.payPassword = payPassword;
        this.redBag = redBag;
        this.keepMoney = keepMoney;
    }

    /**
     * 开始登陆
     *
     * @throws Exception
     */
    private void start() throws Exception {
        String cert = LoginUtils.gotoLogin(getUserName(), getPassword());
        if (cert != null){
            doInvest(cert);
        }

    }

    private void doInvest(String cert) throws Exception {
        printMsg("发起请求");
        //获取符合条件的月标ID
        String assetId = null;
        do {
            assetId = getAssetList(cert);
        } while (!StringUtils.isNoneBlank(assetId));
        HttpClient client = new HttpClient();
        Document doc = gotoAssetDetail(client, assetId, cert);
        if (doc != null) {
            //获取红包ID
            String uniqKey = doc.body().getElementById("uniqKey").val();
            String iborrownumid = doc.body().getElementById("iborrownumid").val();
            String couponId = getCoupon(cert, iborrownumid, assetId);
            if (!"0".equals(couponId)) {
                Integer zhkyye = Double.valueOf(doc.getElementById("zhkyye").val()).intValue();
                printMsg("剩余可投金额：" + zhkyye);
                if (zhkyye > getKeepMoney()){
                    //提交标
                    checkPay(client, doc, cert, couponId);
                } else {
                    printMsg("投资结束，程序退出");
                    System.exit(0);
                }

            }
        }
    }

    /**
     * 获取月标列表
     *
     * @param tmpcookies
     * @return
     * @throws IOException
     */
    private String getAssetList(String cert) throws IOException, InterruptedException {
        HttpClient client = new HttpClient();
        NameValuePair[] nameValuePairs = {
            new NameValuePair("format", "json"),
            new NameValuePair("aprrange", "0"),
            new NameValuePair("selectdate", "2"),
            new NameValuePair("repaystyle", "1"),
            new NameValuePair("goto_page", ""),
            new NameValuePair("page_href", "")
        };
        PostMethod postMethod = new PostMethod(URL_ASSET_LIST);
        postMethod.setRequestHeader("cookie", cert);
        postMethod.setRequestBody(nameValuePairs);
        int stats = client.executeMethod(postMethod);
        if (stats == HttpStatus.SC_OK) {
            StringBuffer response = new StringBuffer();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.getProperty("line.separator"));
            }
            reader.close();
            Map<String, Object> resultMap = (Map<String, Object>) JSON.parse(response.toString());
            JSONArray array = (JSONArray)resultMap.get("list");
            if (array != null && array.size() > 0) {
                for (Object obj : array) {
                    JSONObject jsonObject = (JSONObject)obj;
                    String id = jsonObject.get("id").toString();
                    Long remain = Long.valueOf(jsonObject.get("remain").toString());
                    if (remain >= (getRedBag() * 100)) {
                        printMsg("抓取到符合条件的月标：月标ID：" + id);
                        return id;
                    }
                }
                Thread.sleep(1000);
                printMsg("没有符合条件的月标");
                getAssetList(cert);
            } else {
                Thread.sleep(1000);
                printMsg("没有符合条件的月标");
                getAssetList(cert);
            }
        }
        return null;
    }

    /**
     * 获取符合条件的红包
     *
     * @param cookies
     * @param iborrownumid
     */
    public String getCoupon(String tmpcookies, String iborrownumid, String assetId)
        throws Exception {
        NameValuePair[] nameValuePairs = {
            new NameValuePair("investMoney", ""),
            new NameValuePair("borrowNum", iborrownumid),
            new NameValuePair("pageNum", "1")
        };
        PostMethod postMethod = new PostMethod(URL_ASSET_COUPON);
        postMethod.setRequestHeader("Cookie", tmpcookies);
        postMethod.setRequestBody(nameValuePairs);
        HttpClient client = new HttpClient();
        int status = client.executeMethod(postMethod);
        if (status == HttpStatus.SC_OK) {
            StringBuffer response = new StringBuffer();
            BufferedReader reader = new BufferedReader( new InputStreamReader(postMethod.getResponseBodyAsStream(), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line).append(System.getProperty("line.separator"));
            }
            reader.close();
            Map<String, Object> resultMap = (Map<String, Object>) JSON.parse(response.toString());
            Integer resStatus = (Integer)resultMap.get("status");
            String info = (String)resultMap.get("info");
            if (resStatus.equals(1)) {
                JSONArray array = (JSONArray)resultMap.get("data");
                Double withdrawalCashD = Double.valueOf((String)resultMap.get("withdrawal_cash"));
                Integer withdrawalCash = withdrawalCashD.intValue();
                //可投金额是否大于红包金额
                Boolean isAccept = withdrawalCash > getRedBag() * 100;
                if (array.size() > 0 && isAccept) {
                    for (Object obj : array) {
                        JSONObject jsonObject = (JSONObject)obj;
                        String id = jsonObject.get("id").toString();
                        Integer value = Integer.valueOf(jsonObject.get("value").toString());
                        Integer userConstraint = Integer.valueOf(jsonObject.get("user_constraint").toString());
                        Integer currRbPercent = userConstraint / value;
                        if (getRedBag() >= currRbPercent) {
                            printMsg("获取符合的红包，ID:" + id);
                            //return id + ":" + userConstraint;
                            return "18718388:" + userConstraint;
                        }
                    }
                }
                printMsg("没有红包！");
            } else {
                if (StringUtils.isBlank(info)) {
                    printMsg("获取红包错误：没有符合要求的红包!");
                } else {
                    printMsg("获取红包错误：" + info);
                    Thread.sleep(2000);
                    //doInvest(tmpcookies);
                }
            }
        }
        return "0";
    }

    /**
     * 获取符合标的详情页
     *
     * @param assetId
     * @param tmpcookies
     * @throws IOException
     */
    public Document gotoAssetDetail(HttpClient client, String assetId, String tmpcookies) throws IOException {
        GetMethod getMethod = new GetMethod(URL_ASSET_DETAIL.concat(assetId));
        getMethod.setRequestHeader("Referer", "https://jr.yatang.cn/Financial/asset");
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
     * @param cookies
     * @param iborrownumid
     */
    public String checkPay(HttpClient client, Document doc, String tmpcookies, String hbId)
        throws Exception {
        Long amount = Long.valueOf(hbId.split(":")[1]);
        String hash = doc.body().getElementsByAttributeValue("name","__hash__").val();
        String userId = doc.body().getElementById("cuid").val();
        String uniqKey = doc.body().getElementById("uniqKey").val();
        String iborrownumid = doc.body().getElementById("iborrownumid").val();
        String payPwd = Xxtea.encryptToBase64String(getPayPassword(), uniqKey);
        NameValuePair[] nameValuePairs = {
            new NameValuePair("p_pay", payPwd),
            new NameValuePair("user_id", userId),
            new NameValuePair("ibnum", iborrownumid),
            new NameValuePair("lunchId", hbId.split(":")[0]),
            new NameValuePair("amount", amount.toString()),
            new NameValuePair("__hash__", hash),
            new NameValuePair("vcode", "")
        };
        PostMethod postMethod = new PostMethod(URL_ASSET_CHECKPAY);
        postMethod.setRequestHeader("cookie", tmpcookies);
        postMethod.setRequestHeader("Accept", "application/json, text/javascript, */*; q=0.01");
        postMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        postMethod.setRequestHeader("Origin", "https://jr.yatang.cn");
        postMethod.setRequestHeader("Referer", "https://jr.yatang.cn/Invest/ViewBorrow/ibid/");
        postMethod.setRequestHeader("User-Agent", USER_AGENT);
        postMethod.setRequestHeader("X-Requested-With", "XMLHttpRequest");
        postMethod.setRequestBody(nameValuePairs);
        //HttpClient client = new HttpClient();
        //Thread.sleep(1000);
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
            printMsg("提交返回信息："+ JSONObject.toJSONString(resultMap));
            Integer resStatus = (Integer)resultMap.get("status");
            String info = (String)resultMap.get("info");
            if (resStatus.equals(119)) {
                printMsg("进入投资列表...请稍候...");
                String tnum = (String)resultMap.get("tnum");
                checkIsPaySuccess(tmpcookies, iborrownumid, tnum);
            } else {
                printMsg("投资失败：" + info);
                try {
                    Thread.sleep(2000);
                    doInvest(tmpcookies);
                } catch (Exception e) {
                    Thread.sleep(2000);
                    doInvest(tmpcookies);
                }
            }
        }
        return "";
    }

    public String checkIsPaySuccess(String tmpcookies, String bNum, String tNUm)
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
            try {
                String info = (String)resultMap.get("info");
                if (resStatus) {
                    printMsg("投资成功");
                    doInvest(tmpcookies);
                } else {
                    if (StringUtils.isNoneBlank(info) && !info.equals("null")){
                        printMsg("没投上，标已经没了 ^_^ ");
                        Thread.sleep(2000);
                        doInvest(tmpcookies);
                    } else {
                        printMsg("等待中..." + (info == null ? "" : info));
                        Thread.sleep(1000);
                        checkIsPaySuccess(tmpcookies, bNum, tNUm);
                    }
                }
            } catch (Exception e) {
                try {
                    printMsg("没投上，标已经没了 ^_^ ");
                    Thread.sleep(2000);
                    doInvest(tmpcookies);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
        return "";
    }

    private void printMsg(String msg){
        System.out.println(sdf.format(new Date()) + ":-------" + msg);
    }

    public static void main(String[] args) {
        try {
            CommonInvest method = new CommonInvest("15021040643","pk123456","pk19920715",60L, 0L);
            method.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
