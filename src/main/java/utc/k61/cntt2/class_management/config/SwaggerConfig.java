//package utc.k61.cntt2.class_management.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//import static com.google.common.collect.Lists.newArrayList;
//
//@EnableSwagger2
//@Configuration
//public class SwaggerConfig implements WebMvcConfigurer {
//
////    @Bean
////    public SwaggerUiConfigParameters swaggerUiConfigParameters() {
////        return SwaggerUiConfigParametersBuilder.builder()
////                .withDefaultUrl("/swagger-ui/index.html")
////                .build();
////    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/swagger-ui/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/")
//                .resourceChain(false);
//    }
//    public static final String AUTHORIZATION_HEADER = "Authorization";
//
//    private ApiKey apiKey(){
//        return new ApiKey("JWT", AUTHORIZATION_HEADER, "header");
//    }
//
//    private ApiInfo apiInfo() {
//        return new ApiInfoBuilder()
//                .title("My Class API")
//                .description("API of Class Management service")
//                .version("1.0")
//                .build();
//    }
//
//    @Bean
//    public Docket api() {
//
//        return new Docket(DocumentationType.SWAGGER_2).select()
//                .apis(RequestHandlerSelectors.basePackage("utc.k61.cntt2.class_management.controller"))
//                .apis(RequestHandlerSelectors.any())
//                .paths(PathSelectors.any()).build()
//                .apiInfo(new ApiInfo("CRM Services", "A set of services for CRM", "1.0.0", null, null, null, null,
//                        new ArrayList<>()))
//                .securitySchemes(newArrayList(apiKey())).securityContexts(newArrayList(securityContext()));
//    }
//
//    private SecurityContext securityContext(){
//        return SecurityContext.builder().securityReferences(defaultAuth()).build();
//    }
//
//    private List<SecurityReference> defaultAuth(){
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        return List.of(new SecurityReference("JWT", authorizationScopes));
//    }
//}
//
