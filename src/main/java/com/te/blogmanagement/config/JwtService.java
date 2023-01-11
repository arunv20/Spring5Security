package com.te.blogmanagement.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service //Auto transformed to managed bean
public class JwtService {


    private static final String SECRET_KEY = "2B4D6251655468576D5A7134743777217A24432646294A404E635266556A586E";

    public String generateToken(UserDetails userDetails){
        return  generateToken(new HashMap<>(),userDetails);
    }
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);

    }

    private<T> T extractClaim(String token, Function<Claims,T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(
            Map<String,Object> extraclaims,
            UserDetails userDetails
    ){
        return Jwts.builder()
                .setClaims(extraclaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis()+1000*60*24))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

    }
    public boolean isTokenValid(String token, UserDetails userDetails){
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())&& !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token,Claims :: getExpiration);
    }

    private Claims  extractAllClaims(String token){ // Claims is class from jsonwebtoken.claims dependency
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())   //use to create a signature digitally ensure the message was not changed
                .build()
                .parseClaimsJws(token)
                .getBody();

    }

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);

    }
}