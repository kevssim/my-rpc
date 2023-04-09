package github.kevssim.service;


import java.util.Random;

public class HelloServiceImpl implements HelloService {
    private static int count = 0;
    private static Random random = new Random();

    //当有消费方调用该方法时就返回一个结果
    @Override
    public String hello(String mes) throws Exception {
        //System.out.println("收到消息：" + mes + " " + (++count));
        Thread.sleep(2000);
        return "你好客户端，我已经收到你的消息[" + mes + "] 第" + (++count) + "次";
    }

    @Override
    public String introduct() {
        return "我是服务端";
    }
}
