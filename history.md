0.3.1 / 2013-10-02
=================
* Adding support for `Long`, and now generally any primitive or wrapper.

0.3.0 / 2013-09-04
=================
* Adding support for `List`, `Array`, and `BigDecimal` for `Props` which means traits and event properties can now contain these items
* Fixing bug where Flusher thread can continue without realizing that flushing should stop
* Renaming `Client` to `AnalyticsClient
* Adding statistics around messages dropped during high queue conditions
* Adding Analytics.VERSION to payload

0.2.0 / 2013-04-23
=================
* Updating flush mechanism to a single thread that flushes in the background, and in batches when it can
* Removing `flushAt`, `flushAfter` options
* Adding alias command
* Removing async-http-client as dependency and replacing with Apache's HTTPComponents. Netty no longer needs to start to flush

0.1.7 / 2013-03-22
=================
* Adding context.providers and nested properties support

0.1.6 / 2013-03-22
=================
* Fixed utf-8 encoding issue

0.1.4 / 2013-01-18
=================
* Setting `flushAt` default to 20

0.1.3 / 2013-01-17
=================
* Removing sessionId
* Fixing POM

0.1.0 / 2013-01-14
=================
* Repo going public