package sample;

import java.io.IOException;
import okhttp3.*;

/** This interceptor overrides the HTTP client URL */
final class RewriteUrlInterceptor implements Interceptor {
  @Override
  public Response intercept(Interceptor.Chain chain) throws IOException {
    Request originalRequest = chain.request();
    Request modifiedUrlRequest = originalRequest.newBuilder().url("proxy-url").build();
    return chain.proceed(modifiedUrlRequest);
  }
}
