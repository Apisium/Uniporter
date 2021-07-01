# Uniporter

A netty wrapper for Minecraft, which allows running multiple protocols in same port.

Currently, works for HTTP/1.1 standards and regular SSL, more protocols will be added later on.

No direct NMS codes are used, and tested on Spigot 1.17 and 1.12.2.

## Quick Start

1. Put the jar into `/plugin`.
2. Start or reload the server.
3. Modify `/plugin/Uniporter/route.yml`.
4. Put some html in `/plugin/Uniporter/static/` or other paths in the `route.yml`.
5. Restart or reload the server.
6. You are all good. By default `http://serverip:25565` (assuming your minecraft runs on 25565) will show you something :)

## Route Configuration Explanation

```yaml
# Debug environment, default: false
debug: false
# JKS format SSL keystore
keystore:
    # Keystore path, relative to data folder
    path: keystore.jks
    # Keystore password, you should probably change it
    password: uniporter
# Servers need to be enable
server:
    # Listen on Minecraft port
    :minecraft:
        # Listen on path "/"
        /:
            # Static resources should be handled
            handler: static
    # Uncomment below to see how config works
    ## Listen on port 80, with any host name localhost
    #:80:
    #    handler: static
    #    # Gzip enable or not, default: true
    #    gzip: false
    #    # Extra options
    #    options:
    #        # SSL enable or not, default: false
    #        ssl: false
    #        # Path of static resources relative to data folder, default: ./static
    #        path: ./html
    #    # Extra headers
    #    headers:
    #        # Key is header's name, value is header's value
    #        TestHeader: 'This is a test'
    #    # Listen to hosts, each line is a regex
    #    hosts:
    #        - '^localhost$'
```

## Developer Hints

Okay, if you want to do something fancy with your programming ability, sadly I haven't finished the dev doc, but the javadoc is already done.

Where? Inside the source code :(

Have a look into `cn.apisium.uniporter.Uniporter` and `cn.apisium.uniporter.example`, you'll get the idea!

## FAQ

### Q: Does it support version x.xx.x?

Well, since the plugin contains regular Netty codes, and a non-hardcoded reflection helper, I would argue that it most likely works on all modern minecraft bukkit/spigot/paper server.

However, it is not guarantee to be worked on any forge or fabric hybrid servers, since I'm not entirely sure about how they handled the Netty channel instance. This will be resolved in near future.

### Q: Does it breaks vanilla mechanics?

For gameplay aspect, **no**. The code is supposed to be harmless to vanilla gameplay.

For network aspect, well, sort of, but **it won't break anything**, here is the technical explanation:

Since the plugin need to read the very first byte of handshake packets (like legacy server ping handler does), so if you have a handshake packet start with byte:

- `22` (the SSL handshake byte) or
- any of character `'G', 'H', 'P', 'D', 'C', 'O', 'T'` (the start of a Http request byte for HTTP/1.1)

from vanilla, it broke. Fortunately, as for as I know, there is no such packet exist at least for versions below 1.17.x inclusive.

### Q: Will it be compatible to plugin xxx?

Most likely yes, unless they change the default Netty behavior of the server (which should be vary vary vary rare).

---

Okay, above are questions I made up, if you have anything wanted to add to the list, feel free to submit an issue.

By the way, officially supported issue languages are:

- English (but you might already notice that it is not my native language) and
- Chinese (both simplified or traditional are fine, but I will reply you issue in simplified)
- Marsnese, I mean, Chinese styled Mars language, aka 火星文 :)

Oh, here should be one more Q:

### Q: Why don't you create a Chinese version Readme?

Because I don't want to rewrite the whole thing twice, or maybe you can do the translation :)

PR welcome!

---

Yeah, I know this doc has insufficient information, I will do some more work later on, for now, if you have any enquire, please submit an issue or contact me on Telegram: https://t.me/Baleine_2000
