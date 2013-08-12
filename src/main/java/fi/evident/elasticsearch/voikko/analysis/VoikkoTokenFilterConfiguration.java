package fi.evident.elasticsearch.voikko.analysis;

final class VoikkoTokenFilterConfiguration {

    /** If true, use analysis candidates returned by Voikko, otherwise use only the first result. */
    boolean analyzeAll = false;

    /** Words shorter than this threshold are ignored */
    int minimumWordSize = 3;

    /** Words longer than this threshold are ignored */
    int maximumWordSize = 100;

}
