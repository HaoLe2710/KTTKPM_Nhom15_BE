package fit.iuh.kttkpm_nhom15_be.reviews.application.events;

import fit.iuh.kttkpm_nhom15_be.reviews.domain.models.Review;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ReviewCreatedEvent extends ApplicationEvent {
    private final Review review;

    public ReviewCreatedEvent(Object source, Review review) {
        super(source);
        this.review = review;
    }
}
