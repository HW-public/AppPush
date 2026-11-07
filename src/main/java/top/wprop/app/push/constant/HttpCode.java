package top.wprop.app.push.constant;

import java.util.HashMap;
import java.util.Map;

public class HttpCode {
    public static final Integer OK = 0;
    /**
     * 调用成功
     */
    public static final Integer InvalidParameter = 1;
    /**
     * 参数错误
     */
    public static final Integer InternalServerError = 2;
    /**
     * 服务器内部错误
     */

    public static final Integer UserExist = 100;
    /**
     * 用户已存在
     */
    public static final Integer UserNotFound = 101;
    /**
     * 用户不存在
     */
    public static final Integer UsernameExist = 102;
    /**
     * 用户名已存在
     */
    public static final Integer UserDeactivated = 103;
    /**
     * 用户已停用
     */
    public static final Integer GroupNameExist = 200;
    /**
     * 群组名称已存在
     */
    public static final Integer GroupNotFound = 201;
    /**
     * 群组不存在
     */
    public static final Integer UserHasNoGroup = 202;
    /**
     * 用户不属于任何Group
     */
    public static final Integer GroupHasSubgroups = 203;
    /**
     * 群组拥有子群组，不允许删除
     */
    public static final Integer GroupHasUsers = 204;
    /**
     * 用户拥有子群组，不允许删除
     */

    public static final Integer TenantNameExist = 300;
    /**
     * 租户名已存在
     */
    public static final Integer TenantNotFound = 301;
    /**
     * 租户不存在
     */
    public static final Integer TenantExpired = 302;
    /**
     * 租户已过期
     */
    public static final Integer TenantDeactivated = 303;
    /**
     * 租户已停用
     */
    public static final Integer TenantPackageNameExist = 304;
    /**
     * 租户套餐名称已存在
     */
    public static final Integer TenantPackageDeactivated = 305;
    /**
     * 租户套餐已停用
     */

    public static final Integer PhoneExist = 400;
    /**
     * 手机号已存在
     */
    public static final Integer PhoneNotFound = 401;
    /**
     * 手机号不存在
     */
    public static final Integer EmailExist = 402;
    /**
     * 用户Email已存在
     */
    public static final Integer ProgramExecuting = 403;
    /**
     * 程序正在执行,请勿重复请求
     */

    public static final Integer WrongPassword = 500;
    /**
     * 用户名或密码错误
     */
    public static final Integer WrongSmsCode = 501;
    /**
     * 短信验证码错误
     */
    public static final Integer WrongCaptchaCode = 502;
    /**
     * 图形验证码错误
     */
    public static final Integer InvalidToken = 503;
    /**
     * 无效的Token
     */
    public static final Integer InvalidRefreshToken = 504;
    /**
     * 无效的RefreshToken
     */
    public static final Integer InvalidClientId = 505;
    /**
     * 无效的client_id
     */
    public static final Integer InvalidAuthorizationCode = 506;
    /**
     * 无效的授权码
     */
    public static final Integer NoPermission = 507;
    /**
     * 用户没有权限
     */
    public static final Integer Unauthorized = 508;
    /**
     * Token无效或者用户没有权限
     */
    public static final Integer LoginExpired = 509;
    /**
     * 登录已过期
     */

    public static final Integer RoleNameExist = 600;
    /**
     * 角色名称已存在
     */
    public static final Integer RoleKeyExist = 601;
    /**
     * 角色字符已存在
     */
    public static final Integer PermissionKeyExist = 602;
    /**
     * 权限字符已存在
     */
    public static final Integer PermissionDeactivated = 603;
    /**
     * 权限已停用
     */
    public static final Integer ResourceNameExist = 604;
    /**
     * 资源名称已存在
     */
    public static final Integer PostNameExist = 605;
    /**
     * 岗位名称已存在
     */
    public static final Integer PostKeyExist = 606;
    /**
     * 岗位编码已存在
     */
    public static final Integer DictTypeExist = 607;
    /**
     * 字典类型已存在
     */
    public static final Integer ClientKeyExist = 608;
    /**
     * 客户端编码已存在
     */
    public static final Integer ConfigKeyExist = 609;
    /**
     * 参数键值已存在
     */
    public static final Integer InvalidResourceUrl = 610;
    /**
     * 无效的资源地址
     */

    private static Map<Integer, String> maps = new HashMap<>();

    static {
        maps.put(OK, "OK");
        maps.put(InvalidParameter, "InvalidParameter");
        maps.put(InternalServerError, "InternalServerError");
        maps.put(UserExist, "UserExist");
        maps.put(UserNotFound, "UserNotFound");
        maps.put(UsernameExist, "UsernameExist");
        maps.put(UserDeactivated, "UserDeactivated");
        maps.put(GroupNameExist, "GroupNameExist");
        maps.put(GroupNotFound, "GroupNotFound");
        maps.put(GroupHasSubgroups, "GroupHasChildGroup");
        maps.put(GroupHasUsers, "GroupHasChildGroup");
        maps.put(UserHasNoGroup, "UserHasNoGroup");
        maps.put(TenantNameExist, "TenantNameExist");
        maps.put(TenantNotFound, "TenantNotFound");
        maps.put(TenantExpired, "TenantExpired");
        maps.put(TenantDeactivated, "TenantDeactivated");
        maps.put(TenantPackageNameExist, "TenantPackageNameExist");
        maps.put(TenantPackageDeactivated, "TenantPackageDeactivated");
        maps.put(PhoneExist, "PhoneExist");
        maps.put(PhoneNotFound, "PhoneNotFound");
        maps.put(EmailExist, "PhoneNotFound");
        maps.put(ProgramExecuting, "ProgramExecuting");
        maps.put(WrongPassword, "WrongPassword");
        maps.put(WrongSmsCode, "WrongSmsCode");
        maps.put(WrongCaptchaCode, "WrongCaptchaCode");
        maps.put(InvalidToken, "InvalidToken");
        maps.put(InvalidRefreshToken, "InvalidRefreshToken");
        maps.put(InvalidClientId, "InvalidClientId");
        maps.put(InvalidAuthorizationCode, "InvalidAuthorizationCode");
        maps.put(NoPermission, "NoPermission");
        maps.put(Unauthorized, "Unauthorized");
        maps.put(LoginExpired, "LoginExpired");
        maps.put(RoleNameExist, "RoleNameExist");
        maps.put(RoleKeyExist, "RoleKeyExist");
        maps.put(PermissionKeyExist, "PermissionKeyExist");
        maps.put(PermissionDeactivated, "PermissionDeactivated");
        maps.put(ResourceNameExist, "ResourceNameExist");
        maps.put(PostNameExist, "PostNameExist");
        maps.put(PostKeyExist, "PostKeyExist");
        maps.put(DictTypeExist, "DictTypeExist");
        maps.put(ConfigKeyExist, "ConfigKeyExist");
        maps.put(ClientKeyExist, "ClientKeyExist");
        maps.put(InvalidResourceUrl, "InvalidResourceUrl");
    }

    public static String getName(Integer code) {
        if (!maps.containsKey(code)) {
            return null;
        }
        String result = maps.get(code);
        return result;
    }
}
