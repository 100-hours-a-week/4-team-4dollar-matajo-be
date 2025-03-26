package org.ktb.matajo.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    Info info =
        new Info()
            .title("MATAJO API 문서")
            .version("v1.0.0")
            .contact(
                new Contact()
                    .name("마타조 github")
                    .url("https://github.com/100-hours-a-week/4-team-4dollar-matajo-fe.git"));

    Server localServer = new Server();
    localServer.setUrl("http://localhost:8080");
    localServer.setDescription("개발 서버");

    return new OpenAPI().info(info).servers(Arrays.asList(localServer));
  }
}
