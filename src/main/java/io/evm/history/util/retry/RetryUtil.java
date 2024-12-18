package io.evm.history.util.retry;

import io.evm.history.config.core.RetryConfig;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import lombok.experimental.UtilityClass;
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
import java.util.function.Supplier;

@UtilityClass
public class RetryUtil {

    public Uni<BatchResponse> retryBatch(Logger log, String logMethod, RetryConfig config, Supplier<CompletableFuture<BatchResponse>> batchFuture) {
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

    public <T> Uni<T> retry(Logger log, String logMethod, RetryConfig config, Uni<T> uni, Predicate<? super Throwable> retry) {
        AtomicInteger i = new AtomicInteger();
        return uni.onFailure(e -> {
                    if (retry.test(e)) {
                        if (log.isDebugEnabled()) {
                            log.errorf(e, "Retry:[%s/%s] [%s] exception.", i.incrementAndGet(), config.atMost(), logMethod);
                        } else {
                            log.errorf("Retry:[%s/%s] [%s] exception. %s: %s", i.incrementAndGet(), config.atMost(), logMethod, e.getClass()
                                    .getSimpleName(), e.getMessage());
                        }
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
