package com.banklab.common.redis;

public class RedisKeyUtil {
    public static String sms(String phone) {
        return "sms:" + phone;
    }

    public static String email(String email) {
        return "email:" + email;
    }

    public static String transaction(Long memberId, String account){return "trhis-sync-status:"+memberId+":"+account;}

    public static String resend(String type) {
        return "resend:" + type;
    }

    public static String verified(String type) {
        return "verified:" + type;
    }

    public static String failCount(String phone) {
        return "fail:" + phone;
    }
}
