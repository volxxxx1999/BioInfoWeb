package com.ahau.common;

public class Code {
    // 上传相关
    public final static Integer UPLOAD_OK = 20011;
    public final static Integer TRAIN_OK = 20021;
    public final static Integer SEND_OK = 20031;
    public final static Integer REMOVE_OK = 20041;
    // 搜索相关（对应不同模块的结果）
    public final static Integer ASSEMBLE_SEARCH_OK = 20051;
    public final static Integer GAPFILL_SEARCH_OK = 20052;
    public final static Integer TELO_SEARCH_OK = 20053;
    public final static Integer CENTRO_SEARCH_OK = 20054;

    // 发送相关
    public final static Integer UPLOAD_ERR = 20010;
    public final static Integer TRAIN_ERR = 20020;
    public final static Integer SEND_ERR = 20030;
    public final static  Integer REMOVE_ERR = 20040;
    public final static Integer SEARCH_ERR = 20050;

    // 查找服务器基因组文件
    public final static Integer FIND_OK = 20061;
    public final static Integer FIND_ERR = 20060;

    //异常相关
    public final static Integer SYSTEM_ERR = 50001;
    public final static Integer BUSINESS_ERR = 50002;
    public final static Integer UNKNOWN_ERR = 50003;

    // 登录相关
    public final static Integer LOGIN_SUCCESS = 10000;
    public final static Integer REGISTER_SUCCESS = 10001;
    public final static Integer VERIFY_SUCESS = 10002;
    public final static Integer VERIFY_FAIL = 10003;
    public final static Integer USR_NOT_EXISTS = 10011;
    public final static Integer USR_ALREADY_EXISTS = 10012;
    public final static Integer WRONG_PASSWORD = 10013;
    public final static Integer UNKONWN_ERROR = 19999;

}
