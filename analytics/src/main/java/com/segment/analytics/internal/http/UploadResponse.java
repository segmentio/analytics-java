package com.segment.analytics.internal.http;

import com.google.auto.value.AutoValue;
import com.segment.analytics.internal.gson.AutoGson;

@AutoValue @AutoGson public abstract class UploadResponse {
  public abstract boolean success();
}
