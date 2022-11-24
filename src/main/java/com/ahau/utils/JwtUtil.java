package com.ahau.utils;

import com.ahau.domain.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Cheryl 769303522@qq.com
 */

@Slf4j
public class JwtUtil {
    // 密钥
    private static final String SECRET = "bioInfo";
    // Token 过期时间，单位为秒
    private static final long EXPRIATION = 60 * 60 * 24 * 7; // 7 天有效

    // JWT 三部分的 Header
    private static Map<String, Object> HEADER = new HashMap<>();

    static {
        HEADER.put("alg", "HS256");
        HEADER.put("typ", "JWT");
    }

    // 生成 Token
    public static String generate(User usr) {
        Date expire = new Date(System.currentTimeMillis() + EXPRIATION * 1000);
        String token = JWT.create()
                .withHeader(HEADER)
                .withClaim("email", usr.getEmail())
                .withClaim("role", usr.getRole())
                .withExpiresAt(expire)
                .withIssuedAt(new Date()) // 签发时间
                .sign(Algorithm.HMAC256(SECRET));
        return token;
    }

    // 验证 Token
    public static Map<String, Claim> verify(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SECRET)).build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaims();
        } catch (Exception e) {
            log.error("token解码异常: " + e.getMessage());
            return null;
        }
    }
}
