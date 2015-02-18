package net.labhackercd.edemocracia.ui;

import android.os.AsyncTask;

import de.greenrobot.event.EventBus;

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

    public static class ResultEvent<T> {
        private final T result;
        private final Object tag;
        private final Throwable throwable;

        public ResultEvent(T result, Object tag) {
            this(result, null, tag);
        }

        public ResultEvent(Throwable throwable, Object tag) {
            this(null, throwable, tag);
        }

        private ResultEvent(T result, Throwable throwable, Object tag) {
            this.tag = tag;
            this.result = result;
            this.throwable = throwable;
        }

        public T getResult() {
            return result;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public Object getTag() {
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
            eventBus.postSticky(new ResultEvent<T>(e, tag));
            return null;
        }
        eventBus.postSticky(new ResultEvent<>(result, tag));
        return null;
    }
}
