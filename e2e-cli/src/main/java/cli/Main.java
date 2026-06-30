package cli;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.segment.analytics.Analytics;
import com.segment.analytics.Callback;
import com.segment.analytics.messages.*;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    private static final Gson gson = new Gson();
    private static final int DEFAULT_MAX_RETRIES = 1000;

    public static void main(String[] args) {
        Map<String, Object> output;
        try {
            output = run(args);
        } catch (Exception e) {
            output = errorOutput(e.getMessage());
        }
        System.out.println(gson.toJson(output));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> run(String[] args) throws Exception {
        int inputIndex = indexOf(args, "--input");
        if (inputIndex == -1 || inputIndex + 1 >= args.length) {
            throw new IllegalArgumentException("Missing required --input argument");
        }

        String inputJson = args[inputIndex + 1];
        Type mapType = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> input = gson.fromJson(inputJson, mapType);

        String writeKey = (String) input.get("writeKey");
        String apiHost = (String) input.get("apiHost");
        Map<String, Object> config = (Map<String, Object>) input.getOrDefault("config", Collections.emptyMap());
        List<Map<String, Object>> sequences = (List<Map<String, Object>>) input.get("sequences");

        int flushAt = intVal(config, "flushAt", 20);
        long flushIntervalMs = longVal(config, "flushInterval", 10000L);
        int maxRetries = intVal(config, "maxRetries", DEFAULT_MAX_RETRIES);

        AtomicBoolean hasError = new AtomicBoolean(false);
        AtomicReference<String> errorMessage = new AtomicReference<>();

        Analytics analytics = Analytics.builder(writeKey)
                .endpoint(apiHost)
                .flushQueueSize(flushAt)
                .flushInterval(Math.max(flushIntervalMs, 1000L), TimeUnit.MILLISECONDS)
                .retries(maxRetries)
                .callback(new Callback() {
                    @Override
                    public void success(Message message) {
                    }

                    @Override
                    public void failure(Message message, Throwable throwable) {
                        hasError.set(true);
                        errorMessage.set(throwable != null ? throwable.getMessage() : "unknown error");
                    }
                })
                .build();

        for (Map<String, Object> seq : sequences) {
            long delayMs = longVal(seq, "delayMs", 0L);
            if (delayMs > 0) {
                Thread.sleep(delayMs);
            }
            List<Map<String, Object>> events = (List<Map<String, Object>>) seq.get("events");
            if (events != null) {
                for (Map<String, Object> event : events) {
                    sendEvent(analytics, event);
                }
            }
        }

        analytics.flush();
        analytics.shutdown();

        if (hasError.get()) {
            return errorOutput(errorMessage.get());
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("sentBatches", 1);
        return result;
    }

    @SuppressWarnings("unchecked")
    private static void sendEvent(Analytics analytics, Map<String, Object> event) {
        String type = (String) event.get("type");
        if (type == null) {
            throw new IllegalArgumentException("Event missing 'type' field");
        }

        String userId = strVal(event, "userId", "");
        String anonymousId = (String) event.get("anonymousId");
        String messageId = (String) event.get("messageId");
        String timestamp = (String) event.get("timestamp");
        Map<String, Object> traits = (Map<String, Object>) event.getOrDefault("traits", Collections.emptyMap());
        Map<String, Object> properties = (Map<String, Object>) event.getOrDefault("properties", Collections.emptyMap());
        String eventName = (String) event.get("event");
        String name = (String) event.get("name");
        String groupId = (String) event.get("groupId");
        String previousId = (String) event.get("previousId");
        Map<String, Object> context = (Map<String, Object>) event.get("context");
        Map<String, Object> integrations = (Map<String, Object>) event.get("integrations");

        MessageBuilder<?, ?> builder;
        switch (type) {
            case "identify":
                builder = IdentifyMessage.builder().traits(traits);
                break;
            case "track":
                builder = TrackMessage.builder(eventName != null ? eventName : "Unknown Event").properties(properties);
                break;
            case "page":
                builder = PageMessage.builder(name != null ? name : "Unknown Page").properties(properties);
                break;
            case "screen":
                builder = ScreenMessage.builder(name != null ? name : "Unknown Screen").properties(properties);
                break;
            case "alias":
                builder = AliasMessage.builder(previousId != null ? previousId : "");
                break;
            case "group":
                builder = GroupMessage.builder(groupId != null ? groupId : "").traits(traits);
                break;
            default:
                throw new IllegalArgumentException("Unknown event type: " + type);
        }

        if (!userId.isEmpty()) {
            builder.userId(userId);
        }
        if (anonymousId != null) {
            builder.anonymousId(anonymousId);
        }
        if (messageId != null) {
            builder.messageId(messageId);
        }
        if (timestamp != null) {
            builder.timestamp(Date.from(Instant.parse(timestamp)));
        }
        if (context != null) {
            builder.context(context);
        }
        if (integrations != null) {
            for (Map.Entry<String, Object> entry : integrations.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof Boolean) {
                    builder.enableIntegration(entry.getKey(), (Boolean) value);
                } else if (value instanceof Map) {
                    builder.integrationOptions(entry.getKey(), (Map<String, ?>) value);
                }
            }
        }

        analytics.enqueue(builder);
    }

    private static Map<String, Object> errorOutput(String error) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", false);
        result.put("error", error);
        result.put("sentBatches", 0);
        return result;
    }

    private static int indexOf(String[] arr, String target) {
        for (int i = 0; i < arr.length; i++) {
            if (target.equals(arr[i])) return i;
        }
        return -1;
    }

    private static int intVal(Map<String, Object> map, String key, int defaultVal) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        return defaultVal;
    }

    private static long longVal(Map<String, Object> map, String key, long defaultVal) {
        Object v = map.get(key);
        if (v instanceof Number) return ((Number) v).longValue();
        return defaultVal;
    }

    private static String strVal(Map<String, Object> map, String key, String defaultVal) {
        Object v = map.get(key);
        if (v instanceof String) return (String) v;
        return defaultVal;
    }
}
