
Version 2.0.0 (April 4th, 2017)
===============================

  * [New](https://github.com/segmentio/analytics-java/pull/99): Make endpoint configurable.
  * [New](https://github.com/segmentio/analytics-java/pull/101): Allow setting a custom message ID.
  * [New](https://github.com/segmentio/analytics-java/pull/101): Allow setting a custom timestamp.

Version 2.0.0-RC7 (August 22nd, 2016)
=====================================

  * Fix: Previously, logging Retrofit messages could cause errors if the message contained formatting directives.

Version 2.0.0-RC6 (August 18th, 2016)
=====================================

  * New: Add ability to set multiple Callback instances.
  * New: Add plugin API.

```java
class LoggingPlugin implements Plugin {
  @Override public void configure(Analytics.Builder builder) {
    builder.log(new Log() {
      @Override public void print(Level level, String format, Object... args) {
        System.out.println(level + ":\t" + String.format(format, args));
      }

      @Override public void print(Level level, Throwable error, String format, Object... args) {
        System.out.println(level + ":\t" + String.format(format, args));
        System.out.println(error);
      }
    });

    builder.callback(new Callback() {
      @Override public void success(Message message) {
        System.out.println("Uploaded " + message);
      }

      @Override public void failure(Message message, Throwable throwable) {
        System.out.println("Could not upload " + message);
        System.out.println(throwable);
      }
    });
  }
}

final Analytics analytics = Analytics.builder(writeKey) //
        .plugin(new LoggingPlugin())
        .build();
```

Version 2.0.0-RC5 (August 12th, 2016)
=====================================

  * Fix: Correctly format and parse dates as per ISO 8601.

Version 2.0.0-RC4 (May 19th, 2016)
==================================

  * New: Add Page API.

Version 2.0.0-RC3 (Dec 15th, 2015)
==================================

  * Fix: Force ISO 8601 format for dates.
  * Use a single thread by default to upload events in the background. Clients can still set their own executor to override this behaviour.

Version 2.0.0-RC2 (Oct 31st, 2015)
==================================

  * New: Add Callback API.
  * Fix: Backpressure behaviour. Enqueuing events on a full queue will block instead of throwing an exception.
  * Removed Guava dependency.

Version 2.0.0-RC1 (Aug 26th, 2015)
==================================

  * Internal: Rename enums with lowercase.
