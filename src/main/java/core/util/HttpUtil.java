package core.util;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * http请求类
 */
public class HttpUtil {
    //连接池最大连接数
    private static final Integer HTTP_POOL_MAX_TOTAL = 30000;
    //单个路由默认最大连接数
    private static final Integer HTTP_POOL_MAX_PER_ROUTE = 1000;

    //不活跃链接剔除时间
    private static final Integer HTTP_POOL_INACTIVITY_EXCLUDE_TIME = 5000;

    /**
     * 从连接池获取连接的超时时间毫秒数
     */
    private final static int CONNECTION_REQUEST_TIMEOUT = 5000;
    /**
     * 与目标主机建立连接的超时时间毫秒数
     */
    private final static int CONNECT_TIMEOUT = 5000;
    /**
     * 数据传输超时时间毫秒数
     */
    private final static int SOCKET_TIMEOUT = 5000;

    /**
     * 全局保存一个httpClient 提高性能
     */
    private CloseableHttpClient httpClient;

    private HttpUtil() {
        createHttpClient();
    }

    private static HttpUtil httpUtil = null;

    public static HttpUtil getInstance() {
        if (httpUtil == null) {
            synchronized (HttpUtil.class) {
                if (httpUtil == null) {
                    httpUtil = new HttpUtil();
                }
            }
        }
        return httpUtil;
    }

    private void createHttpClient() {
        //设置Keep alive 当默认没有timeout时候设置成60s
        ConnectionKeepAliveStrategy strategy = (response, context) -> {
            HeaderElementIterator it = new BasicHeaderElementIterator
                    (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase
                        ("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 60 * 1000;
        };

        //socket连接工厂
        RegistryBuilder<ConnectionSocketFactory> registryBuilder = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.INSTANCE);
        try {
            registryBuilder.register("https", new SSLConnectionSocketFactory(createIgnoreVerifySSL()));
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registryBuilder.build());

        connectionManager.setMaxTotal(HTTP_POOL_MAX_TOTAL);
        connectionManager.setDefaultMaxPerRoute(HTTP_POOL_MAX_PER_ROUTE);
        connectionManager.setValidateAfterInactivity(HTTP_POOL_INACTIVITY_EXCLUDE_TIME);

        httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setKeepAliveStrategy(strategy)
                .build();
    }

    /**
     * 创建一个绕过证书验证的SSLContext
     *
     * @return
     */
    private static SSLContext createIgnoreVerifySSL() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {

            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        };
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        return sslContext;
    }


    public HttpResponse get(HttpRequestParams req) throws Exception {
        return get(req, "utf-8");
    }

    /**
     * get请求
     *
     * @param req
     * @return
     */
    public HttpResponse get(HttpRequestParams req, String charset) throws Exception {
        HttpResponse response = new HttpResponse();

        URIBuilder builder = new URIBuilder(req.getUrl());

        Map<String, String> params = req.getParams();

        if (params != null && params.size() > 0) {
            params.forEach(builder::setParameter);
        }

        HttpGet httpGet = new HttpGet(builder.build());

        httpGet.setConfig(RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setRedirectsEnabled(true)
                .build());

        Map<String, String> headers = req.getHeaders();

        if (headers != null && headers.size() > 0) {
            headers.forEach(httpGet::addHeader);
        }

        org.apache.http.HttpResponse httpResponse = httpClient.execute(httpGet);

        int statusCode = httpResponse.getStatusLine().getStatusCode();

        response.setStatusCode(statusCode);
        InputStream inputStream = httpResponse.getEntity().getContent();
        String result = new BufferedReader(new InputStreamReader(inputStream, charset))
                .lines().collect(Collectors.joining(System.lineSeparator()));

        response.setBody(result);

        return response;
    }

    /**
     * post请求， 入参为json
     *
     * @param req
     * @return
     */
    public HttpResponse post4Json(HttpRequestJson req) throws Exception {
        HttpResponse res = new HttpResponse();
        HttpPost post = new HttpPost(req.getUrl());

        Map<String, String> headers = req.getHeaders();

        if (headers != null && headers.size() > 0) {
            headers.forEach(post::setHeader);
        }

        post.setConfig(RequestConfig.custom().
                setConnectTimeout(CONNECT_TIMEOUT).setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT).setRedirectsEnabled(true).build());

