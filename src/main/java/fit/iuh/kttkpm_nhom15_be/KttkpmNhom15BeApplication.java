package fit.iuh.kttkpm_nhom15_be;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class KttkpmNhom15BeApplication {
    @PostConstruct
    public void init() {
        // Ép toàn bộ App chạy giờ UTC để khớp với PostgreSQL (Docker)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }
    public static void main(String[] args) {
        SpringApplication.run(KttkpmNhom15BeApplication.class, args);
    }

}

