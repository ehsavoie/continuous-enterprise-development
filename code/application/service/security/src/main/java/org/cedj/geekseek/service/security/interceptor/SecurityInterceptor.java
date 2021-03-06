package org.cedj.geekseek.service.security.interceptor;

import java.lang.reflect.Method;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.cedj.geekseek.domain.Current;
import org.cedj.geekseek.domain.user.model.User;
import org.cedj.geekseek.web.rest.core.annotation.Secured;

@Secured
@Interceptor
public class SecurityInterceptor {

    @Inject @Current
    private User user;

    @AroundInvoke
    public Object verify(InvocationContext ic) throws Exception {

        Method target = ic.getMethod();
        if(isStateChangingMethod(target)) {
            if(user != null) {
                return ic.proceed();
            }
            else {
                return Response.status(Status.UNAUTHORIZED).build();
            }
        }
        return ic.proceed();
    }

    private boolean isStateChangingMethod(Method target) {
        return target.isAnnotationPresent(PUT.class) ||
            target.isAnnotationPresent(POST.class) ||
            target.isAnnotationPresent(DELETE.class);
    }
}
