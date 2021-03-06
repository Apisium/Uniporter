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
    public static final String UNIPORTER_ID = "uniporter-global";
    public static final String DECODER_ID = "uniporter-decoder";
    public static final String GZIP_HANDLER_ID = "uniporter-http-gzip";
    public static final String SERVER_HANDLER_ID = "uniporter-http-server-handler";
    public static final String ROUTED_RESPONSE_HANDLER_ID = "uniporter-http-response";
    public static final String ROUTED_REQUEST_HANDLER_ID = "uniporter-http-request";
    public static final String AGGREGATOR_HANDLER_ID = "uniporter-http-aggregator";
    public static final String DECODER_HANDLER_ID = "uniporter-http-decoder";
    public static final String ENCODER_HANDLER_ID = "uniporter-http-encoder";
    public static final String PRE_ROUTE_ID = "uniporter-pre-router";

    public static final String DEFAULT_TAIL_ID = "DefaultChannelPipeline$TailContext#0";

    public static final String KEY_STORE_FORMAT = "JKS";
    public static final String KEY_MANAGER = "SunX509";
    public static final String SSL_PROTOCOL = "TLS";

    public static final Set<Character> HTTP_METHODS = new HashSet<>(Arrays.asList('G', 'H', 'P', 'D', 'C', 'O', 'T'));
}
