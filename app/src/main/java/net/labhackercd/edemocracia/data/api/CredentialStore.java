package net.labhackercd.edemocracia.data.api;

public interface CredentialStore {
    public String get();
    public void set(String credential);
    public void clear();
}
