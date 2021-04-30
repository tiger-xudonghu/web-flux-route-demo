package com.example.webfluxdemo.core;

/**
 * @author SZ
 * @Description:
 * @date 2021/4/29 14:43
 */
public interface Constants {
    String APP_PARAM = "appParam";
    String CONTEXT = "context";
    String LOCATION = "location";
    String META_DATA = "metaData";
    String CLIENT_RESPONSE_ATTR = "webHandlerClientResponse";
    String DUBBO_RPC_RESULT = "dubbo_rpc_result";
    String DUBBO_RPC_RESULT_EMPTY = "dubbo has not return value!";
    String CLIENT_RESPONSE_RESULT_TYPE = "webHandlerClientResponseResultType";
    String CLIENT_RESPONSE_CONN_ATTR = "nettyClientResponseConnection";
    String HTTP_TIME_OUT = "httpTimeOut";
    String ORIGINAL_RESPONSE_CONTENT_TYPE_ATTR = "original_response_content_type";
    String HTTP_URL = "httpUrl";
    String DUBBO_PARAMS = "dubbo_params";
    String DECODE = "UTF-8";
    String MODULE = "module";
    String METHOD = "method";
    String APP_KEY = "appKey";
    String GREEN_CHANNEL = "greenChannel";
    String OPEN_GREEN_CHANNEL = "openGreenChannel";
    String REQUEST_ID = "requestId";
    String EXT_INFO = "extInfo";
    String PATH_VARIABLE = "pathVariable";
    String HTTP_METHOD = "httpMethod";
    String RPC_TYPE = "rpcType";
    String SIGN = "sign";
    String PATH = "path";
    String VERSION = "version";
    String SIGN_PARAMS_ERROR = "Incomplete authentication parameters!";
    String SIGN_APP_KEY_IS_NOT_EXIST = "Authentication signature APP KEY does not exist.";
    String SIGN_PATH_NOT_EXIST = "The authentication key is not configured and the path is not matched.";
    String SIGN_VALUE_IS_ERROR = "Wrong signature value!";
    String TIMESTAMP = "timestamp";
    String REJECT_MSG = " You are forbidden to visit";
    String REWRITE_URI = "rewrite_uri";
    String HTTP_ERROR_RESULT = "this is bad request or fuse ing please try again later";
    String DUBBO_ERROR_RESULT = "dubbo rpc have error or fuse ing please check your param and  try again later";
    String SPRING_CLOUD_ERROR_RESULT = "spring cloud rpc have error or fuse ing please check your param and  try again later";
    String TIMEOUT_RESULT = "this request is time out  Please try again later";
    String UPSTREAM_NOT_FIND = "this can not rule upstream please check you configuration!";
    String TOO_MANY_REQUESTS = "the request is too fast please try again later";
    String SIGN_IS_NOT_PASS = "sign is not pass,Please check you sign algorithm!";
    String LINE_SEPARATOR = System.getProperty("line.separator");
    int MAX_CONCURRENT_REQUESTS = 100;
    int ERROR_THRESHOLD_PERCENTAGE = 50;
    int REQUEST_VOLUME_THRESHOLD = 20;
    int SLEEP_WINDOW_INMILLISECONDS = 5000;
    long TIME_OUT = 5000L;
    String COLONS = ":";
    String REQUEST_LATENCY = "REQUEST_LATENCY";

    default void findConstants() {
    }
}
