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

import com.alibaba.otter.canal.protocol.CanalEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import java.util.List;

@SpringBootApplication
public class CanalTestApplication {
    public static final Logger logger = LoggerFactory.getLogger(CanalTestApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(CanalTestApplication.class);
    }

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
}
