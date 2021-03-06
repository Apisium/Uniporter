package cn.apisium.uniporter.util;

import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

/**
 * Translated from https://github.com/pillarjs/resolve-path/blob/master/index.js#L45
 */
public class PathResolver {
    private static final Path ROOT = Paths.get("/");
    private static final Pattern UP_PATH_REGEXP = Pattern.compile("(?:^|[\\\\/])\\.\\.(?:[\\\\/]|$)");

    /**
     * Translated from https://github.com/pillarjs/resolve-path/blob/master/index.js#L45
     *
     * @param relativePath the non absolute path
     * @return a clean path that can be used safely
     * @throws IllegalHttpStateException if path is not "good"
     */
    public static String resolvePath(String relativePath) throws IllegalHttpStateException {

        // containing NULL bytes is malicious
        if (relativePath.indexOf('\0') != -1) {
            throw new IllegalHttpStateException("Malicious Path", HttpResponseStatus.BAD_REQUEST);
        }

        try {
            // path outside root
            if (UP_PATH_REGEXP.matcher(Paths.get("./" + relativePath).normalize().toString()).find()) {
                throw new IllegalHttpStateException("Forbidden", HttpResponseStatus.FORBIDDEN);
            }
        } catch (InvalidPathException ignored) {
            throw new IllegalHttpStateException("Malicious Path", HttpResponseStatus.BAD_REQUEST);
        }

        // join the relative path
        return ROOT.resolve(relativePath).normalize().toString();
    }
}
