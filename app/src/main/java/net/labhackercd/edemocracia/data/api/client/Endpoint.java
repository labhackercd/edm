package net.labhackercd.edemocracia.data.api.client;

/** An API endpoint. */
public abstract class Endpoint {

    /** Create an endpoint with the provided {@code url}. */
    public static Endpoint createFixed(final String url) {
        if (url == null) {
            throw new IllegalStateException("url == null");
        }
        return new Endpoint() {
            @Override public String url() {
                return url;
            }
        };
    }

    /**
     * The base URL.
     * <p>
     * Consumers will call this method every time they need to create a request
     * allowing values to change over time.
     */
    public abstract String url();
}
