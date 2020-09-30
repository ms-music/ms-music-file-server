package com.ms.music.file.server.config;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;import javax.servlet.FilterChain;import javax.servlet.ServletException;import javax.servlet.http.HttpServletRequest;import javax.servlet.http.HttpServletResponse;import java.io.IOException;@Componentpublic class RequestFilter extends OncePerRequestFilter {    @Override    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {        System.out.println("过滤请求");        System.out.println(request.getHeader("origin"));        System.out.println(request.getHeader("Origin"));        response.setHeader("Access-Control-Allow-Origin", request.getHeader("origin"));        response.addHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");        response.setHeader("Access-Control-Allow-Headers", "*");        chain.doFilter(request, response);    }}