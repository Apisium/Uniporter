package cn.apisium.uniporter.util;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.Uniporter;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.HashMap;

/**
 * Create SSL related instances.
 *
 * @author Baleine_2000
 */
public class SSLFactory {
    private static final HashMap<String, SSLContext> contexts = new HashMap<>();

    /**
     * Create server side {@link SSLEngine} instance for Uniporter keystore.
     *
     * @return a new {@link SSLEngine}
     */
    public static SSLEngine createEngine() {
        return createEngine(
                Uniporter.getRouteConfig().getKeyStore().getAbsolutePath(),
                Uniporter.getRouteConfig().getSslKeyStorePassword());
    }

    /**
     * Create server side {@link SSLEngine} instance for the given keystore.
     * <p>
     * Keystore should be JKS format.
     *
     * @param keyPath  the keystore path
     * @param password password of the keystore
     * @return a new {@link SSLEngine}
     */
    public static SSLEngine createEngine(String keyPath, String password) {
        SSLEngine engine = getContext(keyPath, password).createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(false);
        return engine;
    }

    /**
     * Get the {@link SSLContext} for the given keystore.
     * <p>
     * Keystore should be JKS format.
     *
     * @param keyPath  the keystore path
     * @param password password of the keystore
     * @return a new or cached {@link SSLContext}
     */
    public static SSLContext getContext(String keyPath, String password) {
        return contexts.computeIfAbsent(keyPath, (key) -> {
            SSLContext context;
            if (keyPath == null) {
                throw new IllegalArgumentException("Key path should not be null");
            }

            // Load the keystore
            try (InputStream keyInput = new FileInputStream(keyPath)) {
                KeyStore store = KeyStore.getInstance(Constants.KEY_STORE_FORMAT);
                store.load(keyInput, password.toCharArray());

                KeyManagerFactory x509 = KeyManagerFactory.getInstance(Constants.KEY_MANAGER);
                x509.init(store, password.toCharArray());

                context = SSLContext.getInstance(Constants.SSL_PROTOCOL);
                context.init(x509.getKeyManagers(), null, null);
            } catch (Exception e) {
                throw new IllegalStateException("Unable to initialize ssl context", e);
            }

            return context;
        });
    }
}
