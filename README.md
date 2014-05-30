Voikko Analysis for Elasticsearch
=================================

The Voikko Analysis plugin provides Finnish language analysis using [Voikko](http://voikko.puimula.org/).

[![Build Status](https://drone.io/bitbucket.org/evidentsolutions/elasticsearch-analysis-voikko/status.png)](https://drone.io/bitbucket.org/evidentsolutions/elasticsearch-analysis-voikko/latest)

Installing
----------

The plugin needs `libvoikko` shared library to work. Details of installing the library varies
based on operating system. In Debian based systems `apt-get install libvoikko1` should work.

Next, you'll need to download [morpho dictionary](http://www.puimula.org/htp/testing/voikko-snapshot/dict-morpho.zip).
Unzip this into Voikko's dictionary directory (e.g. `/usr/lib/voikko` in Debian) or into a directory you specify with
`dictionaryPath` configuration property.

Finally, to install the plugin, run: `bin/plugin --install fi.evident.elasticsearch/elasticsearch-analysis-voikko/0.2.0`.

Docker-image
------------

If you are using [Docker](https://www.docker.io/), you can use [komu/elasticsearch-voikko](https://index.docker.io/u/komu/elasticsearch-voikko/) image to get Elasticsearch instance
with the above installations included.

Configuring
-----------

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

    -------------------------------------------------------------------------------------------
    | Parameter         | Default value    | Description                                      |
    -------------------------------------------------------------------------------------------
    | language          | fi_FI            | Language to use                                  |
    -------------------------------------------------------------------------------------------
    | dictionaryPath    | system dependent | path to voikko dictionaries                      |
    -------------------------------------------------------------------------------------------
    | analyzeAll        | false            | Use all analysis possibilities or just the first |
    -------------------------------------------------------------------------------------------
    | minimumWordSize   | 3                | minimum length of words to analyze               |
    -------------------------------------------------------------------------------------------
    | maximumWordSize   | 100              | maximum length of words to analyze               |
    -------------------------------------------------------------------------------------------
    | libraryPath       | system dependent | path to directory containing libvoikko           |
    -------------------------------------------------------------------------------------------
    | poolMaxSize       | 10               | maximum amount of Voikko-instances to pool       |
    -------------------------------------------------------------------------------------------
    | analysisCacheSize | 1024             | number of analysis results to cache              |
    -------------------------------------------------------------------------------------------

Development
-----------

To run the tests, you need to specify `voikko.home` system property which should point to
a directory containing libvoikko shared library and subdirectory `dicts` which contains
the [morpho dictionary](http://www.puimula.org/htp/testing/voikko-snapshot/dict-morpho.zip).

License
-------

This library is released under the LGPL, version 2.1 or later.