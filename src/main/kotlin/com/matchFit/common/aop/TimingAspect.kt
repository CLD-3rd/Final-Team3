package com.matchFit.common.aop

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.stereotype.Component

@Aspect
@Component
class TimingAspect(private val meterRegistry: MeterRegistry) {
    @Around("execution(public * com.matchFit..service..*(..))")
    @Throws(Throwable::class)
    fun timeServiceMethods(pjp: ProceedingJoinPoint): Any? {
        val signature = pjp.signature as MethodSignature
        val className = signature.declaringType.simpleName
        val methodName = signature.name

        val sample = Timer.start(meterRegistry)
        try {
            return pjp.proceed()
        } finally {
            val timer = Timer.builder("method.execution")
                .description("Execution time of methods (AOP)")
                .tags("class", className, "method", methodName)
                .publishPercentileHistogram()
                .register(meterRegistry)
            sample.stop(timer)
        }
    }
}
