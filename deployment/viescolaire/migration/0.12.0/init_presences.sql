BEGIN;
-- Initialisation des tables de présences
-- Le fichier est découpé en deux parties:
-- - Initialisation des données globales à tous les établissements
-- - Procédure permettant l'Initialisation des données spécifiques à chaque établissement

-- TABLE: mailing_element_type
  TRUNCATE TABLE presences.mailing_element_type CASCADE;
  INSERT INTO presences.mailing_element_type(id, label) VALUES
    (1, 'Absences'),
    (2, 'Retards'),
    (3, 'Punition'),
    (4, 'Sanction');

  -- TABLE: mailing_type
  TRUNCATE TABLE presences.mailing_type CASCADE;
  INSERT INTO presences.mailing_type(id, label) VALUES
    (1, 'PDF'),
    (2, 'Email'),
    (3, 'SMS');

-- TABLE: etat_appel
  TRUNCATE TABLE presences.etat_appel CASCADE;
  INSERT INTO presences.etat_appel(id, libelle) VALUES
    (1, 'Init'),
    (2, 'En cours'),
    (3, 'Fait');

-- TABLE: type_evt
  TRUNCATE TABLE presences.type_evt CASCADE;
  INSERT INTO presences.type_evt(id, libelle) VALUES
    (1, 'Absence'),
    (2, 'Retard'),
    (3, 'Départ'),
    (4, 'Incident'),
    (5, 'Observation');

  -- Procedure to insert a new city
  CREATE OR REPLACE FUNCTION presences.activateStructure(structureId VARCHAR(36))
    RETURNS void AS $$
  BEGIN
    -- TABLE: etablissements_actifs
    INSERT INTO presences.etablissements_actifs(id_etablissement, actif) VALUES
      (structureId, TRUE );

    -- TABLE: categorie_motif_absence
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES
      (structureId, 'Non Justifiant' ),
      (structureId, 'Justifiant' ),
      (structureId, 'CATEGORIE ABSENCE' ),
      (structureId, 'TRANSPORT' ),
      (structureId, 'MEDICAL' ),
      (structureId, 'SANTE' ),
      (structureId, 'DEFAUT' ),
      (structureId, 'AUTRE' ),
      (structureId, 'EXCUSES' );

    -- TABLE: categorie_motif_appel
    -- INSERT INTO presences.categorie_motif_appel(id_etablissement, libelle) VALUES
    --   (structureId, 'MEDICAL' ),
    --   (id_etablissement, libelle) VALUES (structureId, 'TECHNIQUE' ),
    --   (id_etablissement, libelle) VALUES (structureId, 'CATEGORIE APPEL' );

    -- TABLE: motif_appel
    -- INSERT INTO presences.motif_appel(id_etablissement, libelle, justifiant) VALUES
    --    (structureId, 'Réseau indisponible', true ),
    --    (structureId, 'Professeur malade', true );

    -- TABLE: incident_type
    INSERT INTO presences.incident_type(id_etablissement, libelle) VALUES
      (structureId, 'Insulte' ),
      (structureId, 'Agression' ),
      (structureId, 'Harcèlement' ),
      (structureId, 'Vol' ),
      (structureId, 'Vandalisme' ),
      (structureId, 'Non respect du RI' );

    -- TABLE: incident_gravite
    INSERT INTO presences.incident_gravite(id_etablissement, niveau, libelle) VALUES
      (structureId, 0, 'Bénin' ),
      (structureId, 1, 'Peu grave' ),
      (structureId, 2, 'Assez grave' ),
      (structureId, 3, 'Grave' ),
      (structureId, 4, 'Très grave' ),
      (structureId, 5, 'Extrêmement grave' );

    -- TABLE: incident_lieu
    INSERT INTO presences.incident_lieu(id_etablissement, libelle) VALUES
      (structureId, 'Cours de récréation' ),
      (structureId, 'Préau' ),
      (structureId, 'Réfectoire' ),
      (structureId, 'Salle de classe' ),
      (structureId, 'CDI' ),
      (structureId, 'Extérieur de l établissement' ),
      (structureId, 'Autre (toilettes / couloirs)' );
	
    -- TABLE: incident_partenaire
    INSERT INTO presences.incident_partenaire(id_etablissement, libelle) VALUES
      (structureId, 'Police' ),
      (structureId, 'Parquet' ),
      (structureId, 'Rectorat' ),
      (structureId, 'Inspection Académique' );

    -- TABLE: incident_protagoniste_type
    INSERT INTO presences.incident_protagoniste_type(id_etablissement, libelle) VALUES 
      (structureId, 'Victime' ),
      (structureId, 'Agresseur' ),
      (structureId, 'Témoin' );

    -- TABLE: motif
    INSERT INTO presences.motif(id_etablissement, libelle, justifiant, collectif, is_sanction) VALUES 
      -- Motifs jutifiants
      (structureId, 'Maladie avec justificatif',                          true, false, false ),
      (structureId, 'Hospitalisation',                                    true, false, false ),
      (structureId, 'Rendez-vous médical',                                true, false, false ),
      (structureId, 'Retard transport',                                   true, false, false ),
      (structureId, 'Grève transport',                                    true, false, false ),
      (structureId, 'Raison familiale',                                   true, false, false ),
      (structureId, 'Rendez-vous extérieur',                              true, false, false ),
      (structureId, 'Compétition sportive',                               true, false, false ),
      (structureId, 'Absent de cours mais présent dans l établissement',  true, false, false ),
      -- Motifs non jutifiants
      (structureId, 'Maladie sans justificatif',                false, false, false ),
      (structureId, 'Appel famille / En attente justificatif',  false, false, false ),
      (structureId, 'Panne de réveil',                          false, false, false ),
      (structureId, 'Confusion emploi du temps',                false, false, false ),
      (structureId, 'Retard non justifié',                      false, false, false ),
      -- Motifs collectifs
      (structureId, 'Voyage scolaire',            true, true, false ),
      (structureId, 'Stage',                      true, true, false ),
      (structureId, 'Compétition sportive',       true, true, false ),
    -- Motifs de sanction
      (structureId, 'Exclusion de cours',    true, false, true ),
      (structureId, 'Exclusion temporaire',  true, false, true ),
      (structureId, 'Exclusion définitive',  true, false, true );

    -- TABLE: presence_period_type
    INSERT INTO presences.presence_period_type(structure_id, label) VALUES
      (structureId, 'Heure de retenue'),
      (structureId, 'Cours de rattrapage'),
      (structureId, 'Devoir sur table');
	
    -- TABLE: punishment_type
    INSERT INTO  presences.punishment_type(structure_id, label, require_presence_period)VALUES
      (structureId, 'Retenue',                      true),
      (structureId, 'Devoir officiel sur table',    true),
      (structureId, 'Devoir officiel à la maison',  false),
      (structureId, 'Devoir supplémentaire',        false),
      (structureId, 'Cours supplémentaire',         true),
      (structureId, 'Fiche de réflexion',           false),
      (structureId, 'TIG',                          false);
	
    -- TABLE: sanction_type
    INSERT INTO presences.sanction_type(structure_id, label, require_period_absence) VALUES
      (structureId, 'Avertissement',                false),
      (structureId, 'Blâme',                        false),
      (structureId, 'Mesure de responsabilisation', false),
      (structureId, 'Exclusion avec sursis',        false),
      (structureId, 'Exclusion',                    true);
  END;
  $$ LANGUAGE plpgsql;
END;
