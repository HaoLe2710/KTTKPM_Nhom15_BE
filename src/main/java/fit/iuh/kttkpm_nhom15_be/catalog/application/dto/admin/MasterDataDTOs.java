package fit.iuh.kttkpm_nhom15_be.catalog.application.dto.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class MasterDataDTOs {

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductTypeRequest {
        @NotEmpty(message = "Code is required")
        private String code;
        @NotEmpty(message = "Name is required")
        private String name;
        private Boolean isActive;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ProductTypeResponse {
        private String id;
        private String code;
        private String name;
        private Boolean isActive;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OptionRequest {
        @NotEmpty(message = "Code is required")
        private String code;
        @NotEmpty(message = "Name is required")
        private String name;
        private List<String> values;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OptionResponse {
        private String id;
        private String code;
        private String name;
        private List<OptionValueResponse> values;
    }

    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class OptionValueResponse {
        private String id;
        private String value;
    }
}
