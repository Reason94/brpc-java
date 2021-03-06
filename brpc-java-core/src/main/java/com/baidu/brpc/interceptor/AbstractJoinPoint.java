package com.baidu.brpc.interceptor;

import java.util.List;

import org.apache.commons.lang3.Validate;

import com.baidu.brpc.Controller;
import com.baidu.brpc.protocol.Request;

import lombok.Getter;

/**
 * Abstract implementation of JoinPoint
 * @author Li Yuanxin(liyuanxin@baidu.com)
 */
@Getter
public abstract class AbstractJoinPoint implements JoinPoint {

    private int index = 0;
    protected Controller controller;
    protected Request request;

    public AbstractJoinPoint(Controller controller, Request request) {
        Validate.notNull(request, "request cannot be null");
        // controller config by user, may be null
        this.controller = controller;
        this.request = request;
    }

    @Override
    public Object proceed() throws Exception {
        List<Interceptor> interceptors = getInterceptors();
        if (interceptors != null && index < interceptors.size()) {
            // not the last interceptor, invoke the around process of interceptors sequentially
            Interceptor interceptor = interceptors.get(index++);
            try {
                return interceptor.aroundProcess(this);
            } finally {
                index--;
            }
        } else {
            // the last interceptor or none interceptors, invoke the intercepted method
            return internalProceed();
        }
    }

    @Override
    public Object proceed(Object[] args) throws Exception {
        Validate.notNull(args, "args array passed to proceed cannot be null");
        if (request.getArgs() != null && args.length != request.getArgs().length) {
            throw new IllegalArgumentException("expecting " + request.getArgs().length
                    + " args to proceed, but was passed " + args.length + " args");
        }
        request.setArgs(args);
        return proceed();
    }

    /**
     * get the interceptors from the server or client
     * @return interceptors
     */
    protected abstract List<Interceptor> getInterceptors();

    /**
     * invoke the intercepted method
     * @return invoke result
     * @throws Exception exception thrown in proceed
     */
    protected abstract Object internalProceed() throws Exception;
}
