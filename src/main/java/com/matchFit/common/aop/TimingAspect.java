package com.matchFit.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

@Aspect
@Component
public class TimingAspect {

    private final MeterRegistry meterRegistry;

    public TimingAspect(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    // pointcut: 서비스 패키지의 public 메서드만 타이밍 (패키지명은 네 프로젝트에 맞게 변경)
    @Around("execution(public * com.matchFit..service..*(..))")
    public Object timeServiceMethods(ProceedingJoinPoint pjp) throws Throwable {
    	System.out.println("[TimingAspect] Entering method: " + pjp.getSignature());
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        String className = signature.getDeclaringType().getSimpleName();
        String methodName = signature.getName();

        // 샘플 시작
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // 실제 메서드 실행
            return pjp.proceed();
        } finally {
            // Timer 생성 및 태그 추가 (퍼센타일 히스토그램은 레지스트리/설정에 따라 publish 필요)
            Timer timer = Timer.builder("method.execution")
                    .description("Execution time of methods (AOP)")
                    .tags("class", className, "method", methodName)
                    .register(meterRegistry);

            // 샘플을 해당 Timer에 기록
            sample.stop(timer);
        }
    }
}