package fit.iuh.kttkpm_nhom15_be.promotions.application.pricing;

import fit.iuh.kttkpm_nhom15_be.promotions.domain.models.PromotionType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PromotionCalculatorFactory {
    private final Map<PromotionType, PromotionCalculator> strategies;

    public PromotionCalculatorFactory(List<PromotionCalculator> calculators) {
        this.strategies = calculators.stream()
            .collect(Collectors.toMap(PromotionCalculator::getSupportedType, calculator -> calculator));
    }

    public PromotionCalculator getCalculator(PromotionType type) {
        PromotionCalculator calculator = strategies.get(type);
        if (calculator == null) {
            throw new IllegalArgumentException("Unsupported promotion type: " + type);
        }
        return calculator;
    }
}
