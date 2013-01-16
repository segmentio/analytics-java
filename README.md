analytics-java
==============

[![Build Status](https://travis-ci.org/segmentio/analytics-java.png)](https://travis-ci.org/segmentio/analytics-java)


analytics-java is a java client for [Segment.io](https://segment.io). If you're using client-side javascript, check out [analytics.js](https://github.com/segmentio/analytics.js).

### Java Analytics Made Simple

[Segment.io](https://segment.io) is the cleanest, simplest API for recording analytics data.

Setting up a new analytics solution can be a real pain. The APIs from each analytics provider are slightly different in odd ways, code gets messy, and developers waste a bunch of time fiddling with long-abandoned client libraries. We want to save you that pain and give you an clean, efficient, extensible analytics setup.

[Segment.io](https://segment.io) wraps all those APIs in one beautiful, simple API. Then we route your analytics data wherever you want, whether it's Google Analytics, Mixpanel, Customer io, Chartbeat, or any of our other integrations. After you set up Segment.io you can swap or add analytics providers at any time with a single click. You won't need to touch code or push to production. You'll save valuable development time so that you can focus on what really matters: your product.

```javascript
Analytics.initialize("YOUR_SECRET_KEY");
Analytics.track("ilya@segment.io", "Listened to a Song");
```

and turn on integrations with just one click at [Segment.io](https://segment.io).

![](http://i.imgur.com/YnBWI.png)

More on integrations [here](#integrations).

### High Performance

This client uses an internal queue to efficiently send your events in aggregate, rather than making an HTTP
request every time. This means that it is safe to use in your high scale web server controllers, or in your backend services
without worrying that it will make too many HTTP requests and slow down the program. You no longer need to use a message queue to have analytics.

[Feedback is very welcome!](mailto:friends@segment.io)

## Quick-start

If you haven't yet, get an API secret [here](https://segment.io).

#### Install

Maven repository hosting coming soon. For now, clone the project and include it in your project dependencies in eclipse.

#### Initialize the client

To get started, just initialize the `Analytics` singleton once:

```java
Analytics.initialize("YOUR_SECRET_KEY");
```

#### Identify a User

Identifying a user ties all of their actions to an id, and associates user `traits` to that id.

```javascript
void identify(String sessionId,
              String userId,
              DateTime timestamp,
              Traits traits);
```

**sessionId** (String) is a unique id associated with an anonymous user **before** they are logged in. If the user
is logged in, you can just omit it.

**userId** (String) is the user's id **after** they are logged in. It's the same id as which you would recognize a signed-in user in your system. Note: you must provide either a `sessionId` or a `userId`.

**traits** (Traits) is a dictionary with keys like `subscriptionPlan` or `age`. You only need to record a trait once, no need to send it again.

**timestamp** (DateTime, optional) is a jodatime `DateTime`representing when the track took place. If the **identify** just happened, leave it blank and we'll use the server's time. If you are importing data from the past, make sure you provide this argument.

```java
Analytics.identify("507f1f77bcf8", new Traits()
                    .put("name", "Achilles")
                    .put("email", "achilles@segment.io")
                    .put("subscriptionPlan", "Premium")
                    .put("friendCount", 29));
```

#### Track an Action

Whenever a user triggers an event, you’ll want to track it.

```java
void identify(String sessionId,
              String userId,
              String event,
              DateTime timestamp,
              EventProperties properties);
```

**sessionId** (String) is a unique id associated with an anonymous user **before** they are logged in. Even if the user
is logged in, you can still send us the **sessionId** or you can just omit it.

**userId** (String) is the user's id **after** they are logged in. It's the same id as which you would recognize a signed-in user in your system. Note: you must provide either a `sessionId` or a `userId`.

**event** (String) describes what this user just did. It's a human readable description like "Played a Song", "Printed a Report" or "Updated Status".

**timestamp** (DateTime, optional) is a Javascript date object representing when the track took place. If the event just happened, leave it blank and we'll use the server's time. If you are importing data from the past, make sure you provide this argument.

**properties** (EventProperties) is a dictionary with items that describe the event in more detail. This argument is optional, but highly recommended—you’ll find these properties extremely useful later.

```java
Analytics.track("507f1f77bcf8", "Purchased Item", new EventProperties()
                    .put("revenue", 39.95)
                    .put("shippingMethod", "2-day");
```

That's it, just two functions!

## Integrations

There are two main modes of analytics integration: client-side and server-side. You can use just one, or both.

#### Client-side vs. Server-side

* **Client-side analytics** - (via [analytics.js](https://github.com/segmentio/analytics.js)) works by loading in other integrations
in the browser.

* **Server-side analytics** - (via [analytics-node](https://github.com/segmentio/analytics-node), [analytics-python](https://github.com/segmentio/analytics-python), [analytics-ruby](https://github.com/segmentio/analytics-ruby), [analytics-java](https://github.com/segmentio/analytics-java) and other server-side libraries) works
by sending the analytics request to [Segment.io](https://segment.io). Our servers then route the message to your desired integrations.

Some analytics services have REST APIs while others only support client-side integrations.

You can learn which integrations are supported server-side vs. client-side on your [project's integrations]((https://segment.io) page.

## Advanced

### Batching Behavior

By default, the client will flush:

+ the first time it gets a message
+ every message (control with `flushAt`)
+ if 10 seconds has passed since the last flush (control with `flushAfter`)

#### Enable Batching

Batching allows you to not send an HTTP request every time you submit a message. In high scale environments, it's a good ide to set `flushAt` to about 25, meaning the client will flush every 25 messages.

```java
Analytics.iniitialize("YOUR_API_SECRET", new Options().setFlushAt(25));
````

#### Flush Whenever You Want

At the end of your program, you may want to flush to make sure there's nothing left in the queue.

```java
Analytics.flush();
```

#### Why Batch?

This client is built to support high performance environments. That means it is safe to use analytics-java in a web server that is serving hundreds of requests per second.

**How does the batching work?**

Every action **does not** result in an HTTP request, but is instead queued in memory. Messages are flushed in batch in the background, which allows for much faster operation.

**What happens if there are just too many messages?**

If the client detects that it can't flush faster than it's receiving messages, it'll simply stop accepting messages. This means your program won't crash because of a backed up analytics queue.

#### Message Acknowledgement

Batching means that your message might not get sent right away.

**How do I know when this specific message is flushed?**

Every `identify` and `track` accepts a Callback in its most verbose overload.

```java
Analytics.identify("aksj2kdj2kj2kje", "507f1f77bcf8",
                        new Context(), DateTime.now(),
                        new Traits()
                        .put("name", "Achilles")
                        .put("email", "achilles@segment.io")
                        .put("subscriptionPlan", "Premium")
                        .put("friendCount", 29), new Callback() {

                            public void onResponse(Response response) {
                                if (response.getStatusCode() == 200) {
                                    // success
                                } else {
                                    // failure
                                    // System.err.println(response.getResponseBody());
                                }
                            }
                });
```

Remember to use this only for debugging. The callback gets executed on the asynchronous
event loop powering Netty's web client. If you run long running operations on the callback,
then you risk breaking the client.

### Understanding the Client Options

If you hate defaults, than you'll love how configurable the analytics-java is.
Check out these gizmos:

```java
Analytics.initialize("MY_API_SECRET", new Options()
                                        .setFlushAt(50)
                                        .setFlushAfter((int)TimeUnit.SECONDS.toMillis(10))
                                        .setMaxQueueSize(10000));
```

* **flushAt** (int) - Flush after this many messages are in the queue.
* **flushAfter** (int) - Flush after this many milliseconds have passed since the last flush.
* **maxQueueSize** (int) - Stop accepting messages into the queue after this many messages are backlogged in the queue.

### Multiple Clients

Different parts of your app may require different types of batching. In that case, you can initialize different `analytic-java` client instances. `Analytics.initialize` becomes the `Client`'s constructor.

```java
Client client = new Client("testsecret", new Options()
                                    .setFlushAt(50)
                                    .setFlushAfter((int)TimeUnit.SECONDS.toMillis(10))
                                    .setMaxQueueSize(10000));
client.track(..);
```

## Testing

```bash
mvn test
```

## License

```
WWWWWW||WWWWWW
 W W W||W W W
      ||
    ( OO )__________
     /  |           \
    /o o|    MIT     \
    \___/||_||__||_|| *
         || ||  || ||
        _||_|| _||_||
       (__|__|(__|__|
```

(The MIT License)

Copyright (c) 2012 Segment.io Inc. <friends@segment.io>

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.