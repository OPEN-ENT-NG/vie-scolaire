BEGIN;

-- Initialisation des tables de présences
-- Le fichier est découpé en deux parties:
-- - Initialisation des données globales à tous les établissements
-- - Procédure permettant l'Initialisation des données spécifiques à chaque établissement

-- TABLE: mailing_element_type
  TRUNCATE TABLE presences.mailing_element_type CASCADE;

  INSERT INTO presences.mailing_element_type(id, label) VALUES (1, 'Absences');
  INSERT INTO presences.mailing_element_type(id, label) VALUES (2, 'Retards');
  INSERT INTO presences.mailing_element_type(id, label) VALUES (3, 'Punition');
  INSERT INTO presences.mailing_element_type(id, label) VALUES (4, 'Sanction');

  -- TABLE: mailing_type
  TRUNCATE TABLE presences.mailing_type CASCADE;

  INSERT INTO presences.mailing_type(id, label) VALUES (1, 'PDF');
  INSERT INTO presences.mailing_type(id, label) VALUES (2, 'Email');
  INSERT INTO presences.mailing_type(id, label) VALUES (3, 'SMS');

-- TABLE: etat_appel
  TRUNCATE TABLE presences.etat_appel CASCADE;

  INSERT INTO presences.etat_appel(id, libelle) VALUES (1, 'Init');
  INSERT INTO presences.etat_appel(id, libelle) VALUES (2, 'En cours');
  INSERT INTO presences.etat_appel(id, libelle) VALUES (3, 'Fait');

