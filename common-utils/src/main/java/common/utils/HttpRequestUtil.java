package common.utils;


import com.alibaba.fastjson.JSON;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.NoHttpResponseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

public class HttpRequestUtil {

    private static Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    private static final int TIMEOUT = 150 * 1000;

    private static CloseableHttpClient httpClient = null;

    private final static Object syncLock = new Object();

    // 设置超时
    private static void config(HttpRequestBase httpRequestBase) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(TIMEOUT)
                .setConnectTimeout(TIMEOUT)
                .setSocketTimeout(TIMEOUT)
                .build();
        httpRequestBase.setConfig(requestConfig);
    }

    private static HttpClientContext createBasicAuthContext(CredentialsProvider credentialsProvider, String url) {
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(getHttpHost(url), basicAuth);

        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credentialsProvider);
        context.setAuthCache(authCache);
        return context;
    }

    private static HttpHost getHttpHost(String url) {
        String hostname = url.split("/")[2];
        int port = 80;
        if (hostname.contains(":")) {
            String[] arr = hostname.split(":");
            hostname = arr[0];
            port = Integer.parseInt(arr[1]);
        }

        HttpHost httpHost = new HttpHost(hostname, port);
        return httpHost;
    }

    /**
     * @param username
     * @param password
     * @return
     */
    public static CredentialsProvider getBasicCredentialsProvider(String url, String username, String password) {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        Credentials defaultCreds = new UsernamePasswordCredentials(username, password);

        HttpHost host = getHttpHost(url);
        credsProvider.setCredentials(new AuthScope(host.getHostName(), host.getPort()), defaultCreds);

        return credsProvider;
    }

    /**
     * 获取httpClient
     *
     * @param url
     * @return
     */
    public static CloseableHttpClient getHttpClient(String url) {
        if (httpClient == null) {
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient(200, 40, 100, getHttpHost(url));
                }
            }
        }
        return httpClient;
    }


    private static CloseableHttpClient createHttpClient(int maxTotal, int maxPerRoute, int maxRoute, HttpHost httpHost) {
        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory
                .getSocketFactory();
        LayeredConnectionSocketFactory sslsf = SSLConnectionSocketFactory
                .getSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder
                .<ConnectionSocketFactory>create()
                .register("http", plainsf)
                .register("https", sslsf)
                .build();

        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(
                registry);

        // 将最大连接数增加
        cm.setMaxTotal(maxTotal);
        // 将每个路由基础的连接增加
        cm.setDefaultMaxPerRoute(maxPerRoute);

        // 将目标主机的最大连接数增加
        cm.setMaxPerRoute(new HttpRoute(httpHost), maxRoute);

        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = (exception, executionCount, context) -> {
            if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                return false;
            }
            if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                return true;
            }
            if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                return false;
            }
            if (exception instanceof InterruptedIOException) {// 超时
                return false;
            }
            if (exception instanceof UnknownHostException) {// 目标服务器不可达
                return false;
            }
            if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                return false;
            }
            if (exception instanceof SSLException) {// SSL握手异常
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求是幂等的，就再次尝试
            if (!(request instanceof HttpEntityEnclosingRequest)) {
                return true;
            }
            return false;
        };

        HttpClientBuilder clientBuilder = HttpClients.custom();
        clientBuilder.setConnectionManager(cm)
                .setRetryHandler(httpRequestRetryHandler);
        CloseableHttpClient httpClient = clientBuilder.build();
        return httpClient;
    }


    public static <T> T doPost(String url, String json, Class<T> clz) throws Exception {
        String result = doPost(url, json);
        return JSON.parseObject(result, clz);
    }


    public static String doPostWithCredentials(String url, String jsonStr, CredentialsProvider credentialsProvider) throws Exception {
        try {
            CloseableHttpClient client = getHttpClient(url);
            HttpPost post = new HttpPost(url);
            config(post);
            logger.info("Executing request: " + post.getRequestLine());
            if (!StringUtils.isEmpty(jsonStr)) {
                StringEntity s = new StringEntity(jsonStr, "UTF-8");
                s.setContentEncoding("UTF-8");
                s.setContentType("application/json");
                post.setEntity(s);
            }
            String responseBody = null;
            if (credentialsProvider != null) {
                HttpClientContext clientContext = createBasicAuthContext(credentialsProvider, url);
                responseBody = client.execute(post, getStringResponseHandler(url), clientContext);
            } else {
                responseBody = client.execute(post, getStringResponseHandler(url));
            }
            return responseBody;
        } catch (Exception e) {
            if (e instanceof HttpHostConnectException || e.getCause() instanceof ConnectException) {
                throw new ConnectException("====> 连接服务器" + url + "失败： " + e.getMessage());
            }
            logger.error("HttpRequestUtil.doPost: " + e.getMessage(), e);
        }
        return null;
    }

    public static String doPost(String url, String json) throws Exception {
        return doPostWithCredentials(url, json, null);
    }

    private static ResponseHandler<String> getStringResponseHandler(String url) {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity, "UTF-8") : null;
            } else {
                logger.error("Unexpected response from request: " + url + "  response status: " + status);
                throw new ClientProtocolException("Unexpected response status: " + status);
            }
        };
    }

    public static <T> T doGet(String url, Class<T> clz) throws Exception {
        String result = doGet(url);
        return JSON.parseObject(result, clz);
    }

    public static String doGetWithCredentials(String url, CredentialsProvider credentialsProvider) throws Exception {
        try {
            CloseableHttpClient client = getHttpClient(url);
            HttpGet httpget = new HttpGet(url);
            config(httpget);
            logger.info("Executing request: " + httpget.getRequestLine());
            String responseBody = null;
            if (credentialsProvider != null) {
                HttpClientContext clientContext = createBasicAuthContext(credentialsProvider, url);
                responseBody = client.execute(httpget, getStringResponseHandler(url), clientContext);
            } else {
                responseBody = client.execute(httpget, getStringResponseHandler(url));
            }
            return responseBody;
        } catch (Exception e) {
            if (e instanceof HttpHostConnectException || e.getCause() instanceof ConnectException) {
                throw e;
            }
            logger.error("HttpRequestUtil.doGet: " + e.getMessage(), e);
        }
        return null;
    }

    public static String doGet(String url) throws Exception {
        return doGetWithCredentials(url, null);
    }

}
