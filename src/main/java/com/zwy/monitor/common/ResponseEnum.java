package com.zwy.monitor.common;

/**
 * @author zwy
 * @date 2022年04月11日 15:13
 */
public enum ResponseEnum {
    /**
     * login
     */
    NO_LOGIN("10403", "未登录或登录过期"),
    USER_NOT_EXISTS("100001", "用户名密码错误"),
    LOGIN_SUCCESS("100002", "登录成功"),
    UPLOAD_SUCCESS("100003", "上传成功"),
    CHECK_UPLOAD_SUCCESS("100004", "检查上传成功"),
    CREATE_DIRECTORY_SUCCESS("100005", "文件创建成功"),
    SELECT_DIRECTORY_SUCCESS("100006", "文件查询成功"),
    SAME_NAME_DIRECTORY("100007", "文件夹已经创建"),
    WAIT_MARGE_FILE("100009", "等待分片合并"),
    DELETE_SUCCESS("100010", "文件删除成功"),
    UPDATE_FAVORITE_SUCCESS("100011", "修改收藏成功"),
    FIND_FAVORITE_SUCCESS("100012", "查询收藏成功"),
    RENAME_FILE_SUCCESS("100013","重命名成功"),
    RENAME_FILE_FAIL("100014","当前目录有该文件名"),
    NOT_FIND_ID("100015","未找到文件id"),

    HAS_UPLOAD("100016","已经上传")
    ;
    private final String code;
    private final String msg;

    ResponseEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    /**
     * 添加结果
     *
     * @param result result
     * @author zwy
     * @date 2022/4/11 0011 15:26
     */
    public void toResult(RestResult result) {
        result.setCode(this.getCode());
        result.setMsg(this.getMsg());
    }

    /**
     * 添加结果
     *
     * @param result result
     * @param data   data
     * @author zwy
     * @date 2022/4/11 0011 15:26
     */
    public void toResult(RestResult result, Object data) {
        result.setCode(this.getCode());
        result.setMsg(this.getMsg());
        result.setData(data);
    }
}
