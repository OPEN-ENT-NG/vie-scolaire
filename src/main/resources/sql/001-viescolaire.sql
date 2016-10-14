--
-- PostgreSQL database dump
--

-- Dumped from database version 9.5.1
-- Dumped by pg_dump version 9.6rc1

-- Started on 2016-09-20 10:01:17

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 12 (class 2615 OID 16812)
-- Name: abs; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA abs;


ALTER SCHEMA abs OWNER TO postgres;

--
-- TOC entry 13 (class 2615 OID 16813)
-- Name: notes; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA notes;


ALTER SCHEMA notes OWNER TO postgres;

--
-- TOC entry 14 (class 2615 OID 16814)
-- Name: viesco; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA viesco;


ALTER SCHEMA viesco OWNER TO postgres;

SET search_path = notes, pg_catalog;

--
-- TOC entry 743 (class 1247 OID 16817)
-- Name: share_tuple; Type: TYPE; Schema: notes; Owner: postgres
--

CREATE TYPE share_tuple AS (
	member_id character varying(36),
	action character varying(255)
);


ALTER TYPE share_tuple OWNER TO postgres;

SET search_path = abs, pg_catalog;

--
-- TOC entry 321 (class 1255 OID 17365)
-- Name: merge_users(character varying, character varying); Type: FUNCTION; Schema: abs; Owner: postgres
--

CREATE FUNCTION merge_users(key character varying, data character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
   LOOP
       UPDATE notes.users SET username = data WHERE id = key;
       IF found THEN
           RETURN;
       END IF;
       BEGIN
           INSERT INTO notes.users(id,username) VALUES (key, data);
           RETURN;
       EXCEPTION WHEN unique_violation THEN
       END;
   END LOOP;
END;
$$;


ALTER FUNCTION abs.merge_users(key character varying, data character varying) OWNER TO postgres;

SET search_path = notes, pg_catalog;

--
-- TOC entry 318 (class 1255 OID 16818)
-- Name: insert_groups_members(); Type: FUNCTION; Schema: notes; Owner: postgres
--

CREATE FUNCTION insert_groups_members() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
   BEGIN
     IF (TG_OP = 'INSERT') THEN
           INSERT INTO notes.members (id, group_id) VALUES (NEW.id, NEW.id);
           RETURN NEW;
       END IF;
       RETURN NULL;
   END;
$$;


ALTER FUNCTION notes.insert_groups_members() OWNER TO postgres;

--
-- TOC entry 319 (class 1255 OID 16819)
-- Name: insert_users_members(); Type: FUNCTION; Schema: notes; Owner: postgres
--

CREATE FUNCTION insert_users_members() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
   BEGIN
     IF (TG_OP = 'INSERT') THEN
           INSERT INTO notes.members (id, user_id) VALUES (NEW.id, NEW.id);
           RETURN NEW;
       END IF;
       RETURN NULL;
   END;
$$;


ALTER FUNCTION notes.insert_users_members() OWNER TO postgres;

--
-- TOC entry 320 (class 1255 OID 16820)
-- Name: merge_users(character varying, character varying); Type: FUNCTION; Schema: notes; Owner: postgres
--

CREATE FUNCTION merge_users(key character varying, data character varying) RETURNS void
    LANGUAGE plpgsql
    AS $$
BEGIN
   LOOP
       UPDATE notes.users SET username = data WHERE id = key;
       IF found THEN
           RETURN;
       END IF;
       BEGIN
           INSERT INTO notes.users(id,username) VALUES (key, data);
           RETURN;
       EXCEPTION WHEN unique_violation THEN
       END;
   END LOOP;
END;
$$;


ALTER FUNCTION notes.merge_users(key character varying, data character varying) OWNER TO postgres;

SET search_path = abs, pg_catalog;

SET default_tablespace = '';

SET default_with_oids = false;

--
-- TOC entry 222 (class 1259 OID 16821)
-- Name: absence_prev; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE absence_prev (
    absence_prev_id bigint NOT NULL,
    absence_prev_restriction_matiere character varying(42),
    absence_prev_timestamp_dt timestamp without time zone,
    absence_prev_timestamp_fn timestamp without time zone,
    fk_eleve_id bigint,
    fk_motif_id bigint
);


ALTER TABLE absence_prev OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 16824)
-- Name: absence_prev_absence_prev_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE absence_prev_absence_prev_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE absence_prev_absence_prev_id_seq OWNER TO postgres;

--
-- TOC entry 2618 (class 0 OID 0)
-- Dependencies: 223
-- Name: absence_prev_absence_prev_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE absence_prev_absence_prev_id_seq OWNED BY absence_prev.absence_prev_id;


--
-- TOC entry 224 (class 1259 OID 16826)
-- Name: appel; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE appel (
    id bigint NOT NULL,
    fk_personnel_id bigint,
    fk_cours_id bigint,
    fk_etat_appel_id bigint,
    fk_justificatif_appel_id bigint,
    owner character varying(36),
    created timestamp without time zone,
    modified timestamp without time zone
);


ALTER TABLE appel OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 16829)
-- Name: appel_appel_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE appel_appel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE appel_appel_id_seq OWNER TO postgres;

--
-- TOC entry 2619 (class 0 OID 0)
-- Dependencies: 225
-- Name: appel_appel_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE appel_appel_id_seq OWNED BY appel.id;


--
-- TOC entry 226 (class 1259 OID 16831)
-- Name: creneaux; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE creneaux (
    creneaux_id bigint NOT NULL,
    fk4j_etab_id uuid,
    creneaux_timestamp_dt timestamp without time zone,
    creneaux_timestamp_fn timestamp without time zone
);


ALTER TABLE creneaux OWNER TO postgres;

--
-- TOC entry 227 (class 1259 OID 16834)
-- Name: creneaux_creneaux_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE creneaux_creneaux_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE creneaux_creneaux_id_seq OWNER TO postgres;

--
-- TOC entry 2620 (class 0 OID 0)
-- Dependencies: 227
-- Name: creneaux_creneaux_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE creneaux_creneaux_id_seq OWNED BY creneaux.creneaux_id;


--
-- TOC entry 228 (class 1259 OID 16836)
-- Name: etat_appel; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE etat_appel (
    etat_appel_id bigint NOT NULL,
    fk4j_etab_id uuid,
    etat_appel_libelle character varying(42)
);


ALTER TABLE etat_appel OWNER TO postgres;

--
-- TOC entry 229 (class 1259 OID 16839)
-- Name: etat_appel_etat_appel_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE etat_appel_etat_appel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE etat_appel_etat_appel_id_seq OWNER TO postgres;

--
-- TOC entry 2621 (class 0 OID 0)
-- Dependencies: 229
-- Name: etat_appel_etat_appel_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE etat_appel_etat_appel_id_seq OWNED BY etat_appel.etat_appel_id;


--
-- TOC entry 230 (class 1259 OID 16841)
-- Name: evenement; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE evenement (
    id bigint NOT NULL,
    evenement_timestamp_arrive timestamp without time zone,
    evenement_timestamp_depart timestamp without time zone,
    evenement_commentaire character varying(250),
    evenement_saisie_cpe boolean,
    fk_eleve_id bigint,
    fk_appel_id bigint,
    fk_type_evt_id bigint,
    fk_pj_pj bigint,
    fk_motif_id bigint,
    owner character varying(36),
    created timestamp without time zone,
    modified timestamp without time zone
);


