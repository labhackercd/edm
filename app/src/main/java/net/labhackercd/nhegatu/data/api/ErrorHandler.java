package net.labhackercd.nhegatu.data.api;

public interface ErrorHandler {
    public Throwable handleError(ServiceError error);

    ErrorHandler DEFAULT = new ErrorHandler() {
        @Override
        public Throwable handleError(ServiceError error) {
            return error;
        }
    };
}
