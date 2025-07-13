package ua.vhlab.tnfvvc.logging;

import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest http = (HttpServletRequest) request;

        // System.out.println("Remote IP: " + http.getRemoteAddr());
        // System.out.println("X-Forwarded-For: " + http.getHeader("X-Forwarded-For"));
        // System.out.println("X-Real-IP: " + http.getHeader("X-Real-IP"));
        // System.out.println("Referer: " + http.getHeader("Referer"));
        // System.out.println("Origin: " + http.getHeader("Origin"));
        // System.out.println("User-Agent: " + http.getHeader("User-Agent"));
        // Вивід усіх заголовків
        Enumeration<String> names = http.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            //System.out.println("Header: " + name + " = " + http.getHeader(name));
        }

        // Вивід усіх cookies
        if (http.getCookies() != null) {
            for (Cookie cookie : http.getCookies()) {
                //  System.out.println("Cookie: " + cookie.getName() + " = " + cookie.getValue());
            }
        } else {
            // System.out.println("No cookies");
        }

        chain.doFilter(request, response);
    }
}