        StringEntity body = new StringEntity(req.getBody(), Charset.forName("UTF-8"));
        body.setContentEncoding("UTF-8");
        body.setContentType("application/json");
        post.setEntity(body);

        org.apache.http.HttpResponse httpResponse = httpClient.execute(post);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        res.setStatusCode(statusCode);
        res.setBody(EntityUtils.toString(httpResponse.getEntity(), "UTF-8"));
        return res;
    }

    public HttpResponse post4Para(HttpRequestParams req) throws Exception {
        return post4Para(req, CONNECT_TIMEOUT, CONNECTION_REQUEST_TIMEOUT, SOCKET_TIMEOUT);
    }

    public HttpResponse post4Para(HttpRequestParams req, int connectTimeout, int connectionRequestTimeout, int socketTimeout) throws Exception {
        HttpResponse res = new HttpResponse();

        URIBuilder builder = new URIBuilder(req.getUrl());

        Map<String, String> params = req.getParams();

        if (params != null && params.size() > 0) {
            params.forEach(builder::setParameter);
        }

        HttpPost httpPost = new HttpPost(builder.build());

        httpPost.setConfig(RequestConfig.custom()
                .setConnectTimeout(connectTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setSocketTimeout(socketTimeout)
                .setRedirectsEnabled(true)
                .build());

        Map<String, String> headers = req.getHeaders();

        if (headers != null && headers.size() > 0) {
            headers.forEach(httpPost::setHeader);
        }

        org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPost);

        int statusCode = httpResponse.getStatusLine().getStatusCode();

        res.setStatusCode(statusCode);

        res.setBody(EntityUtils.toString(httpResponse.getEntity()));

        return res;
    }

    /**
     * put请求
     * eg.
     * parameters: http://127.0.0.1:3301/put      map: {"id", "1"} {"orderId", "2"}
     * url :http://127.0.0.1:3301/put?id=1&orderId=2
     *
     * @param req
     * @return
     */
    public HttpResponse put(HttpRequestParams req) throws Exception {
        HttpResponse response = new HttpResponse();

        URIBuilder builder = new URIBuilder(req.getUrl());

        Map<String, String> params = req.getParams();

        if (params != null && params.size() > 0) {
            params.forEach(builder::setParameter);
        }

        HttpPut httpPut = new HttpPut(builder.build());

        httpPut.setConfig(RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setRedirectsEnabled(true)
                .build());

        Map<String, String> headers = req.getHeaders();

        if (headers != null && headers.size() > 0) {
            headers.forEach(httpPut::addHeader);
        }

        org.apache.http.HttpResponse httpResponse = httpClient.execute(httpPut);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        response.setStatusCode(statusCode);
        response.setBody(EntityUtils.toString(httpResponse.getEntity()));
        return response;
    }


    /**
     * delete
     * @param req
     * @return
     * @throws Exception
     */
    public HttpResponse delete(HttpRequestParams req) throws Exception {
        HttpResponse response = new HttpResponse();

        URIBuilder builder = new URIBuilder(req.getUrl());

        Map<String, String> params = req.getParams();

        if (params != null && params.size() > 0) {
            params.forEach(builder::setParameter);
        }

        HttpDelete httpDelete = new HttpDelete(builder.build());

        httpDelete.setConfig(RequestConfig.custom()
                .setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                .setSocketTimeout(SOCKET_TIMEOUT)
                .setRedirectsEnabled(true)
                .build());

        Map<String, String> headers = req.getHeaders();

        if (headers != null && headers.size() > 0) {
            headers.forEach(httpDelete::addHeader);
        }

        org.apache.http.HttpResponse httpResponse = httpClient.execute(httpDelete);

        int statusCode = httpResponse.getStatusLine().getStatusCode();
        response.setStatusCode(statusCode);
        response.setBody(EntityUtils.toString(httpResponse.getEntity()));
        return response;
    }
}
