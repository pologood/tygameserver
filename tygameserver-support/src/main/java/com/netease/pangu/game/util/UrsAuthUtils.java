package com.netease.pangu.game.util;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

/**
 * Created by huangc on 2017/2/5.
 */
public class UrsAuthUtils {

    private final static Logger logger = Logger.getLogger(UrsAuthUtils.class);
    private final static String SALT_SEGMENT = "yc2EAAAABIwAAAQEApy3VNB&*()mBeJ11QHqZ2bTr~f@/0d#5$%^s8J69phCSEntqfvVZMBYWD5dqP8gyP+gyT8u/aqQzErgsOJKowQqpvIV//0WP1Cz8vclNUKqypi0UPQb8e+zAJ/7MsjZhHgb7vbnHt/lZ,.p&*((3)45YPHvgc0PZ4Kbm4WxHFkNe/cDM";
    private final static long CREDIDENTIAL_LIFE = DateUtils.MILLIS_PER_MINUTE * 5;// 凭据有效期为20分钟
    private final static String CREDIDENTIAL_COOKIE_NAME = "hd_ty_cred";
    private final static String P_INFO = "P_INFO";
    private final static String USERNAME_ATTR_KEY = "USERNAME_ATTR_KEY";
    private final static String QUERY_BIND_PRODUCT_KEY;
    public static final String COOKIENAME_URS = "NTES_SESS";
    // Cookie过期时间
    public static final long URS_COOKIE_LIFE = DateUtils.MILLIS_PER_MINUTE * 40 / 1000;
    public static final long URS_PERSIST_COOKIE_LIFE = DateUtils.MILLIS_PER_DAY * 60 / 1000;


    static {
        Properties properties = new Properties();
        try {
            properties.load(UrsAuthUtils.class.getResourceAsStream("/common.properties"));
        } catch (IOException e) {
            logger.error("Failed loading urs.queryBind.productKey from common.properties", e);
        }
        QUERY_BIND_PRODUCT_KEY = properties.getProperty("urs.queryBind.productKey", "");

        if (StringUtils.isBlank(QUERY_BIND_PRODUCT_KEY)) {
            logger.warn("urs.queryBind.productKey not configured in common.properties. queryAccountAliasBind is disabled.");
        }
    }

    private final static class Credential {
        private String userName;
        private long expiresAt;
    }

    /**
     * 设置凭据
     *
     * @param request
     * @param response
     * @param credential
     */
    private static void setCredential(FullHttpRequest request, FullHttpResponse response, Credential credential) {
        Cookie cookie = new DefaultCookie(CREDIDENTIAL_COOKIE_NAME, credential.expiresAt + "|" + encodeCredential(credential.userName, credential.expiresAt, NettyHttpUtil.getCookieValue(request, COOKIENAME_URS, "")));
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        cookie.setHttpOnly(true);
        cookie.setHttpOnly(true);
        NettyHttpUtil.setCookie(response, cookie);

    }

    /**
     * 检查合法的凭据是否存在，如果存在的话，则更新其安全凭据
     *
     * 如果存在，会在request中设置SEC_USERNAME_ATTR_KEY属性为登录的用户名
     *
     * @return
     */
    public static boolean checkAndUpdateCredential(FullHttpRequest request, FullHttpResponse response) {
        Credential credential = verifyCredential(request, response);
        if (credential != null) {
            request.headers().add(USERNAME_ATTR_KEY, credential.userName);
            return true;
        } else {
            request.headers().add(USERNAME_ATTR_KEY, null);
            return false;
        }
    }

    /**
     * 获取登录的用户名
     *
     * @param request
     * @return
     */
    public static String getLoginedUserName(FullHttpRequest request, FullHttpResponse response) {
        if(checkAndUpdateCredential(request, response)) {
            String userName = request.headers().get(USERNAME_ATTR_KEY);
            return userName;
        }
        return null;
    }

    public static String getLoginedUserName(FullHttpRequest request) {
        return request.headers().get(USERNAME_ATTR_KEY);
    }

    private static String encodeCredential(String userName, long expiresAt, String ntessess) {
        String actualMd5 = DigestUtils.md5Hex(userName + expiresAt + SALT_SEGMENT + ntessess);
        return actualMd5;
    }

