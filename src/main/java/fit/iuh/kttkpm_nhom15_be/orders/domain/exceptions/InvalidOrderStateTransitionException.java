package fit.iuh.kttkpm_nhom15_be.orders.domain.exceptions;

public class InvalidOrderStateTransitionException extends RuntimeException {

  public InvalidOrderStateTransitionException(String currentState, String attemptedAction) {
    super("Không thể thực hiện '" + attemptedAction + "' khi đơn hàng đang ở trạng thái '" + currentState + "'");
  }
}
