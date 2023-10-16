package org.udg.trackdev.spring.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;

import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;


@OpenAPIDefinition(info = @Info(title = "Trackdev API", version = "1.0.0",
        description = "Documentation for the Trackdev API. This API is used for the Trackdev project.<br> " +
                "The API is organized around REST. Our API has predictable resource-oriented URLs, accepts form-encoded" +
                " request bodies, returns JSON-encoded responses, and uses standard HTTP response codes, authentication," +
                " and verbs.<br> " +
                "To use the API you need to authenticate yourself with a JWT token. You can get a token by using the" +
                " **/auth/login** endpoint.<br> ",
        contact = @io.swagger.v3.oas.annotations.info.Contact(name = "Trackdev", email = "gerard.rovellat@gmail.com"),
        license = @io.swagger.v3.oas.annotations.info.License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html")))
@SecurityScheme(
        name = "bearerAuth",
        description = "Please insert JWT without Bearer prefix into field",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenApiCustomiser openApiCustomiser() {
        return openApi -> {
            Map<String, Schema> schemas = openApi.getComponents().getSchemas();
            openApi.getComponents().setSchemas(new TreeMap<>(schemas));
        };
    }


    @Bean
    public OpenApiCustomiser sortPathsAndTagsAlphabetically() {
        return openApi -> {
            Map<String, PathItem> paths = openApi.getPaths();
            Paths sortedPaths = new Paths();
            TreeMap<String, PathItem> sortedTree = new TreeMap<String, PathItem>(paths);

            Set<Map.Entry<String, PathItem>> pathItems = sortedTree.entrySet();
            Map<String, Map.Entry<String, PathItem>> distinctTagMap = new TreeMap<String, Map.Entry<String, PathItem>>();
            for (Map.Entry<String, PathItem> entry : pathItems) {
                PathItem pathItem = entry.getValue();
                Operation getOp = pathItem.getGet();
                if (getOp != null) {
                    String tag = getOp.getTags().get(0);
                    if (!distinctTagMap.containsKey(tag)) {
                        distinctTagMap.put(tag, entry);
                    }
                }
                Operation postOp = pathItem.getPost();
                if (postOp != null) {
                    String tag1 = postOp.getTags().get(0);
                    if (!distinctTagMap.containsKey(tag1)) {
                        distinctTagMap.put(tag1, entry);
                    }
                }

                Operation putOp = pathItem.getPut();
                if (putOp != null) {
                    String tag2 = putOp.getTags().get(0);
                    if (!distinctTagMap.containsKey(tag2)) {
                        distinctTagMap.put(tag2, entry);
                    }
                }
            }

            LinkedHashMap<String, PathItem> customOrderMap = new LinkedHashMap<String, PathItem>();
            for (Map.Entry<String, PathItem> entry : distinctTagMap.values()) {
                customOrderMap.put(entry.getKey(), entry.getValue());
            }
            for (Map.Entry<String, PathItem> entry : sortedTree.entrySet()) {
                customOrderMap.putIfAbsent(entry.getKey(), entry.getValue());
            }
            sortedPaths.putAll(customOrderMap);
            openApi.setPaths(sortedPaths);

        };
    }

}
