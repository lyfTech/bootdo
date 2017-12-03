package com.bootdo.inverst.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScheduleWelfare {

    /**
     * 获取指定时间对应的毫秒数
     *
     * @param time "HH:mm:ss"
     * @return
     */
    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            return curDate.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * scheduleAtFixedRate
     * 每天指定时间
     * 每天定时安排任务进行执行
     */
    public static void executeAtTimePerDay(List<WelfareInvest> list, String time) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(list.size());
        long oneDay = 24 * 60 * 60 * 1000;
        long initDelay = getTimeMillis(time) - System.currentTimeMillis();
        initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
        System.out.println(list.size() + "个账户将在" + time + "运行...");
        for (WelfareInvest welfareInvest : list) {
            executor.scheduleAtFixedRate(
                welfareInvest,
                initDelay,
                oneDay,
                TimeUnit.MILLISECONDS);
        }

    }

    public static void main(String[] args) {
        List<WelfareInvest> list = new ArrayList<WelfareInvest>();
        list.add(new WelfareInvest("18217103545", "lhyyfn623", "abcd1234"));
        String time = "16:24:00";
        executeAtTimePerDay(list, time);
    }
}
