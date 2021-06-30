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

public class SslFactory {

    private static final HashMap<String, SSLContext> contexts = new HashMap<>();

    public static SSLEngine createEngine() {
        return createEngine(Uniporter.getRouteConfig().getKeyStore().getAbsolutePath());
    }

    public static SSLEngine createEngine(String keyPath) {
        SSLEngine engine = getContext(keyPath).createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(false);
        return engine;
    }

    public static SSLContext getContext(String keyPath) {
        return contexts.computeIfAbsent(keyPath, (key) -> {
            SSLContext context;
            if (keyPath == null) {
                throw new IllegalArgumentException("Key path should not be null");
            }

            String password = Uniporter.getRouteConfig().getSslKeyStorePassword();
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
