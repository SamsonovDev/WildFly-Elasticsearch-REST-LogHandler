package org.wildfly.elasticsearch.log.config;

import java.net.URL;

/**
 * Settings of ElasticsearchLogWriter
 * @author s.samsonov
 */
public class Settings {
    private boolean checkRetryInterval = true;
    private int connectTimeout;
    private int readTimeout;
    private int maxQueueSize;
    private int maxRetries;
    private long retryInterval;
    private URL url;

    public Settings() {}

    public boolean getCheckRetryInterval() {
        return this.checkRetryInterval;
    }

    public void setCheckRetryInterval(boolean checkRetryInterval) {
        this.checkRetryInterval = checkRetryInterval;
    }

    public int getConnectTimeout() {
        return this.connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return this.readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxQueueSize() {
        return this.maxQueueSize;
    }

    public void setMaxQueueSize(int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public int getMaxRetries() {
        return this.maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long getRetryInterval() {
        return this.retryInterval;
    }

    public void setRetryInterval(long retryInterval) {
        this.retryInterval = retryInterval;
    }

    public URL getUrl() {
        return this.url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }
}
