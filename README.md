# Voikko Analysis for Elasticsearch

The Voikko Analysis plugin provides Finnish language analysis using [Voikko](http://voikko.puimula.org/).

[![Build Status](https://drone.io/bitbucket.org/evidentsolutions/elasticsearch-analysis-voikko/status.png)](https://drone.io/bitbucket.org/evidentsolutions/elasticsearch-analysis-voikko/latest)

## Supported versions

| Plugin version | Elasticsearch version |
| -------------- | ----------------------|
| 0.4.0          | 2.2.1                 |
| 0.3.0          | 1.5.2                 |

## Installing

The plugin needs `libvoikko` shared library to work. Details of installing the library varies
based on operating system. In Debian based systems `apt-get install libvoikko1` should work.

Next, you'll need to download [morpho dictionary](http://www.puimula.org/htp/testing/voikko-snapshot/dict-morpho.zip).
Unzip this into Voikko's dictionary directory (e.g. `/usr/lib/voikko` in Debian) or into a directory you specify with
`dictionaryPath` configuration property.

Finally, to install the plugin, run: `bin/plugin install fi.evident.elasticsearch/elasticsearch-analysis-voikko/0.4.0`.

### Security manager

Elasticsearch 2.x ships with a security manager enabled by default. Plugins can specify the permissions
that they need in `plugin-security.policy`. However, elasticsearch-analysis-voikko uses
[JNA library](https://github.com/java-native-access/jna) which is already distributed with Elasticsearch
and therefore can't be included in the plugin zip. This means that the security policy bundled with the
plugin will not apply to JNA, yet it should be able to load `libvoikko` from the system.

Having tried various workarounds, the only solution I've found is to disable the security manager when starting
Elasticsearch:

```
bin/elasticsearch --security.manager.enabled=false
```

This is somewhat unfortunate, but no less secure than running ES 1.x which did not include a security manager.
I'd be happy to get pull requests for something better.

## Configuring

Include `finnish` tokenizer and `voikko` filter in your analyzer, for example:

    :::json
    {
      "index": {
        "analysis": {
          "analyzer": {
            "default": {
              "tokenizer": "finnish",
              "filter": ["lowercase", "voikkoFilter"]
            }
          },
          "filter": {
            "voikkoFilter": {
              "type": "voikko"
            }
          }
        }
      }
    }

You can use the following filter options to customize the behaviour of the filter:

| Parameter         | Default value    | Description                                      |
|-------------------|------------------|--------------------------------------------------|
| language          | fi_FI            | Language to use                                  |
| dictionaryPath    | system dependent | path to voikko dictionaries                      |
| analyzeAll        | false            | Use all analysis possibilities or just the first |
| minimumWordSize   | 3                | minimum length of words to analyze               |
| maximumWordSize   | 100              | maximum length of words to analyze               |
| libraryPath       | system dependent | path to directory containing libvoikko           |
| poolMaxSize       | 10               | maximum amount of Voikko-instances to pool       |
| analysisCacheSize | 1024             | number of analysis results to cache              |

## Development

To run the tests, you need to specify `voikko.home` system property which should point to
a directory containing libvoikko shared library and subdirectory `dicts` which contains
the [morpho dictionary](http://www.puimula.org/htp/testing/voikko-snapshot/dict-morpho.zip).

## License

This library is released under the LGPL, version 2.1 or later.


```
 curl -XPUT 'localhost:9200/myindex' -d '{
  "index": {
    "analysis": {
      "analyzer": {
        "default": {
          "tokenizer": "finnish",
          "filter": ["lowercase", "voikkoFilter"]
        }
      },
      "filter": {
        "voikkoFilter": {
          "type": "voikko",
          "dictionaryPath": "/Users/komu/dev/voikko/dicts"
        }
      }
    }
  }
}
```

```
curl -XPOST 'localhost:9200/myindex/1?pretty' -d '{ "doc": { "name": "kissansa" }}'
```

```
bin/plugin remove elasticsearch-analysis-voikko && bin/plugin install file:/Users/komu/src/evident/elasticsearch-analysis-voikko/target/releases/elasticsearch-analysis-voikko-0.4.0-SNAPSHOT.zip && bin/elasticsearch -Djava.security.policy=foo.policy
```