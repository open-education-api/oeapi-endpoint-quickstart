package oeapi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Set response header with application version for debugging reported
 * issues.  The git version from `git.properties` file as supplied by
 * `git-commit-id-maven-plugin` and requires `gitDescribe.tags` set to
 * `true` to populate `git.commit.id.describe` properly.
 */
@Component
public class VersionHeaderFilter extends OncePerRequestFilter {
    public static final String header = "X-Oeapi-Quickstart-Version";
    public static final String version = loadVersion();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        response.setHeader(header, version);
        filterChain.doFilter(request, response);
    }

    private static final String propertiesResource = "git.properties";
    private static final String propertyName = "git.commit.id.describe";

    private static String loadVersion() {
        String v;
        ClassLoader cl = VersionHeaderFilter.class.getClassLoader();
        try (InputStream in = cl.getResourceAsStream(propertiesResource)) {
            Properties p = new Properties();
            p.load(in);
            v = p.getProperty(propertyName);
        } catch (IOException e) {
            v = "unknown";
        }
        return v;
    }
}
