package com.segment.analytics;

import com.segment.analytics.messages.Message;
import com.segment.analytics.messages.MessageBuilder;
import java.util.concurrent.Phaser;

/**
 * The {@link Analytics} class doesn't come with a blocking {@link Analytics#flush()} implementation
 * out of the box. Use this class to be able to block until the client has processed all in-flight
 * messages.
 *
 * <pre><code>
 * BlockingFlushPlugin blockingFlushPlugin = BlockingFlushPlugin.create();
 * Analytics analytics = Analytics.builder(writeKey)
 *      .plugin(blockingFlushPlugin)
 *      .build();
 *
 * // Do some work.
 *
 * analytics.flush(); // Trigger a flush.
 * blockingFlush.block(); // Block until the flush completes.
 * analytics.shutdown(); // Shut down after the flush is complete.
 * </code></pre>
 */
@Beta
public class BlockingFlushPlugin implements Plugin {

  public static BlockingFlushPlugin create() {
    return new BlockingFlushPlugin();
  }

  private BlockingFlushPlugin() {
    this.phaser = new Phaser(1);
  }

  private final Phaser phaser;

  @Override public void configure(Analytics.Builder builder) {
    builder.messageTransformer(new MessageTransformer() {
      @Override public boolean transform(MessageBuilder builder) {
        phaser.register();
        return true;
      }
    });

    builder.callback(new Callback() {
      @Override public void success(Message message) {
        phaser.arrive();
      }

      @Override public void failure(Message message, Throwable throwable) {
        phaser.arrive();
      }
    });
  }

  public void block() {
    phaser.arriveAndAwaitAdvance();
  }
}
