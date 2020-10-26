/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 abel533@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.mybatis.canal;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Configuration
@ConditionalOnProperty(prefix = CanalProperties.PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CanalProperties.class)
public class CanalAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(CanalAutoConfiguration.class);

    private final CanalProperties canalProperties;

    public CanalAutoConfiguration(CanalProperties canalProperties) {
        this.canalProperties = canalProperties;
    }

    @Bean
    @ConditionalOnProperty(prefix = CanalProperties.PREFIX, name = "type", havingValue = "CLUSTER")
    public CanalConnector clusterConnector() {
        log.debug("集群模式: {}", canalProperties);
        return CanalConnectors.newClusterConnector(canalProperties.getAddress() + ":" + canalProperties.getPort(),
                canalProperties.getDestination(), canalProperties.getUsername(), canalProperties.getPassword());
    }

    @Bean
    @ConditionalOnProperty(prefix = CanalProperties.PREFIX, name = "type", havingValue = "SINGLE")
    public CanalConnector singleConnector() {
        log.debug("单机模式: {}", canalProperties);
        return CanalConnectors.newSingleConnector(new InetSocketAddress(canalProperties.getAddress(), canalProperties.getPort()),
                canalProperties.getDestination(), canalProperties.getUsername(), canalProperties.getPassword());
    }


    @Bean
    @ConditionalOnBean(CanalConnector.class)
    public InitCanalListener initCanalListener() {
        return new InitCanalListener();
    }

    public class InitCanalListener {

        @Autowired
        private CanalConnector connector;

        @Autowired
        private CanalProperties canalProperties;

        @Autowired
        private ApplicationEventPublisher publisher;

        private Thread receiveThread;

        @PostConstruct
        public void init() {
            log.info("连接 canal");
            connector.connect();
            log.info("连接 canal 成功");
            connector.subscribe(canalProperties.getFilter());
            log.info("订阅 " + canalProperties.getFilter() + " 成功");
            receiveThread = new Thread(null, () -> {
                log.info("开始监听 canal");
                Integer batchSize = canalProperties.getBatchSize();
                batchSize = (batchSize == null || batchSize <= 0) ? 10 : batchSize;
                log.info("开始监听 canal, 每批次大小: " + batchSize);
                while (!Thread.currentThread().isInterrupted()) {
                    Message message = connector.getWithoutAck(batchSize);
                    long batchId = message.getId();
                    int size = message.getEntries().size();
                    if (batchId == -1 || size == 0) {
                        try {
                            log.debug("no data, sleep 500ms");
                            Thread.sleep(500);
                        } catch (InterruptedException ignore) {
                        }
                    } else {
                        publisher.publishEvent(new CanalMessageEvent(message));
                    }
                    connector.ack(batchId);
                }
                log.info("停止监听");
            }, "canal-message-listener");
            receiveThread.start();
        }

        @PreDestroy
        public void destroy() {
            log.info("关闭监听");
            receiveThread.interrupt();
            log.info("断开 canal 连接");
            connector.disconnect();
            log.info("canal 客户端关闭成功");
        }
    }

}
