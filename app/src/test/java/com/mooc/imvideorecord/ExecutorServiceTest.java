package com.mooc.imvideorecord;

import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author SuTs
 * @create 2020/6/18 16:21
 * @Describe
 */
public class ExecutorServiceTest {
    @Test
    public void simpleUsage() throws Exception {
        ExecutorService service = Executors.newFixedThreadPool(4);
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
        for (int i = 0; i < 10; i++) {
            service.submit(new Runnable() {
                @Override
                public void run() {
                    String time = format.format(System.currentTimeMillis());
                    System.out.println(Thread.currentThread().getName() + ":" + time);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        Thread.sleep(4 * 1000);
    }
}
