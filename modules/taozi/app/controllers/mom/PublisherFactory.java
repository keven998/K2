package controllers.mom;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import play.Configuration;

import java.io.IOException;

/**
 * Created by Heaven on 2015/3/26.
 */
public class PublisherFactory {
    private static PublisherFactory factory = null;

    Log logger = LogFactory.getLog(this.getClass());

    private Connection connection = null;

    private String host = null;
    private int port = 0;
    private boolean durable = false;
    private String exchangeType = null;

    private PublisherFactory() {
        // 读取配置信息
        Configuration config = Configuration.root().getConfig("mom");
        host = config.getString("host", "localhost");
        port = config.getInt("port", 5672);
        durable = config.getBoolean("durable", false);
        exchangeType = config.getString("exchangeType", "topic");

        // 创建连接
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
//            e.printStackTrace();
            logger.error(this.getClass().getSimpleName() + " can not connect to " + host + ":" + port);
        }
    }

    public MessagePublisher getMessagePublisher(String exchangeName) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            channel.exchangeDeclare(exchangeName, this.exchangeType, this.durable);
        } catch (Exception e) {
            logger.error("error curried when creating MessagePublisher");
        }
        return new MessagePublisher(exchangeName, channel);
    }

    public TaskPublisher getTaskPublisher(String exchangeName) {
        Channel channel = null;
        try {
            channel = connection.createChannel();
            // Celery 中对exchangeType  和 durable 都有限定，因此不使用默认配置
            channel.exchangeDeclare(exchangeName, "direct", true);
        } catch (Exception e) {
            logger.error("error curried when creating TaskPublisher");
        }
        return new TaskPublisher(exchangeName, channel);
    }

    public static PublisherFactory getInstance() {
        if (factory != null)
            return factory;

        synchronized ("factory") {
            if (factory != null)
                return factory;
            factory = new PublisherFactory();
        }
        return factory;
    }

}