ALTER TABLE evenement OWNER TO postgres;

--
-- TOC entry 231 (class 1259 OID 16844)
-- Name: evenement_evenement_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE evenement_evenement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE evenement_evenement_id_seq OWNER TO postgres;

--
-- TOC entry 2622 (class 0 OID 0)
-- Dependencies: 231
-- Name: evenement_evenement_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE evenement_evenement_id_seq OWNED BY evenement.id;


--
-- TOC entry 232 (class 1259 OID 16846)
-- Name: evenement_hist; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE evenement_hist (
    evenement_hist_id bigint NOT NULL,
    fk_personnel_id bigint,
    fk_evenement_id bigint,
    evenement_hist_description character varying(42),
    evenement_hist_detail character varying(1024),
    evenement_hist_timestamp_mod timestamp without time zone
);


ALTER TABLE evenement_hist OWNER TO postgres;

--
-- TOC entry 233 (class 1259 OID 16852)
-- Name: evenement_hist_evenement_hist_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE evenement_hist_evenement_hist_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE evenement_hist_evenement_hist_id_seq OWNER TO postgres;

--
-- TOC entry 2623 (class 0 OID 0)
-- Dependencies: 233
-- Name: evenement_hist_evenement_hist_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE evenement_hist_evenement_hist_id_seq OWNED BY evenement_hist.evenement_hist_id;


--
-- TOC entry 234 (class 1259 OID 16854)
-- Name: justificatif_appel; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE justificatif_appel (
    justificatif_appel_id bigint NOT NULL,
    fk4j_etab_id uuid,
    justificatif_appel_libelle character varying(42)
);


ALTER TABLE justificatif_appel OWNER TO postgres;

--
-- TOC entry 235 (class 1259 OID 16857)
-- Name: justificatif_appel_justificatif_applel_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE justificatif_appel_justificatif_applel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE justificatif_appel_justificatif_applel_id_seq OWNER TO postgres;

--
-- TOC entry 2624 (class 0 OID 0)
-- Dependencies: 235
-- Name: justificatif_appel_justificatif_applel_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE justificatif_appel_justificatif_applel_id_seq OWNED BY justificatif_appel.justificatif_appel_id;


--
-- TOC entry 236 (class 1259 OID 16859)
-- Name: motif; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE motif (
    motif_id bigint NOT NULL,
    fk4j_etab_id uuid,
    motif_libelle character varying(150),
    motif_justifiant boolean,
    motif_commentaire character varying(250),
    motif_defaut boolean
);


ALTER TABLE motif OWNER TO postgres;

--
-- TOC entry 237 (class 1259 OID 16862)
-- Name: motif_motif_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE motif_motif_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE motif_motif_id_seq OWNER TO postgres;

--
-- TOC entry 2625 (class 0 OID 0)
-- Dependencies: 237
-- Name: motif_motif_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE motif_motif_id_seq OWNED BY motif.motif_id;


--
-- TOC entry 238 (class 1259 OID 16864)
-- Name: pj; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE pj (
    pj_id bigint NOT NULL,
    pj_doc character varying(250)
);


ALTER TABLE pj OWNER TO postgres;

--
-- TOC entry 239 (class 1259 OID 16867)
-- Name: pj_pj_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE pj_pj_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE pj_pj_id_seq OWNER TO postgres;

--
-- TOC entry 2626 (class 0 OID 0)
-- Dependencies: 239
-- Name: pj_pj_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE pj_pj_id_seq OWNED BY pj.pj_id;


--
-- TOC entry 240 (class 1259 OID 16869)
-- Name: se_produit_sur; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE se_produit_sur (
    evenement_id bigint NOT NULL,
    creneaux_id bigint NOT NULL
);


ALTER TABLE se_produit_sur OWNER TO postgres;

--
-- TOC entry 241 (class 1259 OID 16872)
-- Name: se_produit_sur_evenement_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE se_produit_sur_evenement_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE se_produit_sur_evenement_id_seq OWNER TO postgres;

--
-- TOC entry 2627 (class 0 OID 0)
-- Dependencies: 241
-- Name: se_produit_sur_evenement_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE se_produit_sur_evenement_id_seq OWNED BY se_produit_sur.evenement_id;


--
-- TOC entry 242 (class 1259 OID 16874)
-- Name: type_evt; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE type_evt (
    type_evt_id bigint NOT NULL,
    fk4j_etab_id uuid,
    type_evt_libelle character varying(42)
);


ALTER TABLE type_evt OWNER TO postgres;

--
-- TOC entry 243 (class 1259 OID 16877)
-- Name: type_evt_type_evt_id_seq; Type: SEQUENCE; Schema: abs; Owner: postgres
--

CREATE SEQUENCE type_evt_type_evt_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE type_evt_type_evt_id_seq OWNER TO postgres;

--
-- TOC entry 2628 (class 0 OID 0)
-- Dependencies: 243
-- Name: type_evt_type_evt_id_seq; Type: SEQUENCE OWNED BY; Schema: abs; Owner: postgres
--

ALTER SEQUENCE type_evt_type_evt_id_seq OWNED BY type_evt.type_evt_id;


--
-- TOC entry 291 (class 1259 OID 17366)
-- Name: users; Type: TABLE; Schema: abs; Owner: postgres
--

CREATE TABLE users (
    id character varying(36) NOT NULL,
    username character varying(255)
);


ALTER TABLE users OWNER TO postgres;

SET search_path = notes, pg_catalog;

--
-- TOC entry 244 (class 1259 OID 16879)
-- Name: competences; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE competences (
    id bigint NOT NULL,
    nom text NOT NULL,
    description text,
    idparent integer,
    idtype integer NOT NULL,
    idenseignement integer,
    owner character varying(36),
    created timestamp without time zone,
    modified timestamp without time zone
);


ALTER TABLE competences OWNER TO postgres;

--
-- TOC entry 245 (class 1259 OID 16885)
-- Name: competences_devoirs; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE competences_devoirs (
    id bigint NOT NULL,
    iddevoir integer,
    idcompetence integer,
    owner character varying(36),
    created timestamp without time zone,
    modified timestamp without time zone
);


ALTER TABLE competences_devoirs OWNER TO postgres;

--
-- TOC entry 246 (class 1259 OID 16888)
-- Name: competences-devoirs_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE "competences-devoirs_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "competences-devoirs_id_seq" OWNER TO postgres;

--
-- TOC entry 2629 (class 0 OID 0)
-- Dependencies: 246
-- Name: competences-devoirs_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE "competences-devoirs_id_seq" OWNED BY competences_devoirs.id;


--
-- TOC entry 247 (class 1259 OID 16890)
-- Name: competences_notes; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE competences_notes (
    id bigint NOT NULL,
    iddevoir integer,
    idcompetence integer,
    evaluation integer,
    owner character varying(36),
    ideleve character(36),
    created timestamp without time zone,
    modified timestamp without time zone
);


ALTER TABLE competences_notes OWNER TO postgres;

--
-- TOC entry 2630 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN competences_notes.idcompetence; Type: COMMENT; Schema: notes; Owner: postgres
--

COMMENT ON COLUMN competences_notes.idcompetence IS '
';


--
-- TOC entry 248 (class 1259 OID 16893)
-- Name: competences-notes_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE "competences-notes_id_seq"
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE "competences-notes_id_seq" OWNER TO postgres;

