package cli

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.segment.analytics.Analytics
import com.segment.analytics.Callback
import com.segment.analytics.Log
import com.segment.analytics.messages.*
import org.docopt.Docopt
import java.util.concurrent.Phaser

val usage = """
Analytics Java CLI

Usage:
  analytics --writeKey=<writeKey> --type=<type> --userId=<userId> [--event=<event>] [--properties=<properties>] [--name=<name>] [--traits=<traits>] [--groupId=<groupId>] [--previousId=<previousId>] [--anonymousId=<anonymousId>] [--integrations=<integrations>] [--context=<context>]


  analytics -h | --help
  analytics --version

Options:
  -h --help     Show this screen.
  --version     Show version.
"""

private val GSON = Gson()

fun main(rawArgs: Array<String>) {
    val args = Docopt(usage).parse(rawArgs.toList())

    val messageBuilder: MessageBuilder<*, *>

    val eventType = args["--type"]

    when (eventType) {
        "track" -> {
            messageBuilder = TrackMessage.builder(args["--event"] as String)
            val properties = args["--properties"]
            if (properties != null) {
                messageBuilder.properties(parseJson(properties as String))
            }
        }
        "page" -> {
            messageBuilder = PageMessage.builder(args["--name"] as String)
            val properties = args["--properties"]
            if (properties != null) {
                messageBuilder.properties(parseJson(properties as String))
            }
        }
        "screen" -> {
            messageBuilder = ScreenMessage.builder(args["--name"] as String)
            val properties = args["--properties"]
            if (properties != null) {
                messageBuilder.properties(parseJson(properties as String))
            }
        }
        "identify" -> {
            messageBuilder = IdentifyMessage.builder()
            val traits = args["--traits"]
            if (traits != null) {
                messageBuilder.traits(parseJson(traits as String))
            }
        }
        "alias" -> {
            messageBuilder = AliasMessage.builder(args["--previousId"] as String)
        }
        "group" -> {
            messageBuilder = GroupMessage.builder(args["--groupId"] as String)
            val traits = args["--traits"]
            if (traits != null) {
                messageBuilder.traits(parseJson(traits as String))
            }
        }
        else -> {
            throw AssertionError("unknown command")
        }
    }

    val userId = args["--userId"]
    if (userId != null) {
        messageBuilder.userId(userId as String)
    }

    val anonymousId = args["--anonymousId"]
    if (anonymousId != null) {
        messageBuilder.anonymousId(anonymousId as String)
    }

    val integrations = args["--integrations"]
    if (integrations != null) {
        val integrationsMap = parseJson(integrations as String)
        for ((name, options) in integrationsMap) {
            messageBuilder.integrationOptions(name, options as Map<String, *>)
        }
    }

    val context = args["--context"]
    if (context != null) {
        val contextMap = parseJson(context as String)
        for ((name, options) in contextMap) {
            messageBuilder.context(contextMap)
        }
    }

    val writeKey = args["--writeKey"] as String

    val phaser = Phaser(1)

    val analytics = Analytics.builder(writeKey)
            .flushQueueSize(1)
            .callback(object : Callback {
                override fun success(message: Message?) {
                    phaser.arrive()
                }

                override fun failure(message: Message?, throwable: Throwable?) {
                    throw throwable!!
                }
            })
            .build()

    try {
        phaser.register()
        analytics.enqueue(messageBuilder)
        phaser.arriveAndAwaitAdvance();
    } finally {
        analytics.shutdown()
    }
}

fun parseJson(k: String): Map<String, *> {
    val collectionType = object : TypeToken<Map<String, Any>>() {}.getType()
    return GSON.fromJson(k, collectionType)
}
