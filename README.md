# Adobe Experience Manager (AEM) Versioned Assets
[![Build Status](https://travis-ci.org/marto/aem-versioned-assets.svg?branch=master)](https://travis-ci.org/marto/aem-versioned-assets)

A good web-performance practice is to make web assets that rarely change to be cached for a very long time. By increasing the "cacheability" of a site, it's performance / speed will naturaly increase. The web assets (think all css, js, fonts and iconography) of your site can typically be cached in a CDN, Apache Dispatcher and most importantly on the client's browser. The longer the better.

This project transparently adds a versioned fingerprint to all configured paths (usualy your design/clientlibs path) and rewrites all html output to reference these assets via the fingerprinted URL. For example suppose you house all your iconography, css and js of your site in "/etc/designs/weretail". When configured, any html output genereated by AEM will rewrite your css, js links from `/etc/designs/weretail/clientlibs/core.css` to `/etc/designs/weretail/v-9-v/clientlibs/core.css` where the /v-9-v/ is the unique fingerprint that will change on every deployment. Any iconography, fonts etc references from the css / javascript should use relative path and thus all iconography will also benefit from being able to be cached forever.

## Full Example
TODO

## Setup & Install
TODO

## Releasing Project
```
  Add this to your ~/.m2/setting.xml
  <servers>
    <!-- OSSRH -->
    <server>
      <id>ossrh</id>
      <username>${env.OSSRH_USER}</username>
      <password>${env.OSSRH_PASS}</password>
    </server>
    <server>
      <id>gpg.passphrase</id>
      <passphrase>${env.GPG_PASS}</passphrase>
    </server>
    <server>
       <id>github</id>
       <username>marto</username>
       <password>${env.GIT_TOKEN}</password>
    </server>
  </servers>

 # Setup your creds
  unset HISTFILE
   export GIT_USER=
   export GPG_PASS=
   export OSSRH_USER=
   export OSSRH_PASS=
   export GIT_TOKEN=

 # Then Run

 git co develop
 git mvn -P release release:prepare
 git mvn -P release release:perform
```

# Get token from https://github.com/settings/

### Project Struture

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
│       │                       ├── AssetVersionService.java
│       │                       ├── impl
│       │                       │   ├── AssetVersionServiceImpl.java
│       │                       │   ├── AssetVersionTransformerFactory.java
│       │                       │   └── AssetVersionTransformer.java
│       │                       ├── model
│       │                       │   └── Configuration.java
│       │                       ├── servlet
│       │                       │   ├── AbstractSlingFilter.java
│       │                       │   ├── AssetVersionFilter.java
│       │                       │   ├── AssetVersionUpdateServlet.java
│       │                       │   ├── ComponentContextFilter.java
│       │                       │   ├── RequestContext.java
│       │                       │   └── scrap.jpage
│       │                       └── VersionedAssetUpdateException.java
│       └── test
│           └── java
│               └── io
│                   └── marto
│                       └── aem
│                           └── vassets
│                               ├── impl
│                               │   ├── AssetVersionServiceImplTest.java
│                               │   ├── AssetVersionTransformerFactoryTest.java
│                               │   ├── AssetVersionTransformerTest.java
│                               │   └── TestTypedResourceResolverFactory.java
│                               └── servlet
│                                   ├── AssetVersionFilterTest.java
│                                   ├── ComponentContextFilterTest.java
│                                   └── TestConfiguration.java
├─── versioned-assets-ui
│    ├── pom.xml
│    └── src
│        └── main
│            └── content
│                └─── jcr_root
│                    ├── apps
│                    │   └── vassets
│                    │       ├── components
│                    │       │   └── page
│                    │       │       └── asset-version-configuration
│                    │       │           ├── asset-version-configuration.html
│                    │       │           ├── dialog.xml
│                    │       │           └── scaffolding
│                    │       │               └── _jcr_content
│                    │       │                   └── dialog.xml
│                    │       ├── install
│                    │       └── templates
│                    │           └── asset-version-configuration
│                    └── etc
│                        └── vassets
│                            └── _rep_policy.xml
├── config-example
│   ├── package.sh
│   └── src
│       └─── jcr_root
│           └── apps
│               └── vassets-conf
│                   └── config
│                       ├── org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended-account.xml     <<< Service User Configuration
│                       └── rewriter
│                           └── versioned-assets                                                                   <<< Rewriter configuration example
└─── service-user-and-acls
   ├── package.sh
   └── src
       └─── jcr_root
           ├── content
           │   └── _rep_policy.xml                                                                                <<< Permissions
           ├── etc
           │   └── vassets
           │       └── _rep_policy.xml                                                                            <<< Permissions
           └── home
               └── users
                   └── system
                       └── versioned-assets-service                                                               <<< OSGi Service User 
```

