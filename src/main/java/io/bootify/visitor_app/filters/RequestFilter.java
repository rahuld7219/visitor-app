package io.bootify.visitor_app.filters;

import org.apache.catalina.connector.RequestFacade;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpFilter;
import java.io.IOException;


@Component
public class RequestFilter extends HttpFilter { // , any class extending HttpFilter overrides the doFilter() method
                                                // and it will be executed before and after the controller as defined in the code
    private static final String REQUEST_ID = "requestId";
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException, ServletException, IOException {
//        MDC.put(REQUEST_ID,((RequestFacade) servletRequest).getHeader(REQUEST_ID));
        filterChain.doFilter(servletRequest,servletResponse); // pass the request to the next filter(if any)
                                                                // or to the controller(if this filter is the last in the chain)
                                                                // with the request and response to pass along the chain
        MDC.clear(); // clearing the MDC, this line runs on returning at last from the chain
                        // i.e., on returning at last from the controller(just before sending the response to the client),
    }
}

