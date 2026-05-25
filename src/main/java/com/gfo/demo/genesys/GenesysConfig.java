package com.gfo.demo.genesys;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "genesys")
public class GenesysConfig {

    private Gcti gcti = new Gcti();
    private Composer composer = new Composer();
    private Logging logging = new Logging();

    public static class Gcti {
        private String serverUrl;
        private String username;
        private String password;

        // Getters and Setters
        public String getServerUrl() { return serverUrl; }
        public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class Composer {
        private String apiUrl;

        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }
    }

    public static class Logging {
        private String level;

        public String getLevel() { return level; }
        public void setLevel(String level) { this.level = level; }
    }

    public static class Sip {
        private String serverUrl;
        private int port = 5060;
        private String username;
        private String password;

        // Getters and Setters
        public String getServerUrl() { return serverUrl; }
        public void setServerUrl(String serverUrl) { this.serverUrl = serverUrl; }
        public int getPort() { return port; }
        public void setPort(int port) { this.port = port; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    private Sip sip = new Sip();

    public Sip getSip() { return sip; }
    public void setSip(Sip sip) { this.sip = sip; }

    // Convenience methods for SIP configuration
    public String getSipServerUrl() { return sip.getServerUrl(); }
    public int getSipPort() { return sip.getPort(); }
    public String getSipUsername() { return sip.getUsername(); }
    public String getSipPassword() { return sip.getPassword(); }

    public Gcti getGcti() { return gcti; }
    public void setGcti(Gcti gcti) { this.gcti = gcti; }

    public Composer getComposer() { return composer; }
    public void setComposer(Composer composer) { this.composer = composer; }

    public Logging getLogging() { return logging; }
    public void setLogging(Logging logging) { this.logging = logging; }
}