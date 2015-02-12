package net.labhackercd.edemocracia.ui;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.util.ThrowableFailureEvent;

/**
 * TODO: Replace *tag* checking mechanism with something like a result class builder in such
 * a way that each unique user of this class would have its own non-generic result class type
 * and their results would stick in the event bus even if another user post its own results
 * later. This means SimpleRecyclerViewFragments would not need to always "refresh list on startup."
 *
 * @param <T>
 */
public class RefreshListTask<T> extends AsyncTask<Void, Void, Void> {
    public interface Task<T> {
        public T execute() throws Exception;
    }

    public static class SuccessEvent<T> {
        private final T result;
        private final Object tag;

        public SuccessEvent(T result, Object tag) {
            this.tag = tag;
            this.result = result;
        }

        public T getResult() {
            return result;
        }

        public Object getExecutionScope() {
            return tag;
        }
    }

    private final Object tag;
    private final Task<T> task;
    private final EventBus eventBus;

    public RefreshListTask(Object tag, EventBus eventBus, Task<T> task) {
        super();
        this.tag = tag;
        this.task = task;
        this.eventBus = eventBus;
    }

    @Override
    protected Void doInBackground(Void... params) {
        T result;
        try {
            result = task.execute();
        } catch (Exception e) {
            ThrowableFailureEvent failureEvent = new ThrowableFailureEvent(e);
            failureEvent.setExecutionScope(tag);
            eventBus.post(failureEvent);
            return null;
        }
        eventBus.postSticky(new SuccessEvent<>(result, tag));
        return null;
    }
}
