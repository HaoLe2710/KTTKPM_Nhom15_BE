package fit.iuh.kttkpm_nhom15_be.users.application.usecases;

import fit.iuh.kttkpm_nhom15_be.users.application.dto.UserResponse;
import fit.iuh.kttkpm_nhom15_be.users.domain.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUsersUseCase {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<UserResponse> execute(String keyword, int page, int size) {
        // searchUsers native SQL already orders by created_at DESC.
        // Avoid passing Sort to Pageable to prevent Spring from appending camelCase column names to native SQL.
        Pageable pageable = PageRequest.of(page, size);

        return userRepository.findAll(keyword, pageable)
                .map(UserResponse::fromDomain);
    }
}
