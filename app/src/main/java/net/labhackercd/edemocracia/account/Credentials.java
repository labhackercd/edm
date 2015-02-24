package net.labhackercd.edemocracia.account;

public interface Credentials {
    public String getEmailAddress();
    public String getPassword();

    public interface Provider {
        public Credentials getCredentials();
    }
}
