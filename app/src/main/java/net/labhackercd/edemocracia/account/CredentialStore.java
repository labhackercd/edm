package net.labhackercd.edemocracia.account;

public interface CredentialStore {
    public String get();
    public void set(String credential);
    public void clear();
}