--
-- TOC entry 2631 (class 0 OID 0)
-- Dependencies: 248
-- Name: competences-notes_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE "competences-notes_id_seq" OWNED BY competences_notes.id;


--
-- TOC entry 249 (class 1259 OID 16895)
-- Name: competences_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE competences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE competences_id_seq OWNER TO postgres;

--
-- TOC entry 2632 (class 0 OID 0)
-- Dependencies: 249
-- Name: competences_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE competences_id_seq OWNED BY competences.id;


--
-- TOC entry 250 (class 1259 OID 16897)
-- Name: devoirs; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE devoirs (
    id bigint NOT NULL,
    name character varying(255),
    owner character varying(36) NOT NULL,
    created timestamp without time zone DEFAULT now() NOT NULL,
    modified timestamp without time zone DEFAULT now() NOT NULL,
    coefficient numeric,
    libelle character varying(255),
    idclasse character varying(255) NOT NULL,
    idsousmatiere bigint,
    idperiode bigint NOT NULL,
    idtype bigint NOT NULL,
    idetablissement character varying(255) NOT NULL,
    idetat bigint NOT NULL,
    diviseur integer NOT NULL,
    idmatiere character varying(255),
    ramenersur boolean,
    datepublication date,
    date date
);


ALTER TABLE devoirs OWNER TO postgres;

--
-- TOC entry 251 (class 1259 OID 16905)
-- Name: devoirs_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE devoirs_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE devoirs_id_seq OWNER TO postgres;

--
-- TOC entry 2633 (class 0 OID 0)
-- Dependencies: 251
-- Name: devoirs_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE devoirs_id_seq OWNED BY devoirs.id;


--
-- TOC entry 252 (class 1259 OID 16907)
-- Name: dispense; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE dispense (
    id bigint NOT NULL,
    libelle character varying(255),
    description text
);


ALTER TABLE dispense OWNER TO postgres;

--
-- TOC entry 253 (class 1259 OID 16913)
-- Name: dispense_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE dispense_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE dispense_id_seq OWNER TO postgres;

--
-- TOC entry 2634 (class 0 OID 0)
-- Dependencies: 253
-- Name: dispense_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE dispense_id_seq OWNED BY dispense.id;


--
-- TOC entry 254 (class 1259 OID 16915)
-- Name: enseignements; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE enseignements (
    id bigint NOT NULL,
    nom character varying(255)
);


ALTER TABLE enseignements OWNER TO postgres;

--
-- TOC entry 255 (class 1259 OID 16918)
-- Name: enseignements_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE enseignements_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE enseignements_id_seq OWNER TO postgres;

--
-- TOC entry 2635 (class 0 OID 0)
-- Dependencies: 255
-- Name: enseignements_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE enseignements_id_seq OWNED BY enseignements.id;


--
-- TOC entry 256 (class 1259 OID 16920)
-- Name: etat; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE etat (
    id bigint NOT NULL,
    libelle character varying(255)
);


ALTER TABLE etat OWNER TO postgres;

--
-- TOC entry 257 (class 1259 OID 16923)
-- Name: etat_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE etat_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE etat_id_seq OWNER TO postgres;

--
-- TOC entry 2636 (class 0 OID 0)
-- Dependencies: 257
-- Name: etat_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE etat_id_seq OWNED BY etat.id;


--
-- TOC entry 258 (class 1259 OID 16925)
-- Name: groups; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE groups (
    id character varying(36) NOT NULL,
    name character varying(255)
);


ALTER TABLE groups OWNER TO postgres;

--
-- TOC entry 259 (class 1259 OID 16928)
-- Name: matiere; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE matiere (
    id bigint NOT NULL,
    evaluable boolean,
    matiere character varying(255) NOT NULL,
    idetablissement character varying(255),
    idprofesseur character varying(255)
);


ALTER TABLE matiere OWNER TO postgres;

--
-- TOC entry 260 (class 1259 OID 16934)
-- Name: matiere_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE matiere_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE matiere_id_seq OWNER TO postgres;

--
-- TOC entry 2637 (class 0 OID 0)
-- Dependencies: 260
-- Name: matiere_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE matiere_id_seq OWNED BY matiere.id;


--
-- TOC entry 261 (class 1259 OID 16936)
-- Name: members; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE members (
    id character varying(36) NOT NULL,
    user_id character varying(36),
    group_id character varying(36)
);


ALTER TABLE members OWNER TO postgres;

--
-- TOC entry 262 (class 1259 OID 16939)
-- Name: notes; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE notes (
    id bigint NOT NULL,
    ideleve character varying(255) NOT NULL,
    iddevoir bigint NOT NULL,
    valeur numeric NOT NULL,
    iddispense bigint,
    owner character varying(255),
    modified timestamp without time zone,
    created timestamp without time zone,
    appreciation text
);


ALTER TABLE notes OWNER TO postgres;

--
-- TOC entry 263 (class 1259 OID 16945)
-- Name: notes_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE notes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE notes_id_seq OWNER TO postgres;

--
-- TOC entry 2638 (class 0 OID 0)
-- Dependencies: 263
-- Name: notes_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE notes_id_seq OWNED BY notes.id;


--
-- TOC entry 264 (class 1259 OID 16947)
-- Name: periode; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE periode (
    id bigint NOT NULL,
    libelle character varying(255),
    datedebut date,
    datefin date,
    idetablissement character varying(255)
);


ALTER TABLE periode OWNER TO postgres;

--
-- TOC entry 265 (class 1259 OID 16953)
-- Name: periode_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE periode_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE periode_id_seq OWNER TO postgres;

--
-- TOC entry 2639 (class 0 OID 0)
-- Dependencies: 265
-- Name: periode_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE periode_id_seq OWNED BY periode.id;


--
-- TOC entry 266 (class 1259 OID 16955)
-- Name: scripts; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE scripts (
    filename character varying(255) NOT NULL,
    passed timestamp without time zone DEFAULT now() NOT NULL
);


ALTER TABLE scripts OWNER TO postgres;

--
-- TOC entry 267 (class 1259 OID 16959)
-- Name: shares; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE shares (
    member_id character varying(36) NOT NULL,
    resource_id bigint NOT NULL,
    action character varying(255) NOT NULL
);


ALTER TABLE shares OWNER TO postgres;

--
-- TOC entry 268 (class 1259 OID 16962)
-- Name: typesousmatiere_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE typesousmatiere_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE typesousmatiere_id_seq OWNER TO postgres;

--
-- TOC entry 269 (class 1259 OID 16964)
-- Name: typesousmatiere; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE typesousmatiere (
    id bigint DEFAULT nextval('typesousmatiere_id_seq'::regclass) NOT NULL,
    libelle character varying(255)
);


ALTER TABLE typesousmatiere OWNER TO postgres;

--
-- TOC entry 270 (class 1259 OID 16968)
-- Name: sousmatiere_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE sousmatiere_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE sousmatiere_id_seq OWNER TO postgres;

--
-- TOC entry 2640 (class 0 OID 0)
-- Dependencies: 270
-- Name: sousmatiere_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE sousmatiere_id_seq OWNED BY typesousmatiere.id;


