package io.aime.plugins.protocolhttpclient;

// Log4j
import org.apache.log4j.Logger;

// Net
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

// Security
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class DummyX509TrustManager implements X509TrustManager {

    private static final Logger LOG = Logger.getLogger(DummyX509TrustManager.class.getName());
    private X509TrustManager standardTrustManager = null;

    /**
     * Constructor for DummyX509TrustManager.
     *
     * @param keystore
     *
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     */
    public DummyX509TrustManager(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        super();
        String algo = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory factory = TrustManagerFactory.getInstance(algo);
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();

        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException(algo + ": Trust Manager not supported.");
        }

        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    public boolean isClientTrusted(X509Certificate[] certificates) {
        return true;
    }

    public boolean isServerTrusted(X509Certificate[] certificates) {
        return true;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
    }
}
