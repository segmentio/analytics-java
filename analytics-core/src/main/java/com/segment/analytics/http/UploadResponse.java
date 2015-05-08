package com.segment.analytics.http;

import com.google.auto.value.AutoValue;
import com.segment.analytics.gson.AutoGson;

@AutoValue @AutoGson public abstract class UploadResponse {
  public abstract boolean success();
}
