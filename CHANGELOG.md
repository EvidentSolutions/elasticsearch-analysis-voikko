## x.y.z (yyyy-mm-dd)

  - Added tokenizer that is suitable for tokenizing Finnish text.

## 0.1.4 (2013-11-18)

  - Use 'fi_fi-x-morpho' as the default language, forcing use of morpho-dictionary.

## 0.1.3 (2013-09-18)

  - Allow configuring size of analysis-cache.
  - Cache reads were made with read-lock, although they updated the access order, causing possible corruption.

## 0.1.2 (2013-09-12)

  - Pooling Voikko-instances for better multi-threaded performance.

## 0.1.1 (2013-09-11)

  - Handle unknown tokens correctly.

## 0.1.0 (2013-08-23)

  - Initial revision.
