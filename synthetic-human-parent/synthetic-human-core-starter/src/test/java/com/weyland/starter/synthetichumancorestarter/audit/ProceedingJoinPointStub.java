package com.weyland.starter.synthetichumancorestarter.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.SourceLocation;
import org.aspectj.runtime.internal.AroundClosure;

public class ProceedingJoinPointStub implements ProceedingJoinPoint {
    private final Object result;

    public ProceedingJoinPointStub(Object result) {
        this.result = result;
    }

    @Override
    public Object proceed() throws Throwable {
        if (result instanceof Throwable) throw (Throwable) result;
        return result;
    }

    @Override
    public Object proceed(Object[] args) throws Throwable {
        return proceed();
    }

    @Override
    public String toShortString() {
        return "testMethod()";
    }

    @Override
    public String toLongString() {
        return "public java.lang.String testMethod()";
    }

    @Override
    public Object getThis() {
        return this;
    }

    @Override
    public Object getTarget() {
        return this;
    }

    @Override
    public Object[] getArgs() {
        return new Object[]{"arg1", 42};
    }

    @Override
    public Signature getSignature() {
        return new Signature() {
            @Override
            public String toShortString() {
                return "testMethod()";
            }

            @Override
            public String toLongString() {
                return "public java.lang.String testMethod()";
            }

            @Override
            public String getName() {
                return "testMethod";
            }

            @Override
            public int getModifiers() {
                return 1;
            }

            @Override
            public Class getDeclaringType() {
                return ProceedingJoinPointStub.class;
            }

            @Override
            public String getDeclaringTypeName() {
                return ProceedingJoinPointStub.class.getName();
            }
        };
    }
    
    @Override
    public SourceLocation getSourceLocation() {
        return null;
    }

    @Override
    public String getKind() {
        return "method-execution";
    }

    @Override
    public StaticPart getStaticPart() {
        return null;
    }

    @Override
    public void set$AroundClosure(AroundClosure arc) {}

}