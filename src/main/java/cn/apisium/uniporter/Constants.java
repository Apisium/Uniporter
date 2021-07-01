package cn.apisium.uniporter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Constants that will be used multiple times.
 *
 * @author Baleine_2000
 */
public class Constants {
    public static final String DECODER_ID = "uniporter-decoder";
    public static final String GZIP_HANDLER_ID = "uniporter-http-gzip";

    public static final String DEFAULT_TAIL_ID = "DefaultChannelPipeline$TailContext#0";

    public static final String KEY_STORE_FORMAT = "JKS";
    public static final String KEY_MANAGER = "SunX509";
    public static final String SSL_PROTOCOL = "TLS";

    public static final Set<Character> HTTP_METHODS = new HashSet<>(Arrays.asList('G', 'H', 'P', 'D', 'C', 'O', 'T'));
}