--
-- TOC entry 271 (class 1259 OID 16970)
-- Name: sousmatiere; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE sousmatiere (
    id bigint DEFAULT nextval('sousmatiere_id_seq'::regclass) NOT NULL,
    id_typesousmatiere bigint NOT NULL,
    id_matiere character varying(255) NOT NULL
);


ALTER TABLE sousmatiere OWNER TO postgres;

--
-- TOC entry 272 (class 1259 OID 16974)
-- Name: type; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE type (
    id bigint NOT NULL,
    nom character varying(255),
    idetablissement character varying(255),
    "default" boolean
);


ALTER TABLE type OWNER TO postgres;

--
-- TOC entry 273 (class 1259 OID 16980)
-- Name: type_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE type_id_seq OWNER TO postgres;

--
-- TOC entry 2641 (class 0 OID 0)
-- Dependencies: 273
-- Name: type_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE type_id_seq OWNED BY type.id;


--
-- TOC entry 274 (class 1259 OID 16982)
-- Name: typecompetences; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE typecompetences (
    id bigint NOT NULL,
    nom character varying(255)
);


ALTER TABLE typecompetences OWNER TO postgres;

--
-- TOC entry 275 (class 1259 OID 16985)
-- Name: typecompetences_id_seq; Type: SEQUENCE; Schema: notes; Owner: postgres
--

CREATE SEQUENCE typecompetences_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE typecompetences_id_seq OWNER TO postgres;

--
-- TOC entry 2642 (class 0 OID 0)
-- Dependencies: 275
-- Name: typecompetences_id_seq; Type: SEQUENCE OWNED BY; Schema: notes; Owner: postgres
--

ALTER SEQUENCE typecompetences_id_seq OWNED BY typecompetences.id;


--
-- TOC entry 276 (class 1259 OID 16987)
-- Name: users; Type: TABLE; Schema: notes; Owner: postgres
--

CREATE TABLE users (
    id character varying(36) NOT NULL,
    username character varying(255)
);


ALTER TABLE users OWNER TO postgres;

SET search_path = viesco, pg_catalog;

--
-- TOC entry 277 (class 1259 OID 16990)
-- Name: classe; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE classe (
    classe_id bigint NOT NULL,
    fk4j_classe_id uuid,
    fk4j_etab_id uuid,
    classe_externalid character varying(42),
    classe_libelle character varying(42),
    fk_type_classe_id bigint
);


ALTER TABLE classe OWNER TO postgres;

--
-- TOC entry 278 (class 1259 OID 16993)
-- Name: classe_classe_id_seq; Type: SEQUENCE; Schema: viesco; Owner: postgres
--

CREATE SEQUENCE classe_classe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE classe_classe_id_seq OWNER TO postgres;

--
-- TOC entry 2643 (class 0 OID 0)
-- Dependencies: 278
-- Name: classe_classe_id_seq; Type: SEQUENCE OWNED BY; Schema: viesco; Owner: postgres
--

ALTER SEQUENCE classe_classe_id_seq OWNED BY classe.classe_id;


--
-- TOC entry 279 (class 1259 OID 16995)
-- Name: cours; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE cours (
    cours_id bigint NOT NULL,
    fk4j_etab_id uuid,
    cours_timestamp_dt timestamp without time zone,
    cours_timestamp_fn timestamp without time zone,
    cours_salle character varying(42),
    cours_matiere character varying(42),
    fk_edt_classe character varying(42),
    fk_edt_date character varying(42),
    fk_edt_salle character varying(42),
    fk_edt_matiere character varying(42),
    fk_edt_id_cours character varying(42),
    fk_classe_id bigint
);


ALTER TABLE cours OWNER TO postgres;

--
-- TOC entry 280 (class 1259 OID 16998)
-- Name: cours_cours_id_seq; Type: SEQUENCE; Schema: viesco; Owner: postgres
--

CREATE SEQUENCE cours_cours_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE cours_cours_id_seq OWNER TO postgres;

--
-- TOC entry 2644 (class 0 OID 0)
-- Dependencies: 280
-- Name: cours_cours_id_seq; Type: SEQUENCE OWNED BY; Schema: viesco; Owner: postgres
--

ALTER SEQUENCE cours_cours_id_seq OWNED BY cours.cours_id;


--
-- TOC entry 281 (class 1259 OID 17000)
-- Name: eleve; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE eleve (
    eleve_id bigint NOT NULL,
    fk4j_user_id uuid,
    eleve_externalid bigint,
    eleve_nom character varying(42),
    eleve_prenom character varying(42)
);


ALTER TABLE eleve OWNER TO postgres;

--
-- TOC entry 282 (class 1259 OID 17003)
-- Name: eleve_eleve_id_seq; Type: SEQUENCE; Schema: viesco; Owner: postgres
--

CREATE SEQUENCE eleve_eleve_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE eleve_eleve_id_seq OWNER TO postgres;

--
-- TOC entry 2645 (class 0 OID 0)
-- Dependencies: 282
-- Name: eleve_eleve_id_seq; Type: SEQUENCE OWNED BY; Schema: viesco; Owner: postgres
--

ALTER SEQUENCE eleve_eleve_id_seq OWNED BY eleve.eleve_id;


--
-- TOC entry 283 (class 1259 OID 17005)
-- Name: periode; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE periode (
    periode_id bigint NOT NULL,
    fk4j_etab_id uuid,
    periode_libelle character varying(42),
    periode_timestamp_dt timestamp without time zone,
    periode_timestamp_fn timestamp without time zone
);


ALTER TABLE periode OWNER TO postgres;

--
-- TOC entry 284 (class 1259 OID 17008)
-- Name: periode_periode_id_seq; Type: SEQUENCE; Schema: viesco; Owner: postgres
--

CREATE SEQUENCE periode_periode_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE periode_periode_id_seq OWNER TO postgres;

--
-- TOC entry 2646 (class 0 OID 0)
-- Dependencies: 284
-- Name: periode_periode_id_seq; Type: SEQUENCE OWNED BY; Schema: viesco; Owner: postgres
--

ALTER SEQUENCE periode_periode_id_seq OWNED BY periode.periode_id;


--
-- TOC entry 285 (class 1259 OID 17010)
-- Name: personnel; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE personnel (
    personnel_id bigint NOT NULL,
    fk4j_user_id uuid,
    personnel_externalid bigint,
    personnel_nom character varying(42),
    personnel_prenom character varying(42),
    personnel_profil character varying(42),
    personnel_enseigne boolean,
    fk4j_etab_id uuid
);


ALTER TABLE personnel OWNER TO postgres;

--
-- TOC entry 286 (class 1259 OID 17013)
-- Name: personnel_personnel_id_seq; Type: SEQUENCE; Schema: viesco; Owner: postgres
--

CREATE SEQUENCE personnel_personnel_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE personnel_personnel_id_seq OWNER TO postgres;

--
-- TOC entry 2647 (class 0 OID 0)
-- Dependencies: 286
-- Name: personnel_personnel_id_seq; Type: SEQUENCE OWNED BY; Schema: viesco; Owner: postgres
--

ALTER SEQUENCE personnel_personnel_id_seq OWNED BY personnel.personnel_id;


--
-- TOC entry 287 (class 1259 OID 17015)
-- Name: rel_eleve_classe; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE rel_eleve_classe (
    fk_classe_id bigint NOT NULL,
    fk_eleve_id bigint NOT NULL
);


ALTER TABLE rel_eleve_classe OWNER TO postgres;

