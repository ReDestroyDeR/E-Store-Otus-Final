package ru.red.notificationservice.aop;

import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Log4j2
@Aspect
@Component
public class ControllerLoggingAspect {
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void isController() {
    }

    @Pointcut("execution(reactor.core.publisher.Mono *(*))")
    public void returnsMono() {
    }

    @Pointcut("execution(reactor.core.publisher.Flux *(*))")
    public void returnsFlux() {
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Around("isController() && returnsMono()")
    public Object logMono(ProceedingJoinPoint pjp) throws Throwable {
        log.info("Calling {} with args {}", pjp.getSignature().toShortString(), Arrays.toString(pjp.getArgs()));
        return ((Mono<?>) pjp.proceed())
                .doOnError(e -> log.info("Failed invoking target {} {} {}",
                        pjp.getSignature().toShortString(),
                        Arrays.toString(pjp.getArgs()),
                        e.getMessage()));
    }

    @SuppressWarnings("ReactiveStreamsUnusedPublisher")
    @Around("isController() && returnsFlux()")
    public Object logFlux(ProceedingJoinPoint pjp) throws Throwable {
        log.info("Calling {} with args {}", pjp.getSignature().toShortString(), Arrays.toString(pjp.getArgs()));
        return ((Flux<?>) pjp.proceed())
                .doOnError(e -> log.info("Failed invoking target {} {} {}",
                        pjp.getSignature().toShortString(),
                        Arrays.toString(pjp.getArgs()),
                        e.getMessage()));
    }
}