-- TABLE: type_evt
  TRUNCATE TABLE presences.type_evt CASCADE;

  INSERT INTO presences.type_evt(id, libelle) VALUES (1, 'Absence');
  INSERT INTO presences.type_evt(id, libelle) VALUES (2, 'Retard');
  INSERT INTO presences.type_evt(id, libelle) VALUES (3, 'Départ');
  INSERT INTO presences.type_evt(id, libelle) VALUES (4, 'Incident');
  INSERT INTO presences.type_evt(id, libelle) VALUES (5, 'Observation');

  -- Procedure to insert a new city
  CREATE OR REPLACE FUNCTION presences.activateStructure(structureId VARCHAR(36))
    RETURNS void AS $$
  BEGIN
    -- TABLE: etablissements_actifs
    INSERT INTO presences.etablissements_actifs(id_etablissement, actif) VALUES (structureId, TRUE );

    -- TABLE: categorie_motif_absence
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'Non Justifiant' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'Justifiant' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'CATEGORIE ABSENCE' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'TRANSPORT' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'MEDICAL' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'SANTE' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'DEFAUT' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'AUTRE' );
    INSERT INTO presences.categorie_motif_absence(id_etablissement, libelle) VALUES (structureId, 'EXCUSES' );

    -- TABLE: categorie_motif_appel
    -- INSERT INTO presences.categorie_motif_appel(id_etablissement, libelle) VALUES (structureId, 'MEDICAL' );
    -- INSERT INTO presences.categorie_motif_appel(id_etablissement, libelle) VALUES (structureId, 'TECHNIQUE' );
    -- INSERT INTO presences.categorie_motif_appel(id_etablissement, libelle) VALUES (structureId, 'CATEGORIE APPEL' );

    -- TABLE: motif_appel
    -- INSERT INTO presences.motif_appel(id_etablissement, libelle, justifiant) VALUES (structureId, 'Réseau indisponible', true );
    -- INSERT INTO presences.motif_appel(id_etablissement, libelle, justifiant) VALUES (structureId, 'Professeur malade', true );

    -- TABLE: incident_gravite
    INSERT INTO presences.incident_gravite(id_etablissement, niveau, libelle) VALUES (structureId, 0, 'Bénin' );
    INSERT INTO presences.incident_gravite(id_etablissement, niveau, libelle) VALUES (structureId, 1, 'Peu grave' );
    INSERT INTO presences.incident_gravite(id_etablissement, niveau, libelle) VALUES (structureId, 2, 'Assez grave' );
    INSERT INTO presences.incident_gravite(id_etablissement, niveau, libelle) VALUES (structureId, 3, 'Grave' );
    INSERT INTO presences.incident_gravite(id_etablissement, niveau, libelle) VALUES (structureId, 4, 'Très grave' );
    INSERT INTO presences.incident_gravite(id_etablissement, niveau, libelle) VALUES (structureId, 5, 'Extrêmement grave' );

    -- TABLE: incident_lieu
    INSERT INTO presences.incident_lieu(id_etablissement, libelle) VALUES (structureId, 'Cours de récréation' );
    INSERT INTO presences.incident_lieu(id_etablissement, libelle) VALUES (structureId, 'Extérieur de l''établissement' );
    INSERT INTO presences.incident_lieu(id_etablissement, libelle) VALUES (structureId, 'Préau' );
    INSERT INTO presences.incident_lieu(id_etablissement, libelle) VALUES (structureId, 'Réfectoire' );
    INSERT INTO presences.incident_lieu(id_etablissement, libelle) VALUES (structureId, 'Salle de classe' );

    -- TABLE: incident_partenaire
    INSERT INTO presences.incident_partenaire(id_etablissement, libelle) VALUES (structureId, 'Parquet' );
    INSERT INTO presences.incident_partenaire(id_etablissement, libelle) VALUES (structureId, 'Rectorat' );
    INSERT INTO presences.incident_partenaire(id_etablissement, libelle) VALUES (structureId, 'Police' );
    INSERT INTO presences.incident_partenaire(id_etablissement, libelle) VALUES (structureId, 'Inspection Académique' );

    -- TABLE: incident_protagoniste_type
    INSERT INTO presences.incident_protagoniste_type(id_etablissement, libelle) VALUES (structureId, 'Victime' );
    INSERT INTO presences.incident_protagoniste_type(id_etablissement, libelle) VALUES (structureId, 'Agresseur' );
    INSERT INTO presences.incident_protagoniste_type(id_etablissement, libelle) VALUES (structureId, 'Témoin' );

    -- TABLE: motif
    INSERT INTO presences.motif(id_etablissement, libelle, justifiant, collectif, is_sanction) VALUES
      -- Motifs jutifiants
      (structureId, 'Maladie avec justificatif',  true, false, false ),
      (structureId, 'Retard train',               true, false, false ),
      (structureId, 'Hospitalisation',            true, false, false ),

      -- Motifs non jutifiants
      (structureId, 'Maladie sans justificatif',  false, false, false ),
      (structureId, 'Retard non justifié',        false, false, false ),

      -- Motifs collectifs
      (structureId, 'Stage',                      true, true, false ),
      (structureId, 'Voyage scolaire',            true, true, false ),

      -- Motifs de sanction
      (structureId, 'Exclusion',                  true, false, true );



    -- TABLE: presence_period_type
    INSERT INTO presences.presence_period_type(structure_id, label) VALUES (structureId, 'Cours de rattrapage');
    INSERT INTO presences.presence_period_type(structure_id, label) VALUES (structureId, 'Heure de retenue');

    -- TABLE: punishment_type
    INSERT INTO presences.punishment_type(structure_id, label, require_presence_period) VALUES (structureId, 'Devoir officiel', false);
    INSERT INTO presences.punishment_type(structure_id, label, require_presence_period) VALUES (structureId, 'Retenue', true);

    -- TABLE: sanction_type
    INSERT INTO presences.sanction_type(structure_id, label, require_period_absence) VALUES (structureId, 'Avertissement', false);
    INSERT INTO presences.sanction_type(structure_id, label, require_period_absence) VALUES (structureId, 'Expulsion 3J', true);

  END;
  $$ LANGUAGE plpgsql;





END;