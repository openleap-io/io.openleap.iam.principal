package io.openleap.iam.principal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "io.openleap.iam.principal",
    "io.openleap.starter.core"
})
@EntityScan(basePackages = {
        "io.openleap.iam.principal",
        "io.openleap.starter.core"
})
@EnableJpaRepositories(basePackages = {
        "io.openleap.iam.principal",
        "io.openleap.starter.core"
})
public class PrincipalServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PrincipalServiceApplication.class, args);
    }
}