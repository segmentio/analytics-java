package cli

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.segment.analytics.Analytics
import com.segment.analytics.Callback
import com.segment.analytics.messages.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

data class CLIOutput(
    val success: Boolean,
    val error: String? = null,
    val sentBatches: Int = 0
)

data class CLIConfig(
    val flushAt: Int? = null,
    val flushInterval: Long? = null,
    val maxRetries: Int? = null,
    val timeout: Int? = null
)

data class EventSequence(
    val delayMs: Long = 0,
    val events: List<Map<String, Any>>
)

data class CLIInput(
    val writeKey: String,
    val apiHost: String,
    val sequences: List<EventSequence>,
    val config: CLIConfig? = null
)

private val gson = Gson()

fun main(args: Array<String>) {
    var output = CLIOutput(success = false, error = "Unknown error")

    try {
        // Parse --input argument
        val inputIndex = args.indexOf("--input")
        if (inputIndex == -1 || inputIndex + 1 >= args.size) {
            throw IllegalArgumentException("Missing required --input argument")
        }

        val inputJson = args[inputIndex + 1]
        val input = gson.fromJson(inputJson, CLIInput::class.java)

        val flushAt = input.config?.flushAt ?: 20
        val flushIntervalMs = input.config?.flushInterval ?: 10000L

        val flushLatch = CountDownLatch(1)
        val hasError = AtomicBoolean(false)
        var errorMessage: String? = null

        val analytics = Analytics.builder(input.writeKey)
            .endpoint(input.apiHost)
            .flushQueueSize(flushAt)
            .flushInterval(maxOf(flushIntervalMs, 1000L), TimeUnit.MILLISECONDS)
            .callback(object : Callback {
                override fun success(message: Message?) {
                    // Event sent successfully
                }

                override fun failure(message: Message?, throwable: Throwable?) {
                    hasError.set(true)
                    errorMessage = throwable?.message
                }
            })
            .build()

        // Process event sequences
        for (seq in input.sequences) {
            if (seq.delayMs > 0) {
                Thread.sleep(seq.delayMs)
            }

            for (event in seq.events) {
                sendEvent(analytics, event)
            }
        }

        // Flush and shutdown
        analytics.flush()
        analytics.shutdown()

        output = if (hasError.get()) {
            CLIOutput(success = false, error = errorMessage, sentBatches = 0)
        } else {
            CLIOutput(success = true, sentBatches = 1)
        }

    } catch (e: Exception) {
        output = CLIOutput(success = false, error = e.message ?: e.toString())
    }

    println(gson.toJson(output))
}

fun sendEvent(analytics: Analytics, event: Map<String, Any>) {
    val type = event["type"] as? String
        ?: throw IllegalArgumentException("Event missing 'type' field")

    val userId = event["userId"] as? String ?: ""
    val anonymousId = event["anonymousId"] as? String
    val messageId = event["messageId"] as? String
    val timestamp = event["timestamp"] as? String
    @Suppress("UNCHECKED_CAST")
    val traits = event["traits"] as? Map<String, Any> ?: emptyMap()
    @Suppress("UNCHECKED_CAST")
    val properties = event["properties"] as? Map<String, Any> ?: emptyMap()
    val eventName = event["event"] as? String
    val name = event["name"] as? String
    val category = event["category"] as? String
    val groupId = event["groupId"] as? String
    val previousId = event["previousId"] as? String
    @Suppress("UNCHECKED_CAST")
    val context = event["context"] as? Map<String, Any>
    @Suppress("UNCHECKED_CAST")
    val integrations = event["integrations"] as? Map<String, Any>

    val messageBuilder: MessageBuilder<*, *> = when (type) {
        "identify" -> {
            IdentifyMessage.builder().apply {
                traits(traits)
            }
        }
        "track" -> {
            TrackMessage.builder(eventName ?: "Unknown Event").apply {
                properties(properties)
            }
        }
        "page" -> {
            PageMessage.builder(name ?: "Unknown Page").apply {
                properties(properties)
            }
        }
        "screen" -> {
            ScreenMessage.builder(name ?: "Unknown Screen").apply {
                properties(properties)
            }
        }
        "alias" -> {
            AliasMessage.builder(previousId ?: "")
        }
        "group" -> {
            GroupMessage.builder(groupId ?: "").apply {
                traits(traits)
            }
        }
        else -> throw IllegalArgumentException("Unknown event type: $type")
    }

    if (userId.isNotEmpty()) {
        messageBuilder.userId(userId)
    }
    if (anonymousId != null) {
        messageBuilder.anonymousId(anonymousId)
    }
    if (messageId != null) {
        messageBuilder.messageId(messageId)
    }
    if (timestamp != null) {
        messageBuilder.timestamp(parseTimestamp(timestamp))
    }
    if (context != null) {
        messageBuilder.context(context)
    }
    if (integrations != null) {
        for ((key, value) in integrations) {
            when (value) {
                is Boolean -> messageBuilder.enableIntegration(key, value)
                is Map<*, *> -> @Suppress("UNCHECKED_CAST")
                    messageBuilder.integrationOptions(key, value as Map<String, Any>)
            }
        }
    }

    analytics.enqueue(messageBuilder)
}

private fun parseTimestamp(iso: String): Date {
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return try {
        format.parse(iso)!!
    } catch (_: Exception) {
        // Fallback: try without millis
        val fallback = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        fallback.timeZone = TimeZone.getTimeZone("UTC")
        fallback.parse(iso)!!
    }
}
