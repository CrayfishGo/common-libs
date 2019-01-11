package org.csource.fastdfs;

/**
 *
 */
public class InitConfig {

    /**
     * 单位 秒
     */
    private int connectTimeout = 5;

    /**
     * 单位 秒
     */
    private int networkTimeout = 30;

    private String charset = "UTF-8";

    private String httpSecretKey;

    private int httpTrackerPort = 80;

    private boolean httpAntiStealToken;

    private String[] trackerServers;

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getNetworkTimeout() {
        return networkTimeout;
    }

    public void setNetworkTimeout(int networkTimeout) {
        this.networkTimeout = networkTimeout;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getHttpSecretKey() {
        return httpSecretKey;
    }

    public void setHttpSecretKey(String httpSecretKey) {
        this.httpSecretKey = httpSecretKey;
    }

    public int getHttpTrackerPort() {
        return httpTrackerPort;
    }

    public void setHttpTrackerPort(int httpTrackerPort) {
        this.httpTrackerPort = httpTrackerPort;
    }

    public boolean isHttpAntiStealToken() {
        return httpAntiStealToken;
    }

    public void setHttpAntiStealToken(boolean httpAntiStealToken) {
        this.httpAntiStealToken = httpAntiStealToken;
    }

    public String[] getTrackerServers() {
        return trackerServers;
    }

    public void setTrackerServers(String[] trackerServers) {
        this.trackerServers = trackerServers;
    }
}
