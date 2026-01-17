package com.example.bankcards.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Bank Card Management Service API",
                version = "1.0",
                description = """
                        Backend service for managing bank cards, transfers between own cards, and card block requests.

                        ## Error format
                        All errors use the same JSON shape:
                        ```json
                        { "error": "SOME_ERROR", "message": "Human readable message", "timestamp": "2026-01-17T10:00:00Z", "path": "/api/..." }
                        ```

                        ## Common error codes
                        - `VALIDATION_ERROR` (400) — bean validation failed
                        - `BAD_REQUEST` (400) — malformed JSON / type mismatch / etc.
                        - `UNAUTHORIZED` (401) — missing/invalid JWT or login failed
                        - `FORBIDDEN` (403) — access denied by role
                        - `NOT_FOUND` (404) — entity not found
                        - `CONFLICT` (409) — business rule violation / insufficient funds / already pending request, etc.
                        - `INTERNAL_ERROR` (500) — unexpected server error
                        """
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    /**
     * Adds common error responses to all operations automatically.
     * Keeps Swagger UI informative without duplicating annotations in each controller.
     */
    @Bean
    public OpenApiCustomizer commonErrorResponsesCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(op -> {
                    addErrorResponse(op.getResponses(), "400", "Bad Request");
                    addErrorResponse(op.getResponses(), "401", "Unauthorized");
                    addErrorResponse(op.getResponses(), "403", "Forbidden");
                    addErrorResponse(op.getResponses(), "404", "Not Found");
                    addErrorResponse(op.getResponses(), "409", "Conflict");
                    addErrorResponse(op.getResponses(), "500", "Internal Server Error");
                })
        );
    }

    private void addErrorResponse(io.swagger.v3.oas.models.responses.ApiResponses responses,
                                  String code,
                                  String description) {
        if (responses.containsKey(code)) return;

        ApiResponse apiResponse = new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                ));

        responses.addApiResponse(code, apiResponse);
    }
}
