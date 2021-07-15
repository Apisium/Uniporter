package cn.apisium.uniporter.acme;

import cn.apisium.uniporter.Constants;
import cn.apisium.uniporter.Uniporter;
import cn.apisium.uniporter.router.exception.IllegalHttpStateException;
import cn.apisium.uniporter.server.SimpleServer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.shredzone.acme4j.*;
import org.shredzone.acme4j.challenge.Http01Challenge;
import org.shredzone.acme4j.exception.AcmeException;
import org.shredzone.acme4j.util.CSRBuilder;
import org.shredzone.acme4j.util.KeyPairUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

public class Authorizer extends SimpleChannelInboundHandler<FullHttpRequest> {
    public static SimpleServer server = null;
    final Logger logger;
    final Session session;
    final File database;
    final KeyPair keyPair;
    final Uniporter plugin;
    final CSRBuilder csrb = new CSRBuilder();

    boolean authorized;

    public static KeyPair createOrLoadKey(File database, String keyPath) throws IOException {
        KeyPair keyPair;
        File key = new File(database, keyPath + ".pem");
        if (key.exists()) {
            try (FileReader fr = new FileReader(key)) {
                keyPair = KeyPairUtils.readKeyPair(fr);
            }
        } else {
            keyPair = KeyPairUtils.createKeyPair(2048);
            try (FileWriter fw = new FileWriter(key)) {
                KeyPairUtils.writeKeyPair(keyPair, fw);
            }
        }
        return keyPair;
    }

    public Authorizer(Uniporter plugin) throws IOException {
        this.plugin = plugin;
        logger = plugin.getLogger();
        logger.info("Start order certificate");
        authorized = plugin.getConfig().getBoolean("authorized", false);
        session = new Session(
                (Uniporter.getRouteConfig().isDebug() ?
                        "https://acme-staging-v02.api.letsencrypt.org/directory" :
                        "https://acme-v02.api.letsencrypt.org/directory"));
        database = new File(plugin.getDataFolder(), "keys");
        if (database.exists()) {
            String[] entries = database.list();
            for (String s : entries) {
                File currentFile = new File(database.getPath(), s);
                currentFile.delete();
            }
        }
        if (!database.exists() && !database.mkdirs()) {
            throw new IOException("Failed to create " + database.getPath());
        }


        logger.info("Reading Keypair");
        keyPair = createOrLoadKey(database, "keypair.pem");
        logger.info("Creating HTTP challenge server");
        if (server != null) {
            server.getFuture().channel().close();
            server.getFuture().channel().closeFuture().syncUninterruptibly();
        }
        server = new SimpleServer(80, (ch) -> {
            ChannelPipeline pipeline = ch.pipeline();

            // Set up normal http server
            pipeline.addLast(new HttpRequestDecoder());
            pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
            pipeline.addLast(new HttpResponseEncoder());
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                @Override
                protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
                    Authorizer.this.channelRead0(ctx, msg);
                }
            });
        });
        new Thread(() -> {
            try {
                server.start();
                logger.info("HTTP challenge server created");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void order() throws AcmeException, InterruptedException, IOException, KeyStoreException,
            CertificateException, NoSuchAlgorithmException {
        logger.info("Ordering certificate");
        Account account = new AccountBuilder()
                .addContact("mailto:" + plugin.getConfig().getString("email", "temp@example.com"))
                .agreeToTermsOfService()
                .useKeyPair(keyPair)
                .create(session);
        Order order = account.newOrder()
                .domains(plugin.getConfig().getStringList("domains").toArray(new String[0]))
                .create();
        for (Authorization auth : order.getAuthorizations()) {
            logger.info("Processing " + auth.getIdentifier().getDomain());
            if (auth.getStatus() == Status.PENDING) {
                process(auth);
                csrb.addDomain(auth.getIdentifier().getDomain());
                logger.info("Processed " + auth.getIdentifier().getDomain());
            }
        }
        csrb.setOrganization(plugin.getConfig().getString("organization", "Uniporter User"));
        KeyPair privateKey = createOrLoadKey(database, "private.pem");
        csrb.sign(privateKey);
        byte[] csr = csrb.getEncoded();
        csrb.write(new FileWriter(new File(database, "keys.csr")));
        order.execute(csr);
        int count = 10;
        while (order.getStatus() != Status.VALID && count > 0) {
            count--;
            logger.info("Waiting for order status, " + count + " time(s) left.");
            Thread.sleep(3000L);
            order.update();
        }
        if (order.getStatus() != Status.VALID) {
            throw new IllegalStateException("ACME Timeout");
        }
        Certificate certificate = order.getCertificate();
        assert certificate != null;
        X509Certificate cert = certificate.getCertificate();

        char[] password = Uniporter.getRouteConfig().getSslKeyStorePassword().toCharArray();
        KeyStore store = KeyStore.getInstance(Constants.KEY_STORE_FORMAT);
        store.load(null, password);
        store.setCertificateEntry("letsencrypt", cert);
        store.setKeyEntry("letsencrypt_private", privateKey.getPrivate(), password,
                certificate.getCertificateChain().toArray(new X509Certificate[0]));
        store.store(new FileOutputStream(Uniporter.getRouteConfig().getKeyStore().getPath()), password);
    }

    Http01Challenge currentChallenge;

    CountDownLatch latch = new CountDownLatch(1);

    protected void process(Authorization auth) throws AcmeException, InterruptedException {
        currentChallenge = auth.findChallenge(Http01Challenge.class);
        assert currentChallenge != null;
        currentChallenge.trigger();
        int count = 10;
        logger.info("Token: " + currentChallenge.getToken());
        logger.info("Content: " + currentChallenge.getAuthorization());
        while (auth.getStatus() != Status.VALID && count > 0) {
            count--;
            logger.info("Waiting for auth status, " + count + "time(s) left, current status " + auth.getStatus());
            Thread.sleep(3000L);
            auth.update();
        }
        if (auth.getStatus() != Status.VALID) {
            throw new IllegalStateException("ACME Timeout");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, FullHttpRequest msg) throws Exception {
        try {
            if (!msg.uri().contains(".well-known/acme-challenge")) {
                throw new IllegalStateException();
            }
            String token = msg.uri().substring(msg.uri().lastIndexOf(".well-known/acme-challenge") + (".well-known" +
                    "/acme-challenge").length()).replaceAll("[/]", "");
            logger.info("Accessing token: " + token);
            if (currentChallenge != null && currentChallenge.getToken().equalsIgnoreCase(token)) {
                FullHttpResponse response = new DefaultFullHttpResponse(
                        HttpVersion.HTTP_1_1,
                        HttpResponseStatus.OK,
                        Unpooled.copiedBuffer(currentChallenge.getAuthorization(),
                                StandardCharsets.UTF_8));
                response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain");
                context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                latch.countDown();
            } else {
                IllegalHttpStateException.send(context, HttpResponseStatus.NOT_FOUND, "Not a valid token");
            }
        } catch (Throwable e) {
            IllegalHttpStateException.send(context, e);
        }
    }
}
