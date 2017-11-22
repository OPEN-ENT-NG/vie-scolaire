#!/usr/bin/env python
# -*- coding: utf-8 -*-

import argparse
from datetime import datetime
import psycopg2
from psycopg2.extras import Json
from neo4jrestclient.client import GraphDatabase


parser = argparse.ArgumentParser()
parser.add_argument('-a','--neo_adresse',help='Adresse IP de la base de donnees Neo4J',required=True)
parser.add_argument('-p','--neo_port',help='Numero de port de la base de donnees Neo4J',required=True)
parser.add_argument('-A','--sql_adresse',help='Adresse de la base de donnees PostgreSQL',required=True)
parser.add_argument('-P','--sql_port',help='Numero de port de la base de donnees PostgreSQL',required=True)
parser.add_argument('-DB','--sql_db',help='Nom de la base de données PostgreSQL',required=True)
parser.add_argument('-u','--sql_user',help='Nom de compte de la base de donnees PostgreSQL',required=True)
group = parser.add_mutually_exclusive_group(required=True)
group.add_argument('-pw','--sql_password',help='Nom de la base de données PostgreSQL')
group.add_argument('--no-password',help='Mot de passe du compte de la base données PostgreSQL', action='store_true')
args = parser.parse_args()

gdb = GraphDatabase(str("http://" + args.neo_adresse + ":" + args.neo_port + "/db/data/"))

if args.no_password:
    sql = psycopg2.connect(database=args.sql_db, host=args.sql_adresse, user=args.sql_user)
else:
    sql = psycopg2.connect(database=args.sql_db, host=args.sql_adresse, user=args.sql_user, password=args.sql_password)
cur = sql.cursor()
dict_cur = sql.cursor(cursor_factory=psycopg2.extras.DictCursor)

def getEtabList():
    print('(' + str(datetime.now()) + ') [Start] Get all etab')
    cur.execute("""SELECT array_agg(DISTINCT id_etablissement) FROM viesco.periode""")
    print('(' + str(datetime.now()) + ') [Done] Get all etab')
    return cur.fetchall()[0][0]

def importClasses(id_etablissement):
    print('(' + str(datetime.now()) + ') [Start] Get all classes')
    query = """MATCH (c:Class)--(s:Structure) WHERE s.id IN {id_etablissement} RETURN {ids_classe: collect(c.id), id_etablissement: s.id}"""
    rows = gdb.query(q=query, params={'id_etablissement': id_etablissement})
    idClasseByEtab = {}
    for row in rows:
        idClasseByEtab[row[0]['id_etablissement']] = row[0]['ids_classe']
    print('(' + str(datetime.now()) + ') [Done] Get all classes')
    return idClasseByEtab

def createTable_classe_etab_temp(idClasseByEtab):
    print('(' + str(datetime.now()) + ') [Start] Create temp table classe etab')
    cur.execute(""" CREATE TEMPORARY TABLE rel_classe_etab
                   (
                      id_classe character varying(36) NOT NULL,
                      id_etab character varying(36) NOT NULL
                   ) 
                   ON COMMIT DELETE ROWS;""")
    for key, values in idClasseByEtab.items():
        cur.execute("""INSERT INTO rel_classe_etab (id_classe, id_etab)
                      VALUES (unnest(%s), %s);""", (values, key))
    print('(' + str(datetime.now()) + ') [Done] Create temp table classe etab')

def insertTypePeriode():
    print('(' + str(datetime.now()) + ') [Start] Insert type periode')
    cur.execute("""CREATE TABLE viesco.rel_type_periode(
                    id BIGSERIAL NOT NULL,
                    type bigint NOT NULL,
                    ordre bigint NOT NULL,
                    PRIMARY KEY (id),
                    CONSTRAINT id_unique UNIQUE (id),
                    CONSTRAINT type_ordre_unique UNIQUE (type, ordre)
                  );

                  INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (2, 1);
                  INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (2, 2);
                  INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (3, 1);
                  INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (3, 2);
                  INSERT INTO viesco.rel_type_periode (type, ordre) VALUES (3, 3);
                  """)
    print('(' + str(datetime.now()) + ') [Done] Insert type periode')

