package fit.iuh.kttkpm_nhom15_be.catalog.presentation.controllers.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Catalog")
@RestController
@RequestMapping("/api/v1/admin/catalog")
public class AdminCatalogDefaultsController {

  @GetMapping("/default-options")
  public ResponseEntity<Map<String, Object>> getDefaultOptions() {
    return ResponseEntity.ok(Map.of(
      "containerType", List.of("Chai", "Lo", "Tuyp", "Hu", "Goi", "Hop"),
      "volume", List.of("5ml", "10ml", "15ml", "20ml", "30ml", "40ml", "50ml", "75ml", "100ml", "120ml", "150ml", "200ml", "236ml", "250ml", "300ml", "473ml", "500ml", "1000ml"),
      "weight", List.of("5g", "10g", "15g", "20g", "30g", "50g", "100g", "200g", "500g"),
      "usageTime", List.of("Sang", "Toi", "Sang/Toi"),
      "skinType", List.of("Da dau", "Da kho", "Da hon hop", "Da nhay cam", "Da thuong")
    ));
  }
}

