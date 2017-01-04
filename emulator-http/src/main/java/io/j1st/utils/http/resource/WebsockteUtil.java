package io.j1st.utils.http.resource;

import io.j1st.storage.DataMongoStorage;
import io.j1st.storage.MongoStorage;
import io.j1st.storage.entity.GenData;
import io.j1st.utils.http.entity.ResultEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.validation.Valid;
import javax.websocket.*;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by xiaopeng on 2017/1/3.
 */

@Path("/data")
@Produces(MediaType.APPLICATION_JSON)
public class WebsockteUtil extends AbstractResource {
    private String nickname;
    private Session session;
    private static final String GUEST_PREFIX = "Guest";
    private static final AtomicInteger connectionIds = new AtomicInteger(0);
    private static final Set<WebsockteUtil> connections =
            new CopyOnWriteArraySet<>();

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(GenDataAddUtil.class);

    public WebsockteUtil(MongoStorage mongo, DataMongoStorage dataMongoStorage) {
        super(mongo, dataMongoStorage);
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public ResultEntity postticket(@Valid Map data) {
        logger.debug("Process signIn request: {}", data);
        // TODO: validate name password mobile email format
        Map<String, Object> r = new HashMap<>();
        r.put("username", "test");
        return new ResultEntity<>(r);
    }

    @OnOpen
    public void start(Session session) {
        this.session = session;
        connections.add(this);
        String message = String.format("* %s %s", nickname, "has joined.");
        broadcast(message);
    }

    @OnClose
    public void end() {
        connections.remove(this);
        String message = String.format("* %s %s",
                nickname, "has disconnected.");
        broadcast(message);
    }

    @OnMessage
    public void incoming(String message) {
        // Never trust the client
        // TODO: 过滤输入的内容
        broadcast(message);
    }

    @OnError
    public void onError(Throwable t) throws Throwable {
        System.out.println("Chat Error: " + t.toString());
    }

    private static void broadcast(String msg) {
        for (WebsockteUtil client : connections) {
            try {
                synchronized (client) {
                    client.session.getBasicRemote().sendText(msg);
                }
            } catch (IOException e) {
                System.out.println("Chat Error: Failed to send message to client");
                connections.remove(client);
                try {
                    client.session.close();
                } catch (IOException e1) {
                    // Ignore
                }
                String message = String.format("* %s %s",
                        client.nickname, "has been disconnected.");
                broadcast(message);
            }
        }
    }
}
