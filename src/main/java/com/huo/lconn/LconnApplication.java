package com.huo.lconn;

import com.huo.lconn.backend.MessageProvider;
import com.huo.lconn.backend.TcpMessageProvider;
import com.huo.lconn.client.ClientServer;
import com.huo.lconn.constant.Constant;
import com.huo.lconn.rpc.client.RpcClientManager;
import com.huo.lconn.rpc.client.RpcMultiClient;
import com.huo.lconn.rpc.server.RpcServer;
import com.huo.lconn.utils.ServiceManager;
import com.sun.net.httpserver.HttpServer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.ResourceLeakDetector;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.*;

@Log4j2
@SpringBootApplication
@EnableScheduling
public class LconnApplication {

    public static void main(String[] args) {
        SpringApplication.run(LconnApplication.class, args);
        EventLoopGroup workerGroup = new NioEventLoopGroup(Constant.NETTY_WORKER_NUM);
        RpcClientManager.initAndStart(new RpcMultiClient(workerGroup));
        ClientServer clientServer = new ClientServer(workerGroup);
        MessageProvider messageProvider = new TcpMessageProvider(workerGroup);
        RpcServer rpcServer = new RpcServer(workerGroup);
        //TODO 需要HTTP和WebSocket的消息处理
        try {
            ResourceLeakDetector.Level resourceLeakDetectLv = ResourceLeakDetector.Level.valueOf("SIMPLE");
            ResourceLeakDetector.setLevel(resourceLeakDetectLv);
            addShutdownHook(clientServer, messageProvider, rpcServer);
            ExecutorService executorService = Executors.newCachedThreadPool(r -> new Thread(r, "AliveMain"));
            executorService.execute(() -> clientServer.start());
            executorService.execute(() -> messageProvider.startProvide());
            executorService.execute(() -> rpcServer.start());
        } catch (Throwable t) {
            log.error("initialize pushserver error.", t);
        }
    }

    private static void addShutdownHook(final ClientServer clientServer, final MessageProvider messageProvider, final RpcServer rpcServer) {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                rpcServer.shutdown();
                messageProvider.shutdown();
                clientServer.shutdown();
            }
        }));
    }
}