    private static Credential verifyCredential(FullHttpRequest request, FullHttpResponse response) {
        // 这里必须使用这种原始的方式来获取P_INFO Cookie
        // 因为Tomcat 5.5.17以上的版本会丢弃cookie值中'@'字符及其以后的部分
        // 导致外域邮箱的URS用户名无法正确解析
        String tyHdInfo = null;
        String pInfo = null;
        Set<Cookie> cookies = NettyHttpUtil.getCookies(request);
        for (Cookie cookie : cookies) {
            if(cookie.name().equals(P_INFO)) {
                pInfo = cookie.value();
            } else if(cookie.name().equals(CREDIDENTIAL_COOKIE_NAME)) {
                tyHdInfo = cookie.value();
            }
        }

        String ursName = getUrsNameFromPInfo(pInfo);
        String ntessess = NettyHttpUtil.getCookieValue(request, COOKIENAME_URS, "");

        if(StringUtils.isNotBlank(tyHdInfo)) {
            String[] values = tyHdInfo.split("\\|");
            if(values.length == 2) {
                long expireAtTime = NumberUtils.toLong(values[0], 0);
                String hashedMd5 = values[1];
                if(System.currentTimeMillis() < expireAtTime) {
                    String actualMd5 = encodeCredential(ursName, expireAtTime, ntessess);
                    if (StringUtils.equals(hashedMd5, actualMd5)) {
                        Credential credential = new Credential();
                        credential.expiresAt = expireAtTime;
                        credential.userName = ursName;
                        return credential;
                    }
                }
            }
        }

        if (StringUtils.isNotEmpty(ursName)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("time", String.valueOf(URS_COOKIE_LIFE));
            params.put("cookie", ntessess);
            HttpClientUtils.HttpResult result = HttpClientUtils.get("https://reg.163.com/validate_cookie.do", params);
            String[] values = result.getContentAsString().split("\n");
            int ret = -1;
            if (values != null) {
                ret = NumberUtils.toInt(values[0], 0);
            }
            // 判断验证后的urs是不是跟P_INFO中一样
            if (NumberUtils.toInt(values[0], 0) == 1 && values.length == 2) {
                if (!StringUtils.equals(getCompleteURSUserName(values[1]), ursName)) {
                    ret = -1;
                }
            }
            if(ret >= 0) {
                //验证成功
                Credential credential = new Credential();
                credential.expiresAt = System.currentTimeMillis() + CREDIDENTIAL_LIFE;
                credential.userName = ursName;
                //由于没有登录的controller，所以在此第一次设置cookie
                setCredential(request, response, credential);
                return credential;
            }
        }
        return null;
    }

    private static String getCompleteURSUserName(String userName) {
        if (userName.contains("@")) {
            return userName;
        } else {
            return userName + "@163.com";
        }
    }


    /**
     * 获取当前登录用户的所有URS账户别名，第一个为主账户
     * @param request
     * @return
     */
    public static List<String> getAccountAliasBind(FullHttpRequest request) {
        String userName =  request.headers().get(USERNAME_ATTR_KEY);
        return getAccountAliasBind(userName);
    }

    public static List<String> getAccountAliasBind(String userName) {
        if (StringUtils.isNotBlank(userName)) {
            if (StringUtils.isNotBlank(QUERY_BIND_PRODUCT_KEY)) {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put("product", QUERY_BIND_PRODUCT_KEY);
                params.put("username", userName);
                HttpClientUtils.HttpResult result = HttpClientUtils.get("https://reg.163.com/services/queryAccountAliasBind", params);
                String[] lines = StringUtils.split(result.getContentAsString(), '\n');
                if (StringUtils.equals(lines[0], "201") && lines.length >= 2) {
                    String[] accounts = StringUtils.split(lines[2], '|');
                    return new ArrayList<String>(Arrays.asList(accounts));
                } else if (StringUtils.equals(lines[0], "200")) {
                    return new ArrayList<String>(Arrays.asList(userName));
                } else {
                    logger.warn("Failed parsing queryAccountAliasBind result: " + result.getContentAsString());
                    return new ArrayList<String>(Arrays.asList(userName));
                }
            } else {
                return new ArrayList<String>(Arrays.asList(userName));
            }
        } else {
            return new ArrayList<String>(Arrays.asList(new String[] {null}));
        }
    }

    private static String getUrsNameFromPInfo(String pInfo) {
        if (StringUtils.isNotEmpty(pInfo)) {
            String[] values = pInfo.split("\\|");
            return values[0];
        }
        return null;
    }
}
