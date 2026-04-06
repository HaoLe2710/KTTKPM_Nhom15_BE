package fit.iuh.kttkpm_nhom15_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableDiscoveryClient
public class KttkpmNhom15BeApplication {

    public static void main(String[] args) {
        SpringApplication.run(KttkpmNhom15BeApplication.class, args);
    }

}
