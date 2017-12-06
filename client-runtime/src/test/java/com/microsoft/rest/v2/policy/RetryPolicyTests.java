/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.policy;


import com.microsoft.rest.v2.http.*;
import org.junit.Assert;
import org.junit.Test;
import rx.Single;

public class RetryPolicyTests {
    @Test
    public void exponentialRetryEndOn501() throws Exception {
        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {
                // Send 408, 500, 502, all retried, with a 501 ending
                private final int[] codes = new int[]{408, 500, 502, 501};
                private int count = 0;

                public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
                    return Single.<HttpResponse>just(new MockHttpResponse(codes[count++]));
                }
            },
            new RetryPolicy.Factory(3));

        HttpResponse response = pipeline.sendRequestAsync(
                new HttpRequest(
                        "exponentialRetryEndOn501",
                        "GET",
                        "http://localhost/")).toBlocking().value();

        Assert.assertEquals(501, response.statusCode());
    }

    @Test
    public void exponentialRetryMax() throws Exception {
        final int maxRetries = 5;

        HttpPipeline pipeline = HttpPipeline.build(
            new MockHttpClient() {
                int count = -1;

                public Single<HttpResponse> sendRequestAsync(HttpRequest request) {
                    Assert.assertTrue(count++ < maxRetries);
                    return Single.<HttpResponse>just(new MockHttpResponse(500));
                }
            },
            new RetryPolicy.Factory(maxRetries));

        HttpResponse response = pipeline.sendRequestAsync(
                new HttpRequest(
                        "exponentialRetryMax",
                        "GET",
                        "http://localhost/")).toBlocking().value();

        Assert.assertEquals(500, response.statusCode());
    }
}