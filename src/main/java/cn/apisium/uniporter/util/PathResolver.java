package cn.apisium.uniporter.util;

import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Translated from https://github.com/pillarjs/resolve-path/blob/master/index.js#L45
 */
public class PathResolver {
    private static final Pattern UP_PATH_REGEXP = Pattern.compile("(?:^|[\\\\/])\\.\\.(?:[\\\\/]|$)");

    /**
     * Translated from https://github.com/pillarjs/resolve-path/blob/master/index.js#L45
     *
     * @param relativePath the non absolute path
     * @return a clean path that can be used safely
     * @throws IllegalHttpStateException if path is not "good"
     */
    public static String resolvePath(String relativePath) throws IllegalHttpStateException {
        String root = "/";
        String sep = "/";

        // containing NULL bytes is malicious
        if (relativePath.indexOf('\0') != -1) {
            throw new IllegalHttpStateException("Malicious Path", HttpResponseStatus.BAD_REQUEST);
        }

        // path should never be absolute
        if (Paths.get(relativePath).isAbsolute()) {
            throw new IllegalHttpStateException("Malicious Path", HttpResponseStatus.BAD_REQUEST);
        }

        // path outside root
        if (UP_PATH_REGEXP.matcher(Paths.get('.' + sep + relativePath).normalize().toString()).find()) {
            throw new IllegalHttpStateException("Forbidden", HttpResponseStatus.FORBIDDEN);
        }

        // join the relative path
        return Paths.get(root).resolve(relativePath).normalize().toString();
    }
}
