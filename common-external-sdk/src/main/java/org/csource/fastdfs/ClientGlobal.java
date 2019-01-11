/**
 * Copyright (C) 2008 Happy Fish / YuQing
 * <p>
 * FastDFS Java Client may be copied only under the terms of the GNU Lesser
 * General Public License (LGPL).
 * Please visit the FastDFS Home Page http://www.csource.org/ for more detail.
 **/

package org.csource.fastdfs;

import org.csource.common.MyException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Global variables
 *
 * @author Happy Fish / YuQing
 * @version Version 1.11
 */
public class ClientGlobal {

    public static final int DEFAULT_CONNECT_TIMEOUT = 5; //second
    public static final int DEFAULT_NETWORK_TIMEOUT = 30; //second
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final boolean DEFAULT_HTTP_ANTI_STEAL_TOKEN = false;
    public static final String DEFAULT_HTTP_SECRET_KEY = "FastDFS1234567890";
    public static final int DEFAULT_HTTP_TRACKER_HTTP_PORT = 80;

    public static int g_connect_timeout = DEFAULT_CONNECT_TIMEOUT * 1000; //millisecond
    public static int g_network_timeout = DEFAULT_NETWORK_TIMEOUT * 1000; //millisecond
    public static String g_charset = DEFAULT_CHARSET;
    public static boolean g_anti_steal_token = DEFAULT_HTTP_ANTI_STEAL_TOKEN; //if anti-steal token
    public static String g_secret_key = DEFAULT_HTTP_SECRET_KEY; //generage token secret key
    public static int g_tracker_http_port = DEFAULT_HTTP_TRACKER_HTTP_PORT;

    public static TrackerGroup g_tracker_group;

    private ClientGlobal() {
    }

    /**
     * @param config
     * @throws IOException
     * @throws MyException
     */
    public static void init(InitConfig config) throws IOException, MyException {
        String[] szTrackerServers;
        String[] parts;
        if (config == null) {
            throw new IllegalArgumentException("Init config can not be null");
        }
        g_connect_timeout = config.getConnectTimeout();
        if (g_connect_timeout < 0) {
            g_connect_timeout = DEFAULT_CONNECT_TIMEOUT;
        }
        g_connect_timeout *= 1000; //millisecond

        g_network_timeout = config.getNetworkTimeout();
        if (g_network_timeout < 0) {
            g_network_timeout = DEFAULT_NETWORK_TIMEOUT;
        }
        g_network_timeout *= 1000; //millisecond

        g_charset = config.getCharset();

        szTrackerServers = config.getTrackerServers();
        if (szTrackerServers == null || szTrackerServers.length <= 0) {
            throw new MyException("the tracker_server not found");
        }
        InetSocketAddress[] tracker_servers = new InetSocketAddress[szTrackerServers.length];
        for (int i = 0; i < szTrackerServers.length; i++) {
            parts = szTrackerServers[i].split("\\:", 2);
            if (parts.length != 2) {
                throw new MyException("the value of item \"tracker_server\" is invalid, the correct format is <host:port>");
            }

            tracker_servers[i] = new InetSocketAddress(parts[0].trim(), Integer.parseInt(parts[1].trim()));
        }
        g_tracker_group = new TrackerGroup(tracker_servers);
        g_tracker_http_port = config.getHttpTrackerPort();
        g_anti_steal_token = config.isHttpAntiStealToken();
        if (g_anti_steal_token) {
            g_secret_key = config.getHttpSecretKey();
        }
    }

    /**
     * construct Socket object
     *
     * @param ip_addr ip address or hostname
     * @param port    port number
     * @return connected Socket object
     */
    public static Socket getSocket(String ip_addr, int port) throws IOException {
        Socket sock = new Socket();
        sock.setSoTimeout(ClientGlobal.g_network_timeout);
        sock.connect(new InetSocketAddress(ip_addr, port), ClientGlobal.g_connect_timeout);
        return sock;
    }

    /**
     * construct Socket object
     *
     * @param addr InetSocketAddress object, including ip address and port
     * @return connected Socket object
     */
    public static Socket getSocket(InetSocketAddress addr) throws IOException {
        Socket sock = new Socket();
        sock.setSoTimeout(ClientGlobal.g_network_timeout);
        sock.connect(addr, ClientGlobal.g_connect_timeout);
        return sock;
    }

    public static int getG_connect_timeout() {
        return g_connect_timeout;
    }

    public static void setG_connect_timeout(int connect_timeout) {
        ClientGlobal.g_connect_timeout = connect_timeout;
    }

    public static int getG_network_timeout() {
        return g_network_timeout;
    }

    public static void setG_network_timeout(int network_timeout) {
        ClientGlobal.g_network_timeout = network_timeout;
    }

    public static String getG_charset() {
        return g_charset;
    }

    public static void setG_charset(String charset) {
        ClientGlobal.g_charset = charset;
    }

    public static int getG_tracker_http_port() {
        return g_tracker_http_port;
    }

    public static void setG_tracker_http_port(int tracker_http_port) {
        ClientGlobal.g_tracker_http_port = tracker_http_port;
    }

    public static boolean getG_anti_steal_token() {
        return g_anti_steal_token;
    }

    public static boolean isG_anti_steal_token() {
        return g_anti_steal_token;
    }

    public static void setG_anti_steal_token(boolean anti_steal_token) {
        ClientGlobal.g_anti_steal_token = anti_steal_token;
    }

    public static String getG_secret_key() {
        return g_secret_key;
    }

    public static void setG_secret_key(String secret_key) {
        ClientGlobal.g_secret_key = secret_key;
    }

    public static TrackerGroup getG_tracker_group() {
        return g_tracker_group;
    }

    public static void setG_tracker_group(TrackerGroup tracker_group) {
        ClientGlobal.g_tracker_group = tracker_group;
    }

    public static String configInfo() {
        String trackerServers = "";
        if (g_tracker_group != null) {
            InetSocketAddress[] trackerAddresses = g_tracker_group.tracker_servers;
            for (InetSocketAddress inetSocketAddress : trackerAddresses) {
                if (trackerServers.length() > 0) trackerServers += ",";
                trackerServers += inetSocketAddress.toString().substring(1);
            }
        }
        return "{"
                + "\n  g_connect_timeout(ms) = " + g_connect_timeout
                + "\n  g_network_timeout(ms) = " + g_network_timeout
                + "\n  g_charset = " + g_charset
                + "\n  g_anti_steal_token = " + g_anti_steal_token
                + "\n  g_secret_key = " + g_secret_key
                + "\n  g_tracker_http_port = " + g_tracker_http_port
                + "\n  trackerServers = " + trackerServers
                + "\n}";
    }

}
