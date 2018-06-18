package com.leotesla.httpclient;

/**
 * 客户端错误
 *
 * @version 1.0
 *
 * Created by LeoTesla on 2017/10/7.
 */

public class ClientError {

    /**
     * 本地客户端异常
     */
    public final static int EXCEPTION_CLIENT    = 0;

    /**
     * 服务端异常
     */
    public final static int EXCEPTION_SERVER    = 1;


    /**
     * 本地网络连接异常，无可用网络连接
     */
    public final static int NET_EXCEPTION       = 1000;

    /**
     * 服务异常，非法授权（比如用户禁用）
     */
    public final static int SERVICE_EXCEPTION   = 2000;

    /**
     * 通用异常，一般由客户端代码出错引发，具体原因查看 msg 堆栈信息
     */
    public final static int IO_EXCEPTION        = 3000;

    /**
     * 超时异常，连接或响应超时
     */
    public final static int TIMEOUT_EXCEPTION   = 4000;

    /**
     * SSH 握手异常，一般由客户端证书过期引发
     */
    public final static int SSH_EXCEPTION       = 5000;

    /**
     * 返回数据解析异常，通常由于使用了不匹配的解析器或者服务端未返回预期结果引发
     */
    public final static int DATA_EXCEPTION      = 6000;

    /**
     * URL异常，通用请求异常，非法格式
     * @see java.net.MalformedURLException
     */
    public final static int URL_EXCEPTION       = 7000;

    /**
     * 请求返回码非20x系列,后面加具体的错误码
     */
    public final static int CODE_EXCEPTION      = 8000;

    // 原始异常
    private Exception exception;
    // 错误类型
    private int type;
    // 错误码
    private int code;
    // 错误信息（代码原始错误）
    private String msg;
    // 用户端错误信息
    private String prettyMsg;
    // 是否已被流程处理，一个标记
    private boolean handled;

    public ClientError(int type, int code, String msg) {
        this(null, type, code, msg);
    }

    public ClientError(Exception e, int type, int code, String msg) {
        this.exception = e;
        this.type = type;
        this.code = code;
        this.msg = msg;
    }

    public Exception getException() {
        return exception;
    }

    public int getCode() {
        return code;
    }

    public int getType() {
        return type;
    }

    public String getMsg() {
        return msg;
    }

    public void setPrettyMsg(String prettyMsg) {
        this.prettyMsg = prettyMsg;
    }

    public boolean isHandled() {
        return handled;
    }

    public void closed() {
        this.handled = true;
    }

    /**
     * 获取显示错误信息
     */
    public String getPrettyMsg() {
        return this.prettyMsg + "(" + (0 == type ? "C-" : "S-") + code + ")";
    }

    /**
     * 网络问题
     */
    public boolean isNetwork() {
        return NET_EXCEPTION == this.type;
    }

    /**
     * 数据结构问题
     */
    public boolean isData() {
        return DATA_EXCEPTION == this.type;
    }

    /**
     * 返回状态码异常
     */
    public boolean isInvalidCode() {
        return 8 == this.type / 1000;
    }

    /**
     * SSH 连接问题
     */
    public boolean isSSH() {
        return SSH_EXCEPTION == this.type;
    }

    public void abort() {
        this.type = NET_EXCEPTION;
    }

    @Override
    public String toString() {
        return "ClientError{" +
                "type=" + type +
                ", code=" + code +
                ", msg='" + msg + '\'' +
                ", prettyMsg='" + prettyMsg + '\'' +
                '}';
    }

}
