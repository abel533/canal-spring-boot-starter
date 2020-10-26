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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.lang.NonNull;

@ConfigurationProperties(prefix = CanalProperties.PREFIX)
public class CanalProperties {

    public static final String PREFIX = "canal";

    /**
     * 是否启用xxljob
     */
    private Boolean enabled;
    /**
     * 连接类型
     */
    @NonNull
    private ConnectorType type;
    /**
     * 连接地址
     */
    @NonNull
    private String address;
    /**
     * 连接端口
     */
    @NonNull
    private Integer port;
    /**
     * 目标实例
     */
    @NonNull
    private String destination;
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
    /**
     * 订阅内容过滤器
     */
    private String filter;
    /**
     * 批量处理大小
     */
    private Integer batchSize;

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public ConnectorType getType() {
        return type;
    }

    public void setType(ConnectorType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CanalProperties{");
        sb.append("enabled=").append(enabled);
        sb.append(", type=").append(type);
        sb.append(", address='").append(address).append('\'');
        sb.append(", port=").append(port);
        sb.append(", destination='").append(destination).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", filter='").append(filter).append('\'');
        sb.append(", batchSize=").append(batchSize);
        sb.append('}');
        return sb.toString();
    }

    /**
     * 连接类型
     */
    public enum ConnectorType {
        /**
         * 单节点
         */
        SINGLE,
        /**
         * 集群
         */
        CLUSTER
    }
}
