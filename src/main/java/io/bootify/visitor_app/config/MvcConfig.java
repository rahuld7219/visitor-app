package io.bootify.visitor_app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/vms/**") // route/map any request to this path to
                                                            // the below path (vms-->visiting management system)
                .addResourceLocations("file:/c:/static/vms/"); // static is the folder in which we are storing
                                                                // static files like images, etc., as we are accessing
                                                                // file system so put file:/ at starting,
                                                                // if the server system is linux then it is mapped to file:/static/vms
                                                                // and if the system is windows then it is mapped to file:/c:/static/vms
                                                                // (for other drive we can explicitly specify here
                                                                // like file:/d:/static/vms
        /*
          By above lines any request to /vms/** will be mapped/route to file:/static/vms at the backend
          and the resource at that path will be served to the client
        */

    }
}