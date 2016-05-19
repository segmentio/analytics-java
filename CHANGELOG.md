
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