--
-- TOC entry 288 (class 1259 OID 17018)
-- Name: rel_personnel_cours; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE rel_personnel_cours (
    fk_personnel_id bigint NOT NULL,
    fk_cours_id bigint NOT NULL
);


ALTER TABLE rel_personnel_cours OWNER TO postgres;

--
-- TOC entry 289 (class 1259 OID 17021)
-- Name: type_classe; Type: TABLE; Schema: viesco; Owner: postgres
--

CREATE TABLE type_classe (
    type_classe_id bigint NOT NULL,
    type_classe_libelle character varying(42)
);


ALTER TABLE type_classe OWNER TO postgres;

--
-- TOC entry 290 (class 1259 OID 17024)
-- Name: type_classe_type_classe_id_seq; Type: SEQUENCE; Schema: viesco; Owner: postgres
--

CREATE SEQUENCE type_classe_type_classe_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE type_classe_type_classe_id_seq OWNER TO postgres;

--
-- TOC entry 2648 (class 0 OID 0)
-- Dependencies: 290
-- Name: type_classe_type_classe_id_seq; Type: SEQUENCE OWNED BY; Schema: viesco; Owner: postgres
--

ALTER SEQUENCE type_classe_type_classe_id_seq OWNED BY type_classe.type_classe_id;


SET search_path = abs, pg_catalog;

--
-- TOC entry 2341 (class 2604 OID 17026)
-- Name: absence_prev absence_prev_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY absence_prev ALTER COLUMN absence_prev_id SET DEFAULT nextval('absence_prev_absence_prev_id_seq'::regclass);


--
-- TOC entry 2342 (class 2604 OID 17027)
-- Name: appel id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY appel ALTER COLUMN id SET DEFAULT nextval('appel_appel_id_seq'::regclass);


--
-- TOC entry 2343 (class 2604 OID 17028)
-- Name: creneaux creneaux_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY creneaux ALTER COLUMN creneaux_id SET DEFAULT nextval('creneaux_creneaux_id_seq'::regclass);


--
-- TOC entry 2344 (class 2604 OID 17029)
-- Name: etat_appel etat_appel_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY etat_appel ALTER COLUMN etat_appel_id SET DEFAULT nextval('etat_appel_etat_appel_id_seq'::regclass);


--
-- TOC entry 2345 (class 2604 OID 17030)
-- Name: evenement id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement ALTER COLUMN id SET DEFAULT nextval('evenement_evenement_id_seq'::regclass);


--
-- TOC entry 2346 (class 2604 OID 17031)
-- Name: evenement_hist evenement_hist_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement_hist ALTER COLUMN evenement_hist_id SET DEFAULT nextval('evenement_hist_evenement_hist_id_seq'::regclass);


--
-- TOC entry 2347 (class 2604 OID 17032)
-- Name: justificatif_appel justificatif_appel_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY justificatif_appel ALTER COLUMN justificatif_appel_id SET DEFAULT nextval('justificatif_appel_justificatif_applel_id_seq'::regclass);


--
-- TOC entry 2348 (class 2604 OID 17033)
-- Name: motif motif_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY motif ALTER COLUMN motif_id SET DEFAULT nextval('motif_motif_id_seq'::regclass);


--
-- TOC entry 2349 (class 2604 OID 17034)
-- Name: pj pj_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY pj ALTER COLUMN pj_id SET DEFAULT nextval('pj_pj_id_seq'::regclass);


--
-- TOC entry 2350 (class 2604 OID 17035)
-- Name: se_produit_sur evenement_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY se_produit_sur ALTER COLUMN evenement_id SET DEFAULT nextval('se_produit_sur_evenement_id_seq'::regclass);


--
-- TOC entry 2351 (class 2604 OID 17036)
-- Name: type_evt type_evt_id; Type: DEFAULT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY type_evt ALTER COLUMN type_evt_id SET DEFAULT nextval('type_evt_type_evt_id_seq'::regclass);


SET search_path = notes, pg_catalog;

--
-- TOC entry 2352 (class 2604 OID 17037)
-- Name: competences id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences ALTER COLUMN id SET DEFAULT nextval('competences_id_seq'::regclass);


--
-- TOC entry 2353 (class 2604 OID 17038)
-- Name: competences_devoirs id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_devoirs ALTER COLUMN id SET DEFAULT nextval('"competences-devoirs_id_seq"'::regclass);


--
-- TOC entry 2354 (class 2604 OID 17039)
-- Name: competences_notes id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_notes ALTER COLUMN id SET DEFAULT nextval('"competences-notes_id_seq"'::regclass);


--
-- TOC entry 2357 (class 2604 OID 17040)
-- Name: devoirs id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY devoirs ALTER COLUMN id SET DEFAULT nextval('devoirs_id_seq'::regclass);


--
-- TOC entry 2358 (class 2604 OID 17041)
-- Name: dispense id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY dispense ALTER COLUMN id SET DEFAULT nextval('dispense_id_seq'::regclass);


--
-- TOC entry 2359 (class 2604 OID 17042)
-- Name: enseignements id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY enseignements ALTER COLUMN id SET DEFAULT nextval('enseignements_id_seq'::regclass);


--
-- TOC entry 2360 (class 2604 OID 17043)
-- Name: etat id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY etat ALTER COLUMN id SET DEFAULT nextval('etat_id_seq'::regclass);


--
-- TOC entry 2361 (class 2604 OID 17044)
-- Name: matiere id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY matiere ALTER COLUMN id SET DEFAULT nextval('matiere_id_seq'::regclass);


--
-- TOC entry 2362 (class 2604 OID 17045)
-- Name: notes id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY notes ALTER COLUMN id SET DEFAULT nextval('notes_id_seq'::regclass);


--
-- TOC entry 2363 (class 2604 OID 17046)
-- Name: periode id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY periode ALTER COLUMN id SET DEFAULT nextval('periode_id_seq'::regclass);


--
-- TOC entry 2367 (class 2604 OID 17047)
-- Name: type id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY type ALTER COLUMN id SET DEFAULT nextval('type_id_seq'::regclass);


--
-- TOC entry 2368 (class 2604 OID 17048)
-- Name: typecompetences id; Type: DEFAULT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY typecompetences ALTER COLUMN id SET DEFAULT nextval('typecompetences_id_seq'::regclass);


SET search_path = viesco, pg_catalog;

--
-- TOC entry 2369 (class 2604 OID 17049)
-- Name: classe classe_id; Type: DEFAULT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY classe ALTER COLUMN classe_id SET DEFAULT nextval('classe_classe_id_seq'::regclass);


--
-- TOC entry 2370 (class 2604 OID 17050)
-- Name: cours cours_id; Type: DEFAULT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY cours ALTER COLUMN cours_id SET DEFAULT nextval('cours_cours_id_seq'::regclass);


--
-- TOC entry 2371 (class 2604 OID 17051)
-- Name: eleve eleve_id; Type: DEFAULT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY eleve ALTER COLUMN eleve_id SET DEFAULT nextval('eleve_eleve_id_seq'::regclass);


--
-- TOC entry 2372 (class 2604 OID 17052)
-- Name: periode periode_id; Type: DEFAULT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY periode ALTER COLUMN periode_id SET DEFAULT nextval('periode_periode_id_seq'::regclass);


