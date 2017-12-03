package com.bootdo.inverst.service.impl;

import com.bootdo.inverst.utils.LoginUtils;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author yuefeng.liu
 */
public class IncomeByYear {
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    /**
     * 获取资金记录
     */
    private static final String URL_RECORD = "https://jr.yatang.cn/Account/fund/record?";

    private static final String USER_USERNAME = "18217103545";//用户名
    private static final String USER_PASSWORD = "lhyyfn623";//密码

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");

    private static final Integer YEAR = 2017;

    private static String[] types = {"积分兑换","红包充值","投资返利活动","投资成功","推荐提成","续投奖励","获得投资奖励","股权收款","老推新待收奖励"};

    /**
     * 开始登陆
     *
     * @throws Exception
     */
    private void start() throws Exception {
        String cert = LoginUtils.gotoLogin(USER_USERNAME, USER_PASSWORD);
        if (cert != null){
            doStart(cert);
        }

    }

    private void doStart(String cert) {
        BigDecimal totalRecord = new BigDecimal(0);
        for (int i = 0; i < 12; i++) {
            int month = i + 1;
            int nowMonth = getNowMonth();
            if (month <= nowMonth) {
                totalRecord = totalRecord.add(cualRecordByMonth(cert, month));
            }
        }
        printMsg(YEAR + "年一共收入" + totalRecord);
    }

    private BigDecimal cualRecordByMonth(String cert, int month) {
        HttpClient client = new HttpClient();
        BigDecimal totalRecord = new BigDecimal(0);
        int page = 1;
        try {
            Document doc = gotoRecordList(client, cert, getQueryString(month, page));
            if (doc == null) {
                return BigDecimal.ZERO;
            }
            page = getTotalPage(doc);
            for (int i = 1; i <= page; i++) {
                doc = gotoRecordList(client, cert, getQueryString(month, i));
                totalRecord = totalRecord.add(getTotalRecordByPage(doc));
            }
        } catch (Exception e) {
            printMsg("程序发生异常->错误信息:" + e.getMessage());
        }
        String showMonth = month < 10 ? "0"+month : ""+month;
        printMsg(YEAR + "年" + showMonth + "月收入" + totalRecord);
        return totalRecord;
    }

    private String getQueryString(int month, int page) {
        String toptime = getMinMonthDate(month);
        String backtime = getMaxMonthDate(month);
        String query = "toptime=" + toptime + "&backtime=" + backtime + "&p=" + page;
        return query;
    }

    /**
     * 获取符合标的详情页
     *
     * @param assetId
     * @param tmpcookies
     * @throws IOException
     */
    public Document gotoRecordList(HttpClient client, String tmpcookies, String queryParam) throws IOException {
        GetMethod getMethod = new GetMethod(URL_RECORD.concat(queryParam));
        getMethod.setRequestHeader("Referer", "https://jr.yatang.cn/Account/fund/record");
        getMethod.setRequestHeader("Host", "jr.yatang.cn");
        getMethod.setRequestHeader("User-Agent", USER_AGENT);
        getMethod.setRequestHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
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
     * 获取总页码
     * @param doc
     * @return
     */
    private int getTotalPage(Document doc){
        Elements digg = doc.getElementsByClass("digg");
        String s = digg.html();
        Pattern p = Pattern.compile("数据(.*)页");
        Matcher matcher = p.matcher(s);
        String pageStr = "";
        while(matcher.find()){
            pageStr+=matcher.group(1);
        }
        return Integer.valueOf(pageStr.split("/")[1].trim());
    }

    /**
     * 获取当前页面总收入
     * @param doc
     * @return
     */
    private BigDecimal getTotalRecordByPage(Document doc){
        if (doc == null){
            return BigDecimal.ZERO;
        }
        BigDecimal totalRecord = new BigDecimal(0);
        Elements trs = doc.getElementsByClass("acc-inrc42");
        ListIterator<Element> listIterator = trs.listIterator();
        while (listIterator.hasNext()){
            Element tr = listIterator.next();
            Elements tds = tr.getElementsByTag("td");
            String type = tds.eq(1).html();
            String orange = tds.eq(2).first().getElementsByTag("span").html().replace("+", "");
            totalRecord = totalRecord.add(filterRecord(type, orange));
        }
        return totalRecord;
    }

    private BigDecimal filterRecord(String type, String orange){
        List<String> typeList = Arrays.asList(types);
        BigDecimal orangeValue = null;
        if (typeList.contains(type)){
            orangeValue = new BigDecimal(orange);
            return orangeValue;
        }
        return BigDecimal.ZERO;
    }

    private void printMsg(String msg) {
        System.out.println(sdf.format(new Date()) + ":-------" + msg);
    }

    /**
     * 获取当前时间月份
     *
     * @param args
     */
    public static int getNowMonth() {
        Calendar cal = Calendar.getInstance();
        //获得当前时间的月份，月份从0开始所以结果要加1
        int month = cal.get(Calendar.MONTH) + 1;
        return month;
    }

    /**
     * 获取当前月份最后一天
     *
     * @param month
     * @return
     * @throws ParseException
     */
    public static String getMaxMonthDate(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, YEAR);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return sdfDate.format(calendar.getTime());
    }

    /**
     * 获取当前月份第一天
     *
     * @param month
     * @return
     * @throws ParseException
     */
    public static String getMinMonthDate(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, YEAR);
        calendar.set(Calendar.MONTH, month - 1);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
        return sdfDate.format(calendar.getTime());
    }

    public static void main(String[] args) {
        try {
            IncomeByYear method = new IncomeByYear();
            method.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void test(){
        BigDecimal a = new BigDecimal(0);
        BigDecimal b = new BigDecimal(10);
        System.out.println(a.add(b).intValue());

    }

}
