package com.segment.analytics.http;

import com.segment.analytics.messages.Batch;
import retrofit.http.Body;
import retrofit.http.EncodedPath;
import retrofit.http.POST;
import retrofit.http.Path;

/** REST interface for the Segment API. */
public interface SegmentService {

  // I suggest to mark it as deprecated.
  @POST("/v1/import")
  UploadResponse upload(@Body Batch batch);

  @POST("/{path}")
  UploadResponse upload(@Body Batch batch, @EncodedPath("path") String path);
}
