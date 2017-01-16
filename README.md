# Voikko Analysis for Elasticsearch

The Voikko Analysis plugin provides Finnish language analysis using [Voikko](http://voikko.puimula.org/).

[![Build Status](https://drone.io/bitbucket.org/evidentsolutions/elasticsearch-analysis-voikko/status.png)](https://drone.io/bitbucket.org/evidentsolutions/elasticsearch-analysis-voikko/latest)

## Supported versions

| Plugin version | Elasticsearch version |
| -------------- | ----------------------|
| 0.5.0          | 5.1.1                 |
| 0.4.0          | 2.2.1                 |
| 0.3.0          | 1.5.2                 |

## Installing

### Installing Voikko

The plugin needs `libvoikko` shared library to work. Details of installing the library varies
based on operating system. In Debian based systems `apt-get install libvoikko1` should work.

Next, you'll need to download [morpho dictionary](http://www.puimula.org/htp/testing/voikko-snapshot/dict-morpho.zip) 
(for libvoikko version 4.0+, use [morpho dict v5](http://www.puimula.org/htp/testing/voikko-snapshot-v5/dict-morpho.zip) 
instead).
Unzip this into Voikko's dictionary directory (e.g. `/usr/lib/voikko` in Debian) or into a directory you specify with
`dictionaryPath` configuration property.

### Installing the plugin

Finally, to install the plugin, run: 

```
bin/elasticsearch-plugin install https://repo1.maven.org/maven2/fi/evident/elasticsearch/elasticsearch-analysis-voikko/0.5.0/elasticsearch-analysis-voikko-0.5.0.zip
```

### Security policy

Elasticsearch ships with a pretty restrictive security policy. Plugins can specify the permissions
that they need in `plugin-security.policy`. However, elasticsearch-analysis-voikko uses
[JNA library](https://github.com/java-native-access/jna) which is already distributed with Elasticsearch
and therefore can't be included in the plugin zip. This means that the security policy bundled with the
plugin will not apply to JNA, yet it should be able to load `libvoikko` from the system.

Therefore you need to create a custom security policy, granting Elasticsearch itself the permission
to load `libvoikko`:

```
grant {
  permission java.io.FilePermission "<<ALL FILES>>", "read";
  permission java.lang.reflect.ReflectPermission "newProxyInPackage.org.puimula.libvoikko";
};
```

(You don't really need to grant read access to `<<ALL FILES>>`, you can pass the location
of `libvoikko` instead.)

Save this as `custom-elasticsearch.policy` and tell Elasticsearch to load it:

```
export ES_JAVA_OPTS=-Djava.security.policy=file:/path/to/custom-elasticsearch.policy
```

### Verify installation

After installing the plugin, you can quickly verify that it works by executing:

```
curl -XGET 'localhost:9200/_analyze' -d '
{
  "tokenizer" : "finnish",
  "filter" : [{"type": "voikko", "libraryPath": "/directory/of/libvoikko", "dictionaryPath": "/directory/of/voikko/dictionaries"}],
  "text" : "Testataan voikon analyysiä tällä tavalla yksinkertaisesti."
}'
```

If this works without error messages, you can proceed to configure the plugin index.

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

This library is released under the [Apache License, Version 2.0](http://apache.org/licenses/LICENSE-2.0).