--
-- TOC entry 2373 (class 2604 OID 17053)
-- Name: personnel personnel_id; Type: DEFAULT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY personnel ALTER COLUMN personnel_id SET DEFAULT nextval('personnel_personnel_id_seq'::regclass);


--
-- TOC entry 2374 (class 2604 OID 17054)
-- Name: type_classe type_classe_id; Type: DEFAULT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY type_classe ALTER COLUMN type_classe_id SET DEFAULT nextval('type_classe_type_classe_id_seq'::regclass);


SET search_path = abs, pg_catalog;

--
-- TOC entry 2376 (class 2606 OID 17056)
-- Name: absence_prev absence_prev_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY absence_prev
    ADD CONSTRAINT absence_prev_pkey PRIMARY KEY (absence_prev_id);


--
-- TOC entry 2378 (class 2606 OID 17058)
-- Name: appel appel_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY appel
    ADD CONSTRAINT appel_pkey PRIMARY KEY (id);


--
-- TOC entry 2380 (class 2606 OID 17060)
-- Name: creneaux creneaux_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY creneaux
    ADD CONSTRAINT creneaux_pkey PRIMARY KEY (creneaux_id);


--
-- TOC entry 2382 (class 2606 OID 17062)
-- Name: etat_appel etat_appel_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY etat_appel
    ADD CONSTRAINT etat_appel_pkey PRIMARY KEY (etat_appel_id);


--
-- TOC entry 2386 (class 2606 OID 17064)
-- Name: evenement_hist evenement_hist_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement_hist
    ADD CONSTRAINT evenement_hist_pkey PRIMARY KEY (evenement_hist_id);


--
-- TOC entry 2384 (class 2606 OID 17066)
-- Name: evenement evenement_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement
    ADD CONSTRAINT evenement_pkey PRIMARY KEY (id);


--
-- TOC entry 2388 (class 2606 OID 17068)
-- Name: justificatif_appel justificatif_appel_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY justificatif_appel
    ADD CONSTRAINT justificatif_appel_pkey PRIMARY KEY (justificatif_appel_id);


--
-- TOC entry 2390 (class 2606 OID 17070)
-- Name: motif motif_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY motif
    ADD CONSTRAINT motif_pkey PRIMARY KEY (motif_id);


--
-- TOC entry 2392 (class 2606 OID 17072)
-- Name: pj pj_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY pj
    ADD CONSTRAINT pj_pkey PRIMARY KEY (pj_id);


--
-- TOC entry 2394 (class 2606 OID 17074)
-- Name: se_produit_sur se_produit_sur_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY se_produit_sur
    ADD CONSTRAINT se_produit_sur_pkey PRIMARY KEY (evenement_id, creneaux_id);


--
-- TOC entry 2396 (class 2606 OID 17076)
-- Name: type_evt type_evt_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY type_evt
    ADD CONSTRAINT type_evt_pkey PRIMARY KEY (type_evt_id);


--
-- TOC entry 2459 (class 2606 OID 17370)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


SET search_path = notes, pg_catalog;

--
-- TOC entry 2432 (class 2606 OID 17078)
-- Name: typesousmatiere PK; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY typesousmatiere
    ADD CONSTRAINT "PK" PRIMARY KEY (id);


--
-- TOC entry 2398 (class 2606 OID 17080)
-- Name: competences PK competence; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences
    ADD CONSTRAINT "PK competence" PRIMARY KEY (id);


--
-- TOC entry 2403 (class 2606 OID 17082)
-- Name: competences_notes PK competences notes; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_notes
    ADD CONSTRAINT "PK competences notes" PRIMARY KEY (id);


--
-- TOC entry 2401 (class 2606 OID 17084)
-- Name: competences_devoirs PK devoirs competences; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_devoirs
    ADD CONSTRAINT "PK devoirs competences" PRIMARY KEY (id);


--
-- TOC entry 2413 (class 2606 OID 17086)
-- Name: enseignements PK enseignements; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY enseignements
    ADD CONSTRAINT "PK enseignements" PRIMARY KEY (id);


--
-- TOC entry 2437 (class 2606 OID 17088)
-- Name: type PKTYPE; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY type
    ADD CONSTRAINT "PKTYPE" PRIMARY KEY (id);


--
-- TOC entry 2406 (class 2606 OID 17090)
-- Name: devoirs devoirs_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY devoirs
    ADD CONSTRAINT devoirs_pkey PRIMARY KEY (id);


--
-- TOC entry 2411 (class 2606 OID 17092)
-- Name: dispense dispense_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY dispense
    ADD CONSTRAINT dispense_pkey PRIMARY KEY (id);


--
-- TOC entry 2417 (class 2606 OID 17094)
-- Name: groups groups_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);


--
-- TOC entry 2419 (class 2606 OID 17096)
-- Name: matiere matiere_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY matiere
    ADD CONSTRAINT matiere_pkey PRIMARY KEY (id);


--
-- TOC entry 2421 (class 2606 OID 17098)
-- Name: members members_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY members
    ADD CONSTRAINT members_pkey PRIMARY KEY (id);


--
-- TOC entry 2424 (class 2606 OID 17100)
-- Name: notes notes_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY notes
    ADD CONSTRAINT notes_pkey PRIMARY KEY (id);


--
-- TOC entry 2426 (class 2606 OID 17102)
-- Name: periode periode_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY periode
    ADD CONSTRAINT periode_pkey PRIMARY KEY (id);


--
-- TOC entry 2415 (class 2606 OID 17104)
-- Name: etat pketat; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY etat
    ADD CONSTRAINT pketat PRIMARY KEY (id);


--
-- TOC entry 2428 (class 2606 OID 17106)
-- Name: scripts scripts_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY scripts
    ADD CONSTRAINT scripts_pkey PRIMARY KEY (filename);


--
-- TOC entry 2430 (class 2606 OID 17108)
-- Name: shares share; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY shares
    ADD CONSTRAINT share PRIMARY KEY (member_id, resource_id, action);


--
-- TOC entry 2435 (class 2606 OID 17110)
-- Name: sousmatiere sousmatiere_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY sousmatiere
    ADD CONSTRAINT sousmatiere_pkey PRIMARY KEY (id);


--
-- TOC entry 2439 (class 2606 OID 17112)
-- Name: typecompetences typecompetences_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY typecompetences
    ADD CONSTRAINT typecompetences_pkey PRIMARY KEY (id);


--
-- TOC entry 2441 (class 2606 OID 17114)
-- Name: users users_pkey; Type: CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


SET search_path = viesco, pg_catalog;

--
-- TOC entry 2443 (class 2606 OID 17116)
-- Name: classe classe_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY classe
    ADD CONSTRAINT classe_pkey PRIMARY KEY (classe_id);


--
-- TOC entry 2445 (class 2606 OID 17118)
-- Name: cours cours_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY cours
    ADD CONSTRAINT cours_pkey PRIMARY KEY (cours_id);


--
-- TOC entry 2447 (class 2606 OID 17120)
-- Name: eleve eleve_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY eleve
    ADD CONSTRAINT eleve_pkey PRIMARY KEY (eleve_id);


--
-- TOC entry 2449 (class 2606 OID 17122)
-- Name: periode periode_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY periode
    ADD CONSTRAINT periode_pkey PRIMARY KEY (periode_id);


