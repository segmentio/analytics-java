package com.segment.analytics.http;

import com.segment.analytics.messages.Batch;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * REST interface for the Segment API.
 */
public interface SegmentService {
    @POST("/v1/import")
    Call<UploadResponse> upload(@Body Batch batch);
}
