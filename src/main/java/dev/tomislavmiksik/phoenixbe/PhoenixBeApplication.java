package dev.tomislavmiksik.phoenixbe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class PhoenixBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhoenixBeApplication.class, args);
    }

}
