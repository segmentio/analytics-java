package com.segment.analytics.http;

import com.segment.analytics.messages.Batch;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

/** REST interface for the Segment API. */
public interface SegmentService {
  @POST
  Call<UploadResponse> upload(@Url HttpUrl uploadUrl, @Body Batch batch);
}
