# Verssioned Assets
A good practice is to make assets that never change longed lived to increase the cachability of your site. This project adds a versioned fingerprint to
paths and rewrites all html output to reference any assets in the directory via a versioned fingerprinted URL. For example supose you have /etc/designs/myproject
where you keep all your iconography css and javascript. You setup "Versioned Assets" to rewrite 

    `/etc/designs/myproject/clientlibs/core.css`  to `/etc/designs/myproject/v-9-v/clientlibs/core.css` and use the versioned assets to bump

the entire directory on every deploy. All html output will be rewritted to use the fingerprinted URL transparently.


```
.
├── versioned-assets
│   ├── pom.xml
│   └── src
│       ├── main
│       │   └── java
│       │       └── io
│       │           └── marto
│       │               └── aem
│       │                   └── vassets
│       │                       ├── impl
│       │                       │   ├── AssetVersionTransformerFactoryImpl.java
│       │                       │   └── AssetVersionTransformerImpl.java                <<< Transformer
│       │                       ├── model
│       │                       │   └── Configuration.java                              <<< Sling model that encapsulates configuration
│       │                       ├── servlet
│       │                       │   ├── AbstractSlingFilter.java
│       │                       │   ├── AssetVersionFilter.java
│       │                       │   ├── AssetVersionUpdateServlet.java
│       │                       │   ├── ComponentContextFilter.java
│       │                       │   └─── RequestContext.java
r       │                       ├── VersionedAssets.java
│       │                       └── VersionedAssetUpdateException.java
│       └── test
│           └── java
│               └── io
│                   └── marto
│                       └── aem
│                           └── vassets
│                               ├── impl
│                               │   ├── AssetVersionTransformerFactoryImplTest.java     <<<< TESTS TODO!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
│                               │   └── AssetVersionTransformerImplTest.java
│                               └── servlet
│                                   └── AssetVersionUpdateServletTest.java
├── versioned-assets-ui
│   ├── pom.xml
│   └── src
│       └── main
│           └── content
│               └─── jcr_root
│                   ├── apps
│                   │   └── vassets
│                   │       ├── components
│                   │       │   └── page
│                   │       │       └── asset-version-configuration                     <<<< Scafolding UI
│                   │       │           ├── asset-version-configuration.html
│                   │       │           ├── dialog.xml
│                   │       │           └── scaffolding
│                   │       │               └── _jcr_content
│                   │       │                   └── dialog.xml
│                   │       ├── config
│                   │       │   └── org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-versioned-assets.xml
│                   │       ├── install
│                   │       └── templates
│                   │           └── asset-version-configuration
│                   └─── etc
│                       └── vassets
│                           └── _rep_policy.xml
├── config-example
│   ├── package.sh
│   └── src
│       └── jcr_root
│           └── apps
│               └── sol-dig
│                   └── config
│                       └── rewriter
│                           └── versioned-assets                                      <<< Rewrite configuration example
├── service-conf
│   ├── package.sh
│   └── src
│       └── jcr_root
│           └── apps
│               └── vassets                                                            <<< Service User Configuration
│                   └── config
│                       └── org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-account.xml
└─── service-conf-user
    ├── package.sh                                                                  <<< ACLs and and Service User Principal eample config
    └── src
        └── jcr_root
            ├── content
            │   └── _rep_policy.xml
            ├── etc
            │   └── vassets
            │       └── _rep_policy.xml
            └── home
                └── users
                    └── system
                        └── versioned-assets-service
```