def addColumnIdClasse():
    print('(' + str(datetime.now()) + ') [Start] Add new column id_classe to periode')
    cur.execute("""ALTER TABLE viesco.periode 
                    ADD COLUMN id_classe character varying(36),
                    ADD COLUMN id_type BIGINT,
                    ADD CONSTRAINT fk_type_periode FOREIGN KEY (id_type)
                    REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE
                    ON UPDATE NO ACTION ON DELETE NO ACTION; 
                    """)
    print('(' + str(datetime.now()) + ') [Done] Add new column id_classe to periode')

def setTypePeriode():
    print('(' + str(datetime.now()) + ') [Start] Set type periode ')
    cur.execute("""UPDATE viesco.periode 
                    SET id_type = rel_type_periode.id
                    FROM viesco.rel_type_periode
                    WHERE rel_type_periode.ordre = CAST (regexp_replace(periode.libelle, '([0-9])[^0-9]*', '\\1') AS BIGINT)
                    AND rel_type_periode.type = CAST (3 AS BIGINT)
                      """)
    print('(' + str(datetime.now()) + ') [Start] Set type periode ')

def fillTablePeriode():
    print('(' + str(datetime.now()) + ') [Done] Fill table periode')
    cur.execute("""INSERT INTO viesco.periode
                (id_classe, id_etablissement, id_type, timestamp_dt, timestamp_fn, date_fin_saisie)
                SELECT rel_classe_etab.id_classe, rel_classe_etab.id_etab, periodes.id_type, 
                periodes.timestamp_dt, periodes.timestamp_fn, periodes.date_fin_saisie
                FROM rel_classe_etab
                LEFT JOIN (SELECT periode.id_etablissement, periode.timestamp_dt, 
                            periode.timestamp_fn, periode.date_fin_saisie, periode.id_type
                            FROM viesco.periode
                            ORDER BY periode.id_type
                            ) as periodes
                ON periodes.id_etablissement = rel_classe_etab.id_etab
                """)
    print('(' + str(datetime.now()) + ') [Done] Fill table periode')

def updateFkPeriodeDevoirs():
    print('(' + str(datetime.now()) + ') [Start] Update Fk devoirs')
    cur.execute("""ALTER TABLE notes.devoirs DROP CONSTRAINT fk_periode_id;
                    UPDATE notes.devoirs as devoirs
                    SET id_periode = periode.id_type
                    FROM viesco.periode
                    WHERE devoirs.id_periode = periode.id;
                    ALTER TABLE notes.devoirs ADD CONSTRAINT fk_devoirs_type_periode FOREIGN KEY (id_periode)
                    REFERENCES viesco.rel_type_periode (id) MATCH SIMPLE
                    ON UPDATE NO ACTION ON DELETE NO ACTION;""")
    print('(' + str(datetime.now()) + ') [Done] Update Fk devoirs')

def cleanTablePeriode():
    print('(' + str(datetime.now()) + ') [Start] Clean table periode')
    cur.execute("""ALTER TABLE viesco.periode ALTER COLUMN id_type SET NOT NULL; 
                INSERT INTO viesco.scripts (filename, passed) VALUES (%s, %s);""",
                ('008-alterVieSco-periodes.sql', datetime.now()))
    print('(' + str(datetime.now()) + ') [Done] Clean table periode')

# PROCESS #
###########

createTable_classe_etab_temp(importClasses(getEtabList()))
insertTypePeriode()
addColumnIdClasse()
setTypePeriode()
fillTablePeriode()
updateFkPeriodeDevoirs()
cleanTablePeriode()


cur.close()
dict_cur.close()
sql.commit()
sql.close()