--
-- TOC entry 2451 (class 2606 OID 17124)
-- Name: personnel personnel_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY personnel
    ADD CONSTRAINT personnel_pkey PRIMARY KEY (personnel_id);


--
-- TOC entry 2453 (class 2606 OID 17126)
-- Name: rel_eleve_classe rel_eleve_classe_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY rel_eleve_classe
    ADD CONSTRAINT rel_eleve_classe_pkey PRIMARY KEY (fk_classe_id, fk_eleve_id);


--
-- TOC entry 2455 (class 2606 OID 17128)
-- Name: rel_personnel_cours rel_personnel_cours_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY rel_personnel_cours
    ADD CONSTRAINT rel_personnel_cours_pkey PRIMARY KEY (fk_personnel_id, fk_cours_id);


--
-- TOC entry 2457 (class 2606 OID 17130)
-- Name: type_classe type_classe_pkey; Type: CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY type_classe
    ADD CONSTRAINT type_classe_pkey PRIMARY KEY (type_classe_id);


SET search_path = notes, pg_catalog;

--
-- TOC entry 2404 (class 1259 OID 17342)
-- Name: competences_notes_id_devoir_id_eleve_idx; Type: INDEX; Schema: notes; Owner: postgres
--

CREATE INDEX competences_notes_id_devoir_id_eleve_idx ON competences_notes USING btree (iddevoir, ideleve);


--
-- TOC entry 2407 (class 1259 OID 17132)
-- Name: fki_FK type; Type: INDEX; Schema: notes; Owner: postgres
--

CREATE INDEX "fki_FK type" ON devoirs USING btree (idtype);


--
-- TOC entry 2422 (class 1259 OID 17133)
-- Name: fki_foreignDispense; Type: INDEX; Schema: notes; Owner: postgres
--

CREATE INDEX "fki_foreignDispense" ON notes USING btree (iddispense);


--
-- TOC entry 2408 (class 1259 OID 17134)
-- Name: fki_foreignEtat; Type: INDEX; Schema: notes; Owner: postgres
--

CREATE INDEX "fki_foreignEtat" ON devoirs USING btree (idetat);


--
-- TOC entry 2409 (class 1259 OID 17135)
-- Name: fki_foreignPeriode; Type: INDEX; Schema: notes; Owner: postgres
--

CREATE INDEX "fki_foreignPeriode" ON devoirs USING btree (idperiode);


--
-- TOC entry 2399 (class 1259 OID 17136)
-- Name: idx_compretences_idparent; Type: INDEX; Schema: notes; Owner: postgres
--

CREATE INDEX idx_compretences_idparent ON competences USING btree (idparent);


--
-- TOC entry 2433 (class 1259 OID 17137)
-- Name: sousmatiere_id_typesousmatiere_idx; Type: INDEX; Schema: notes; Owner: postgres
--

CREATE INDEX sousmatiere_id_typesousmatiere_idx ON sousmatiere USING btree (id_typesousmatiere, id_matiere);


--
-- TOC entry 2498 (class 2620 OID 17138)
-- Name: groups groups_trigger; Type: TRIGGER; Schema: notes; Owner: postgres
--

CREATE TRIGGER groups_trigger AFTER INSERT ON groups FOR EACH ROW EXECUTE PROCEDURE insert_groups_members();


--
-- TOC entry 2499 (class 2620 OID 17139)
-- Name: users users_trigger; Type: TRIGGER; Schema: notes; Owner: postgres
--

CREATE TRIGGER users_trigger AFTER INSERT ON users FOR EACH ROW EXECUTE PROCEDURE insert_users_members();


SET search_path = abs, pg_catalog;

--
-- TOC entry 2460 (class 2606 OID 17140)
-- Name: absence_prev absence_prev_fk_eleve_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY absence_prev
    ADD CONSTRAINT absence_prev_fk_eleve_id_fkey FOREIGN KEY (fk_eleve_id) REFERENCES viesco.eleve(eleve_id);


--
-- TOC entry 2461 (class 2606 OID 17145)
-- Name: absence_prev absence_prev_fk_motif_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY absence_prev
    ADD CONSTRAINT absence_prev_fk_motif_id_fkey FOREIGN KEY (fk_motif_id) REFERENCES motif(motif_id);


--
-- TOC entry 2462 (class 2606 OID 17150)
-- Name: appel appel_fk_cours_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY appel
    ADD CONSTRAINT appel_fk_cours_id_fkey FOREIGN KEY (fk_cours_id) REFERENCES viesco.cours(cours_id);


--
-- TOC entry 2463 (class 2606 OID 17155)
-- Name: appel appel_fk_etat_appel_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY appel
    ADD CONSTRAINT appel_fk_etat_appel_id_fkey FOREIGN KEY (fk_etat_appel_id) REFERENCES etat_appel(etat_appel_id);


--
-- TOC entry 2464 (class 2606 OID 17160)
-- Name: appel appel_fk_justificatif_applel_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY appel
    ADD CONSTRAINT appel_fk_justificatif_applel_id_fkey FOREIGN KEY (fk_justificatif_appel_id) REFERENCES justificatif_appel(justificatif_appel_id);


--
-- TOC entry 2465 (class 2606 OID 17165)
-- Name: appel appel_fk_personnel_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY appel
    ADD CONSTRAINT appel_fk_personnel_id_fkey FOREIGN KEY (fk_personnel_id) REFERENCES viesco.personnel(personnel_id);


--
-- TOC entry 2466 (class 2606 OID 17170)
-- Name: evenement evenement_fk_appel_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement
    ADD CONSTRAINT evenement_fk_appel_id_fkey FOREIGN KEY (fk_appel_id) REFERENCES appel(id);


--
-- TOC entry 2467 (class 2606 OID 17175)
-- Name: evenement evenement_fk_eleve_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement
    ADD CONSTRAINT evenement_fk_eleve_id_fkey FOREIGN KEY (fk_eleve_id) REFERENCES viesco.eleve(eleve_id);


--
-- TOC entry 2468 (class 2606 OID 17180)
-- Name: evenement evenement_fk_motif_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement
    ADD CONSTRAINT evenement_fk_motif_id_fkey FOREIGN KEY (fk_motif_id) REFERENCES motif(motif_id);


--
-- TOC entry 2469 (class 2606 OID 17185)
-- Name: evenement evenement_fk_pj_pj_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement
    ADD CONSTRAINT evenement_fk_pj_pj_fkey FOREIGN KEY (fk_pj_pj) REFERENCES pj(pj_id);


--
-- TOC entry 2470 (class 2606 OID 17190)
-- Name: evenement evenement_fk_type_evt_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement
    ADD CONSTRAINT evenement_fk_type_evt_id_fkey FOREIGN KEY (fk_type_evt_id) REFERENCES type_evt(type_evt_id);


--
-- TOC entry 2471 (class 2606 OID 17195)
-- Name: evenement_hist evenement_hist_fk_evenement_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement_hist
    ADD CONSTRAINT evenement_hist_fk_evenement_id_fkey FOREIGN KEY (fk_evenement_id) REFERENCES evenement(id);


