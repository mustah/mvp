package com.elvaco.mvp.configuration.config;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Profile;

@Slf4j
@Profile("aop")
@Aspect
@EnableAspectJAutoProxy
@Configuration
class AopConfig {

  @Pointcut("execution(* com.elvaco.mvp.database.repository.access..*(..))")
  void access() {}

  @Pointcut("execution(* com.elvaco.mvp.database.repository.jpa..*(..))")
  void jpa() {}

  @Pointcut("execution(* com.elvaco.mvp.consumers.rabbitmq.message..*(..))")
  void messages() {}

  @Pointcut("access() || messages() || jpa()")
  public void allPoints() {}

  @Around("allPoints()")
  Object profile(ProceedingJoinPoint pjp) throws Throwable {
    long start = System.nanoTime();
    Object output = pjp.proceed();
    long elapsedTime = System.nanoTime() - start;
    if (TimeUnit.NANOSECONDS.toMillis(elapsedTime) >= 50.0) {
      StringBuilder builder = new StringBuilder();
      if (pjp.getArgs() != null) {
        for (Object o : pjp.getArgs()) {
          builder.append(o).append(" , ");
        }
      }
      log.info(
        "Timer: {} ms {}.{}({})",
        (TimeUnit.NANOSECONDS.toMillis(elapsedTime)),
        pjp.getTarget().getClass().getName(),
        pjp.getSignature().getName(),
        builder
      );
    }
    return output;
  }
}
