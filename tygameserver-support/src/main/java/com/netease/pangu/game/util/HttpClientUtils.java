package com.netease.pangu.game.util;


import org.apache.commons.collections.CollectionUtils;
import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpClientUtils {

    public static Charset defaultCharset = Consts.UTF_8;
    public static RequestConfig defaultRequestConfig = RequestConfig.custom()
            .setSocketTimeout(30000)
            .setConnectTimeout(8000)
            .setConnectionRequestTimeout(8000).build();

    private static final int DEFAULT_MAX_CONNECTION_PER_ROUTE = 30;
    private static final int DEFAULT_MAX_CONNECTION_TOTAL = 80;

    private static PoolingHttpClientConnectionManager connManager = null;
    private static CloseableHttpClient httpClient = null;


    public static HttpContext createAuthContext(String url, String userName, String password) {
        // See: https://hc.apache.org/httpcomponents-client-ga/tutorial/html/authentication.html
        URI uri = URI.create(url);
        HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(targetHost.getHostName(), targetHost.getPort()),
                new UsernamePasswordCredentials(userName, password));

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        authCache.put(targetHost, basicAuth);

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);

        return context;
    }

    private static HttpResult createResult(CloseableHttpResponse response) throws IOException {
        HttpResult result = new HttpResult();
        result.statusCode = response.getStatusLine().getStatusCode();

        HttpEntity responseEntity = response.getEntity();
        if (responseEntity != null) {

            result.contentType = ContentType.get(responseEntity);

            byte[] buf = new byte[65536];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = responseEntity.getContent();
            try {
                while (true) {
                    int len = in.read(buf);
                    if (len >= 0) {
                        out.write(buf, 0, len);
                    } else {
                        break;
                    }
                }
                result.content = out.toByteArray();
            } finally {
                in.close();
            }
        }

        return result;
    }

    public static HttpResult execute(HttpRequestBase request, RequestConfig requestConfig, HttpContext context) {
        request.setConfig(requestConfig);
        CloseableHttpResponse response;
        try {
            response = getHttpClient().execute(request, context);
            if (response != null) {
                try {
                    return createResult(response);
                } finally {
                    response.close();
                }
            } else {
                throw new HttpClientException(String.format("Failed %s url: %s. Emtpy response.", request.getMethod(), request.getURI()));
            }

        } catch (HttpClientException e) {
            throw e;    // rethrow

        } catch (Exception e) {
            throw new HttpClientException(String.format("Failed %s url: %s. Reasone: %s", request.getMethod(), request.getURI(), e.getMessage(), e));

        } finally {
            request.releaseConnection();
        }
    }

    public static HttpResult get(String url, Map<String, Object> params, Charset urlCharset, RequestConfig requestConfig, HttpContext context) {
        String fullUrl = generateUrl(url, params, urlCharset);
        return execute(new HttpGet(fullUrl), requestConfig, context);
    }

    public static HttpResult delete(String url, Map<String, Object> params, Charset urlCharset, RequestConfig requestConfig, HttpContext context) {
        String fullUrl = generateUrl(url, params, urlCharset);
        return execute(new HttpDelete(fullUrl), requestConfig, context);
    }

    public static HttpResult post(String url, Map<String, Object> params, Charset charset, RequestConfig requestConfig, HttpContext context) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new UrlEncodedFormEntity(genNameValuePairsFromMap(params), charset));
        return execute(request, requestConfig, context);
    }

    public static HttpResult put(String url, Map<String, Object> params, Charset charset, RequestConfig requestConfig, HttpContext context) {
        HttpPut request = new HttpPut(url);
        request.setEntity(new UrlEncodedFormEntity(genNameValuePairsFromMap(params), charset));
        return execute(request, requestConfig, context);
    }

    public static HttpResult postBinary(String url, byte[] data, ContentType contentType, RequestConfig requestConfig, HttpContext context) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new ByteArrayEntity(data, contentType));
        return execute(request, requestConfig, context);
    }

    public static HttpResult putBinary(String url, byte[] data, ContentType contentType, RequestConfig requestConfig, HttpContext context) {
        HttpPut request = new HttpPut(url);
        request.setEntity(new ByteArrayEntity(data, contentType));
        return execute(request, requestConfig, context);
    }


    public static HttpResult postUsingJson(String url, Map<String, Object> params, RequestConfig requestConfig, HttpContext context) {
        HttpPost request = new HttpPost(url);
        request.setEntity(createJsonEntity(params));
        return execute(request, requestConfig, context);
    }

    public static HttpResult putUsingJson(String url, Map<String, Object> params, RequestConfig requestConfig, HttpContext context) {
        HttpPut request = new HttpPut(url);
        request.setEntity(createJsonEntity(params));
        return execute(request, requestConfig, context);
    }

    public static HttpResult get(String url, Map<String, Object> params, Charset urlCharset) {
        return get(url, params, urlCharset, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult delete(String url, Map<String, Object> params, Charset urlCharset) {
        return delete(url, params, urlCharset, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult post(String url, Map<String, Object> params, Charset charset) {
        return post(url, params, charset, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult put(String url, Map<String, Object> params, Charset charset) {
        return put(url, params, charset, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult postBinary(String url, byte[] data, ContentType contentType) {
        return postBinary(url, data, contentType, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult putBinary(String url, byte[] data, ContentType contentType) {
        return putBinary(url, data, contentType, defaultRequestConfig, HttpClientContext.create());
    }


    public static HttpResult postUsingJson(String url, Map<String, Object> params) {
        return postUsingJson(url, params, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult putUsingJson(String url, Map<String, Object> params) {
        return putUsingJson(url, params, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult get(String url, Map<String, Object> params) {
        return get(url, params, defaultCharset);
    }

    public static HttpResult delete(String url, Map<String, Object> params) {
        return delete(url, params, defaultCharset);
    }

    public static HttpResult post(String url, Map<String, Object> params) {
        return post(url, params, defaultCharset);
    }

    public static HttpResult put(String url, Map<String, Object> params) {
        return put(url, params, defaultCharset);
    }

    public static HttpResult get(String url) {
        return get(url, new HashMap<String, Object>(), defaultCharset);
    }

    public static HttpResult delete(String url) {
        return delete(url, new HashMap<String, Object>(), defaultCharset);
    }

    public static HttpResult post(String url) {
        return post(url, new HashMap<String, Object>(), defaultCharset);
    }

    public static HttpResult put(String url) {
        return put(url, new HashMap<String, Object>(), defaultCharset);
    }

    public static HttpResult execute(String url, RequestMethod method, Map<String, Object> params) {
        return execute(url, method, params, defaultCharset, defaultRequestConfig, HttpClientContext.create());
    }

    public static HttpResult execute(String url, RequestMethod method, Map<String, Object> params, Charset charset, RequestConfig requestConfig, HttpContext context) {
        switch (method) {
            case GET:
                return get(url, params, charset, requestConfig, context);
            case DELETE:
                return delete(url, params, charset, requestConfig, context);
            case POST:
                return post(url, params, charset, requestConfig, context);
            case PUT:
                return put(url, params, charset, requestConfig, context);
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static HttpResult execute(String url, RequestMethod method) {
        return execute(url, method, new HashMap<String, Object>());
    }

    private static HttpEntity createJsonEntity(Map<String, Object> params) {
        ContentType contentType = ContentType.APPLICATION_JSON;
        String json = JsonUtil.toJson(params);
        byte[] data = json.getBytes(contentType.getCharset());
        return new ByteArrayEntity(data, contentType);
    }


    private static List<NameValuePair> genNameValuePairsFromMap(
            Map<String, Object> params) {
        List<NameValuePair> nvp = new ArrayList<NameValuePair>();
        if (params != null && params.size() > 0) {
            for (String key : params.keySet()) {
                nvp.add(new BasicNameValuePair(key, String.valueOf(params.get(key))));
            }
        }
        return nvp;
    }

    public static String generateUrl(String urlPrefix, Map<String, Object> params, Charset charset) {
        List<NameValuePair> nvp = genNameValuePairsFromMap(params);
        StringBuilder sb = new StringBuilder();
        sb.append(urlPrefix);
        if (CollectionUtils.isNotEmpty(nvp)) {
            if (urlPrefix.contains("?")) {
                if (!urlPrefix.endsWith("&")) {
                    sb.append("&");
                }
            } else {
                sb.append("?");
            }
            sb.append(URLEncodedUtils.format(nvp, charset));
        }
        return sb.toString();
    }

    public static String generateUrl(String urlPrefix, List<NameValuePair> nvp, String charset) {
        StringBuilder sb = new StringBuilder();
        sb.append(urlPrefix);
        if (CollectionUtils.isNotEmpty(nvp)) {
            if (urlPrefix.contains("?")) {
                if (!urlPrefix.endsWith("&")) {
                    sb.append("&");
                }
            } else {
                sb.append("?");
            }
            sb.append(URLEncodedUtils.format(nvp, Charset.forName(charset)));
        }
        return sb.toString();
    }

    private static CloseableHttpClient createHttpClient() {
        try {
            SSLContext sslContext = SSLContexts.custom().build();
            sslContext.init(null,
                    new TrustManager[]{new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }}, null);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("http", PlainConnectionSocketFactory.INSTANCE)
                    .register("https", new SSLConnectionSocketFactory(sslContext))
                    .build();

            connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // Create socket configuration
            SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);
            // Create connection configuration
            ConnectionConfig connectionConfig = ConnectionConfig.custom()
                    .setMalformedInputAction(CodingErrorAction.IGNORE)
                    .setUnmappableInputAction(CodingErrorAction.IGNORE)
                    .setCharset(defaultCharset)
                    .build();
            connManager.setDefaultConnectionConfig(connectionConfig);
            connManager.setMaxTotal(DEFAULT_MAX_CONNECTION_TOTAL);
            connManager.setDefaultMaxPerRoute(DEFAULT_MAX_CONNECTION_PER_ROUTE);

            return HttpClients.custom().setConnectionManager(connManager).build();

        } catch (Exception e) {
            throw new HttpClientException("Failed create http client", e);
        }
    }

    public static CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            synchronized (HttpClientUtils.class) {
                if (httpClient == null) {
                    httpClient = createHttpClient();
                }
            }
        }
        return httpClient;
    }

    public static enum RequestMethod {GET, DELETE, POST, PUT}

    ;

    public static class HttpResult {
        private int statusCode;
        private ContentType contentType;
        private byte[] content;

        public int getStatusCode() {
            return statusCode;
        }

        public ContentType getContentType() {
            return contentType;
        }

        public byte[] getContent() {
            return content;
        }

        public String getContentAsString(Charset forceCharset) {
            return new String(content, forceCharset);
        }

        public String getContentAsString() {
            Charset cs = Charset.defaultCharset();
            if (contentType != null && contentType.getCharset() != null) {
                cs = contentType.getCharset();
            }
            return getContentAsString(cs);
        }

        public Map<String, Object> getJsonContent() {
            // try to parse content as json
            return JsonUtil.fromJson(getContentAsString());
        }
    }

    public static class HttpClientException extends RuntimeException {
        private static final long serialVersionUID = 8826819984229126650L;

        public HttpClientException() {
            super();
        }

        public HttpClientException(String message, Throwable cause) {
            super(message, cause);
        }

        public HttpClientException(String message) {
            super(message);
        }

        public HttpClientException(Throwable cause) {
            super(cause);
        }
    }
}