--
-- TOC entry 2472 (class 2606 OID 17200)
-- Name: evenement_hist evenement_hist_fk_personnel_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY evenement_hist
    ADD CONSTRAINT evenement_hist_fk_personnel_id_fkey FOREIGN KEY (fk_personnel_id) REFERENCES viesco.personnel(personnel_id);


--
-- TOC entry 2473 (class 2606 OID 17205)
-- Name: se_produit_sur se_produit_sur_creneaux_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY se_produit_sur
    ADD CONSTRAINT se_produit_sur_creneaux_id_fkey FOREIGN KEY (creneaux_id) REFERENCES creneaux(creneaux_id);


--
-- TOC entry 2474 (class 2606 OID 17210)
-- Name: se_produit_sur se_produit_sur_evenement_id_fkey; Type: FK CONSTRAINT; Schema: abs; Owner: postgres
--

ALTER TABLE ONLY se_produit_sur
    ADD CONSTRAINT se_produit_sur_evenement_id_fkey FOREIGN KEY (evenement_id) REFERENCES evenement(id);


SET search_path = notes, pg_catalog;

--
-- TOC entry 2476 (class 2606 OID 17215)
-- Name: competences_devoirs FK competence; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_devoirs
    ADD CONSTRAINT "FK competence" FOREIGN KEY (idcompetence) REFERENCES competences(id);


--
-- TOC entry 2478 (class 2606 OID 17220)
-- Name: competences_notes FK competence; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_notes
    ADD CONSTRAINT "FK competence" FOREIGN KEY (idcompetence) REFERENCES competences(id);


--
-- TOC entry 2479 (class 2606 OID 17225)
-- Name: competences_notes FK devoir; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_notes
    ADD CONSTRAINT "FK devoir" FOREIGN KEY (iddevoir) REFERENCES devoirs(id);


--
-- TOC entry 2477 (class 2606 OID 17230)
-- Name: competences_devoirs FK devoirs; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences_devoirs
    ADD CONSTRAINT "FK devoirs" FOREIGN KEY (iddevoir) REFERENCES devoirs(id);


--
-- TOC entry 2475 (class 2606 OID 17235)
-- Name: competences FK enseignements; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY competences
    ADD CONSTRAINT "FK enseignements" FOREIGN KEY (idenseignement) REFERENCES enseignements(id);


--
-- TOC entry 2480 (class 2606 OID 17240)
-- Name: devoirs FK type; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY devoirs
    ADD CONSTRAINT "FK type" FOREIGN KEY (idtype) REFERENCES type(id);


--
-- TOC entry 2481 (class 2606 OID 17245)
-- Name: devoirs fkPeriode; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY devoirs
    ADD CONSTRAINT "fkPeriode" FOREIGN KEY (idperiode) REFERENCES periode(id);


--
-- TOC entry 2487 (class 2606 OID 17250)
-- Name: notes foreignDevoir; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY notes
    ADD CONSTRAINT "foreignDevoir" FOREIGN KEY (iddevoir) REFERENCES devoirs(id);


--
-- TOC entry 2488 (class 2606 OID 17255)
-- Name: notes foreignDispense; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY notes
    ADD CONSTRAINT "foreignDispense" FOREIGN KEY (iddispense) REFERENCES dispense(id);


--
-- TOC entry 2482 (class 2606 OID 17260)
-- Name: devoirs foreignEtat; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY devoirs
    ADD CONSTRAINT "foreignEtat" FOREIGN KEY (idetat) REFERENCES etat(id);


--
-- TOC entry 2483 (class 2606 OID 17265)
-- Name: devoirs foreignSousMatiere; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY devoirs
    ADD CONSTRAINT "foreignSousMatiere" FOREIGN KEY (idsousmatiere) REFERENCES typesousmatiere(id);


--
-- TOC entry 2485 (class 2606 OID 17270)
-- Name: members group_fk; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY members
    ADD CONSTRAINT group_fk FOREIGN KEY (group_id) REFERENCES groups(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2489 (class 2606 OID 17275)
-- Name: shares member_fk; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY shares
    ADD CONSTRAINT member_fk FOREIGN KEY (member_id) REFERENCES members(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2484 (class 2606 OID 17280)
-- Name: devoirs owner_fk; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY devoirs
    ADD CONSTRAINT owner_fk FOREIGN KEY (owner) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2490 (class 2606 OID 17285)
-- Name: shares resource_fk; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY shares
    ADD CONSTRAINT resource_fk FOREIGN KEY (resource_id) REFERENCES devoirs(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- TOC entry 2491 (class 2606 OID 17290)
-- Name: sousmatiere sousmatiere_id_typesousmatiere_fkey; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY sousmatiere
    ADD CONSTRAINT sousmatiere_id_typesousmatiere_fkey FOREIGN KEY (id_typesousmatiere) REFERENCES typesousmatiere(id);


--
-- TOC entry 2486 (class 2606 OID 17295)
-- Name: members user_fk; Type: FK CONSTRAINT; Schema: notes; Owner: postgres
--

ALTER TABLE ONLY members
    ADD CONSTRAINT user_fk FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE ON DELETE CASCADE;


SET search_path = viesco, pg_catalog;

--
-- TOC entry 2492 (class 2606 OID 17300)
-- Name: classe classe_fk_type_classe_id_fkey; Type: FK CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY classe
    ADD CONSTRAINT classe_fk_type_classe_id_fkey FOREIGN KEY (fk_type_classe_id) REFERENCES type_classe(type_classe_id);


--
-- TOC entry 2493 (class 2606 OID 17305)
-- Name: cours cours_fk_classe_id_fkey; Type: FK CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY cours
    ADD CONSTRAINT cours_fk_classe_id_fkey FOREIGN KEY (fk_classe_id) REFERENCES classe(classe_id);


--
-- TOC entry 2494 (class 2606 OID 17310)
-- Name: rel_eleve_classe rel_eleve_classe_fk_classe_id_fkey; Type: FK CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY rel_eleve_classe
    ADD CONSTRAINT rel_eleve_classe_fk_classe_id_fkey FOREIGN KEY (fk_classe_id) REFERENCES classe(classe_id);


--
-- TOC entry 2495 (class 2606 OID 17315)
-- Name: rel_eleve_classe rel_eleve_classe_fk_eleve_id_fkey; Type: FK CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY rel_eleve_classe
    ADD CONSTRAINT rel_eleve_classe_fk_eleve_id_fkey FOREIGN KEY (fk_eleve_id) REFERENCES eleve(eleve_id);


--
-- TOC entry 2496 (class 2606 OID 17320)
-- Name: rel_personnel_cours rel_personnel_cours_fk_cours_id_fkey; Type: FK CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY rel_personnel_cours
    ADD CONSTRAINT rel_personnel_cours_fk_cours_id_fkey FOREIGN KEY (fk_cours_id) REFERENCES cours(cours_id);


--
-- TOC entry 2497 (class 2606 OID 17325)
-- Name: rel_personnel_cours rel_personnel_cours_fk_personnel_id_fkey; Type: FK CONSTRAINT; Schema: viesco; Owner: postgres
--

ALTER TABLE ONLY rel_personnel_cours
    ADD CONSTRAINT rel_personnel_cours_fk_personnel_id_fkey FOREIGN KEY (fk_personnel_id) REFERENCES personnel(personnel_id);


-- Completed on 2016-09-20 10:01:19

--
-- PostgreSQL database dump complete
--

