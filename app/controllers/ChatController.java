package controllers;

import com.typesafe.plugin.RedisPlugin;
import play.libs.Akka;
import play.mvc.WebSocket;
import redis.clients.jedis.Jedis;
import scala.concurrent.duration.Duration;

import java.util.concurrent.TimeUnit;

/**
 * Created by zacharywebert on 2/7/15.
 */
public class ChatController {


    static {

        //subscribe to the message channel
        Akka.system().scheduler().scheduleOnce(
                Duration.create(10, TimeUnit.MILLISECONDS),
                new Runnable() {
                    public void run() {

                        //System.out.print("test" + j.toString());
                        //j.subscribe(new MyListener(), "Messages");
                    }
                },
                Akka.system().dispatcher()
        );
    }

    public static void join(final String username, WebSocket.In<String> in, WebSocket.Out<String> out) {
        System.out.println("******* Join called ********");
        in.onMessage(str -> {
            try {
                Jedis j = play.Play.application().plugin(RedisPlugin.class).jedisPool().getResource();
                System.out.println("Jeddis success " + j);
            } catch (Exception e ){
                System.out.println(e);
            }
            try {
                //All messages are pushed through the pub/sub channel
                //j.publish("Messages", str);
            } finally {
                //play.Play.application().plugin(RedisPlugin.class).jedisPool().returnResource(j);
            }
        });
    }

    public static void message(String message) {
        System.out.println(message);
    }

//    public static class MyListener extends JedisPubSub {
//        @Override
//        public void onMessage(String channel, String messageBody) {
//            //Process messages from the pub/sub channel
//            ChatController.message(messageBody);
//        }
//        @Override
//        public void onPMessage(String arg0, String arg1, String arg2) {
//        }
//        @Override
//        public void onPSubscribe(String arg0, int arg1) {
//        }
//        @Override
//        public void onPUnsubscribe(String arg0, int arg1) {
//        }
//        @Override
//        public void onSubscribe(String arg0, int arg1) {
//        }
//        @Override
//        public void onUnsubscribe(String arg0, int arg1) {
//        }
//    }
}
