package store.purchase.infrastructure.config;

import jakarta.enterprise.context.ApplicationScoped; // Remplacera @Service ou @Component de Spring
import jakarta.enterprise.inject.Produces; // Pour produire des beans
import jakarta.inject.Inject; // Remplacera @Autowired de Spring
import org.jboss.logging.Logger; // Pour la journalisation dans Quarkus

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Ce service charge les certificats au démarrage et les met à disposition via une méthode Producer.
 */
@ApplicationScoped // Le bean est créé une fois pour l'application
public class CertificateProducer {

    private static final Logger LOG = Logger.getLogger(CertificateProducer.class);

    @Inject
    AppStoreVerifierConfig certificateConfig; // Injecte l'interface de configuration générée par Quarkus

    private Set<InputStream> loadedCertificates;

    // La méthode annotée avec @Produces sera appelée par CDI pour créer le bean Set<Certificate>
    // au démarrage de l'application ou lors de la première injection.
    @Produces
    @ApplicationScoped // Le bean produit sera également ApplicationScoped
    public Set<InputStream> produceLoadedCertificates() {
        if (loadedCertificates == null) {
            LOG.info("Chargement des certificats au démarrage de l'application Quarkus...");
            loadedCertificates = new HashSet<>();

            for (String path : certificateConfig.getCertificatePaths()) {
                try (InputStream is = getCertificateInputStream(path)) {
                    if (is != null) {
                        loadedCertificates.add(is);
                    } else {
                        LOG.warnf("  - Impossible d'ouvrir le flux pour le certificat %s", path);
                    }
                } catch (IOException e) {
                    LOG.errorf(e, "Erreur d'IO lors du chargement du certificat depuis %s", path);
                }
            }
            LOG.infof("Total de %d certificats chargés.", loadedCertificates.size());
        }
        return Collections.unmodifiableSet(loadedCertificates);
    }

    /**
     * Méthode utilitaire pour obtenir un InputStream pour un chemin de certificat donné.
     * Utilise le ClassLoader pour charger les ressources du classpath.
     * @param path Le chemin du certificat.
     * @return Un InputStream pour le certificat, ou null si la ressource n'est pas trouvée.
     * @throws IOException Si une erreur d'IO se produit.
     */
    private InputStream getCertificateInputStream(String path) throws IOException {
        // Quarkus cherche par défaut les ressources dans le classpath
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        if (is == null) {
            LOG.warnf("Ressource de certificat non trouvée dans le classpath: %s. Tentative via FileInputStream (peut échouer pour les JAR).", path);
            // Si non trouvé dans le classpath, tente en tant que fichier système (utile pour le dev local)
            // Mais soyez prudent en production avec des JARs, cela ne fonctionnera pas pour les ressources internes.
            try {
                return new FileInputStream(path);
            } catch (IOException e) {
                LOG.errorf(e, "Échec de l'ouverture de %s via FileInputStream.", path);
                return null;
            }
        }
        return is;
    }

    public Set<InputStream> getLoadedCertificates() {
        if (loadedCertificates == null) {
            produceLoadedCertificates();
        }
        return Collections.unmodifiableSet(loadedCertificates);
    }
}