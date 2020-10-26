# Canal Spring Boot Starter

为使 canal 的相关配置可以使用 SpringBoot 风格进行配置,对相关配置进行了简单封装。

## 提醒

当前项目只是示例，没有继续维护的计划。

当前项目也没有发布到 Maven 中央仓库，有需要的自己修改编译使用。

## 1.添加依赖

```xml
<dependency>
    <groupId>io.mybatis</groupId>
    <artifactId>canal-spring-boot-starter</artifactId>
    <version>20.10-SNAPSHOT</version>
</dependency>
```

## 2.添加配置

在`src/main/resources/application.properties` 文件下添加 canal 所需配置,示例如下

```properties
#类型，单机SINGLE，zookeeper集群CLUSTER
canal.type=SINGLE
#主机名或IP
canal.address=10.10.10.226
#端口号
canal.port=11111
#目标实例
canal.destination=example
#订阅规则
canal.filter=.*\\..*
#分批大小
canal.batchSize=100
```

## 3.监听事件

Canal 监听到数据库变化后，会通过 Spring `ApplicationEvent` 方式发布 `CanalMessageEvent` 事件，从事件中 `Message getSource()` 即可拿到变化的消息。

事件订阅实例：

```java
@EventListener
public void canalMessageEvent(CanalMessageEvent messageEvent) {
    List<CanalEntry.Entry> entries = messageEvent.getSource().getEntries();
    for (CanalEntry.Entry entry : entries) {
        if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
                || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
            continue;
        }

        CanalEntry.RowChange rowChange = null;
        try {
            rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
        } catch (Exception e) {
            logger.error("ERROR parse : " + e.getMessage(), e);
            throw new RuntimeException("ERROR parse : " + e.getMessage(), e);
        }
        CanalEntry.EventType eventType = rowChange.getEventType();
        CanalEntry.Header header = entry.getHeader();
        logger.info("================> binlog: [{}:{}] , name [{}, {}], eventType: {}",
                header.getLogfileName(), header.getLogfileOffset(),
                header.getSchemaName(), header.getTableName(),
                eventType);
        for (CanalEntry.RowData rowData : rowChange.getRowDatasList()) {
            if (eventType == CanalEntry.EventType.DELETE) {
                printColumn(rowData.getBeforeColumnsList());
            } else if (eventType == CanalEntry.EventType.INSERT) {
                printColumn(rowData.getAfterColumnsList());
            } else {
                logger.info("----------------> before: ");
                printColumn(rowData.getBeforeColumnsList());
                logger.info("----------------> after: ");
                printColumn(rowData.getAfterColumnsList());
            }
        }
    }
}

private void printColumn(List<CanalEntry.Column> columns) {
    for (CanalEntry.Column column : columns) {
        logger.info(column.getName() + " : " + column.getValue() + "     ---> update: " + column.getUpdated());
    }
}
```