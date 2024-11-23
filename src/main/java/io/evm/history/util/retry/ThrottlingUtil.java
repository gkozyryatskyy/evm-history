package io.evm.history.util.retry;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import lombok.experimental.UtilityClass;

import java.time.Duration;

@UtilityClass
public class ThrottlingUtil {

    public <T> Multi<T> throttling(Long throttling, Multi<T> retval) {
        if (throttling != null) {
            // throttle each block batch for not get relate limit
            return retval.call(() -> Uni.createFrom()
                    .nullItem()
                    .onItem()
                    .delayIt()
                    .by(Duration.ofMillis(throttling)));
        } else {
            return retval;
        }
    }
}
