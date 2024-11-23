package io.evm.history.util.retry;

import io.evm.history.config.core.RetryConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import org.jboss.logging.Logger;
import org.web3j.protocol.core.BatchResponse;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.exceptions.ClientConnectionException;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class RetryUtil {

    public static Uni<BatchResponse> retryBatch(Logger log, String logMethod, RetryConfig config, CompletableFuture<BatchResponse> batchFuture) {
        return retry(log, logMethod, config, Uni.createFrom()
                // web3j http errors wrapped in ClientConnectionException
                .completionStage(batchFuture)
                .invoke(Unchecked.consumer(resp -> {
                    // add rate limit check
                    if (resp.getResponses() != null || resp.getResponses().isEmpty()) {
                        Optional<Response.Error> error = resp.getResponses()
                                .stream()
                                .filter(Response::hasError)
                                .map(Response::getError)
                                .findAny();
                        if (error.isPresent()) {
                            throw new IllegalStateException(error.get().getMessage());
                        }
                    }
                })), e -> e instanceof IOException || e instanceof IllegalStateException || e instanceof ClientConnectionException);
    }

    public static <T> Uni<T> retry(Logger log, String logMethod, RetryConfig config, Uni<T> uni, Predicate<? super Throwable> retry) {
        AtomicInteger i = new AtomicInteger();
        return uni.onFailure(e -> {
                    if (retry.test(e)) {
                        log.errorf(e, "Retry:[%s/%s] [%s] exception.", i.incrementAndGet(), config.atMost(), logMethod);
                        return true;
                    } else {
                        return false;
                    }
                })
                .retry()
                .withBackOff(Duration.ofMillis(config.initialBackoff()), Duration.ofMillis(config.maxBackoff()))
                .withJitter(config.jitter())
                .atMost(config.atMost());
    }
}
