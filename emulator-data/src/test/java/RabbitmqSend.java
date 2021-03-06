import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import io.j1st.data.rabbitmq.RabittMQSend;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by xiaopeng on 2017/5/23.
 */
public class RabbitmqSend {
    private final static Logger logger = LoggerFactory.getLogger(RabittMQSend.class);
    //队列名称
    private final static String QUEUE_NAME = "test";
    public static final String ROUTINGKEY = "zxcvASDF123$";
    @Test
    public  void sendRabbitMQ() {
        String msg="你好!";
        /**
         * 创建连接连接到MabbitMQ
         */
        try {

            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("127.0.0.1");
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setPort(5672);
            //创建一个连接
            Connection connection = factory.newConnection();
            //创建一个频道
            Channel channel = connection.createChannel();
            //指定一个队列
            channel.exchangeDeclare(QUEUE_NAME, "topic", true);
            //往队列中发出一条消息
            channel.basicPublish(QUEUE_NAME, ROUTINGKEY, MessageProperties.BASIC, msg.getBytes());
            //关闭频道和连接
            channel.close();
            connection.close();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
