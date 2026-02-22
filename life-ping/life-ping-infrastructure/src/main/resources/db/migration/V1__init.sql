
CREATE TABLE emergency_contacts
(
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Utilise UUID comme type et génère un UUID par défaut
    name         VARCHAR(255) NOT NULL,
    phone        VARCHAR(20)  NOT NULL,                      -- Taille arbitraire pour le numéro de téléphone
    email        VARCHAR(255) NOT NULL,
    relationship VARCHAR(100) NOT NULL,
    is_active    BOOLEAN          DEFAULT TRUE               -- Valeur par défaut TRUE, comme dans votre entité
);

INSERT INTO emergency_contacts (name, phone, email, relationship, is_active)
VALUES ('Alice Dupont', '0612345678', 'alice.dupont@example.com', 'Amie', TRUE),
       ('Bernard Martin', '0798765432', 'bernard.martin@example.com', 'Frère', TRUE),
       ('Catherine Petit', '0123456789', 'catherine.petit@example.com', 'Mère', TRUE),
       ('David Leroy', '0655443322', 'david.leroy@example.com', 'Collègue', FALSE), -- Exemple d'un contact inactif
       ('Émilie Dubois', '0711223344', 'emilie.dubois@example.com', 'Sœur', TRUE);