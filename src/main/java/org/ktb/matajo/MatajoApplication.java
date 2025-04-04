package org.ktb.matajo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MatajoApplication {

  public static void main(String[] args) {
    SpringApplication.run(MatajoApplication.class, args);
  }
}
