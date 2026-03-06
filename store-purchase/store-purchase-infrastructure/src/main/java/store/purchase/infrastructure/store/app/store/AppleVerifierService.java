package store.purchase.infrastructure.store.app.store;

import com.apple.itunes.storekit.verification.SignedDataVerifier;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import store.purchase.infrastructure.config.AppStoreVerifierConfig;
import store.purchase.infrastructure.config.CertificateProducer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class AppleVerifierService {

    @Inject
    CertificateProducer certificateProducer;
    @Inject
    AppStoreVerifierConfig appStoreVerifierConfig;

    private final Map<String, SignedDataVerifier> cache = new ConcurrentHashMap<>();

    public SignedDataVerifier getVerifier(String bundleId, Long appAppleId) {

        return cache.computeIfAbsent(bundleId, b -> buildVerifier(bundleId, appAppleId));
    }

    private SignedDataVerifier buildVerifier(String bundleId, Long appAppleId) {

        try {

            return new SignedDataVerifier(
                    certificateProducer.produceLoadedCertificates(),
                    bundleId,
                    appAppleId,
                    appStoreVerifierConfig.parsedEnvironment(),
                    true
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to build SignedDataVerifier", e);
        }
    }
}