package net.labhackercd.edemocracia.task;

public abstract class Task implements com.squareup.tape.Task<Task.Callback> {
    @Override
    public void execute(Callback callback) {
        try {
            execute();
            callback.onCompleted();
        } catch (Throwable t) {
            callback.onError(t);
        }
    }

    protected abstract void execute() throws Throwable;

    public abstract boolean shouldRetry(Throwable error);

    public abstract void onCancel(Throwable error);

    public interface Callback {
        public void onCompleted();
        public void onError(Throwable error);
    }
}
