-- SCHEMA viesco : Attention à l'internationalisation
INSERT INTO viesco.type_classe (id, libelle) VALUES (1, 'classe');
INSERT INTO viesco.type_classe (id, libelle) VALUES (2, 'groupe');
SELECT setval('viesco.type_classe_id_seq', 2, true);

-- SCHEMA notes : Attention à l'internationalisation
INSERT INTO notes.type_competences (id, nom) VALUES (1, 'Compétence');
INSERT INTO notes.type_competences (id, nom) VALUES (2, 'Connaissances');
SELECT setval('notes.type_competences_id_seq', 2, true);

INSERT INTO notes.etat(id, libelle) VALUES (1,'Créé');
SELECT setval('notes.etat_id_seq', 1, true);


-- Données du référentiels des Compétences/Enseignements. Pour l'instant ce référentiel est en dur, mais il est convenu de le rendre paramétrable par établissement (le modèle ne le permet pas pour l'instant).
-- Attention à l'internationalisation
INSERT INTO viesco.type_sousmatiere (id, libelle) VALUES (1, 'Ecrit');
INSERT INTO viesco.type_sousmatiere (id, libelle) VALUES (2, 'Oral');
INSERT INTO viesco.type_sousmatiere (id, libelle) VALUES (3, 'TP');
SELECT setval('viesco.type_sousmatiere_id_seq', 3, true);

INSERT INTO notes.enseignements (id, nom) VALUES (1, 'Mathématiques');
INSERT INTO notes.enseignements (id, nom) VALUES (2, 'Education Physique et Sportive');
INSERT INTO notes.enseignements (id, nom) VALUES (3, 'Français');
INSERT INTO notes.enseignements (id, nom) VALUES (4, 'Technologie');
INSERT INTO notes.enseignements (id, nom) VALUES (5, 'Sciences de la vie et de la Terre');
INSERT INTO notes.enseignements (id, nom) VALUES (6, 'Langues');
INSERT INTO notes.enseignements (id, nom) VALUES (7, 'Histoires des arts');
INSERT INTO notes.enseignements (id, nom) VALUES (8, 'Histoire et Géographie');
INSERT INTO notes.enseignements (id, nom) VALUES (9, 'Enseignement morale et civique');
INSERT INTO notes.enseignements (id, nom) VALUES (10, 'Education Musicale');
INSERT INTO notes.enseignements (id, nom) VALUES (11, 'Education aux médias');
INSERT INTO notes.enseignements (id, nom) VALUES (12, 'Arts Plastiques');
SELECT setval('notes.enseignements_id_seq', 12, true);

INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (21, 'Comprendre et interpréter des messages et des discours oraux complexes', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (22, 'S''exprimer de façon maitrisée en s''adressant à un auditoire', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (23, 'Participer de façon constructive à des échanges oraux', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (24, 'Percevoir et exploiter les ressources expressives et créatives de la parole', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (25, 'Pratiquer le compte-rendu', NULL, 22, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (26, 'Raconter une histoire', NULL, 22, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (27, 'Exprimer ses sensations, ses sentiments, formuler un avis personnel à propos d''une œuvre ou d''une situation en visant à faire   partager son point de vue', NULL, 22, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (28, ' Interagir avec autrui dans un échange, une conversation, une situation de recherche', NULL, 23, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (29, 'Participer à un débat, exprimer une opinion argumentée et prendre en compte son interlocuteur', NULL, 23, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (30, 'Animer et arbitrer un débat', NULL, 23, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (31, 'Décrire une œuvre d''art en employant un lexique simple adapté.', NULL, 0, 1, 7);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (32, 'Associer une œuvre à une époque et une civilisation à partir des éléments observés.', NULL, 0, 1, 7);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (33, 'Proposer une analyse critique simple et une interprétation d''une œuvre.', NULL, 0, 1, 7);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (34, 'Construire un exposé de quelques minutes sur un petit ensemble d''œuvres ou une problématique artistique.', NULL, 0, 1, 7);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (35, 'Rendre compte de la visite d''un lieu de conservation ou de diffusion artistique ou de la rencontre avec un métier du patrimoine.', NULL, 0, 1, 7);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (1, 'Chercher', NULL, 0, 1, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (2, 'Développer sa motricité et apprendre à s''exprimer avec son corps', NULL, 0, 1, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (3, 'Extraire d''un document les informations utiles, les reformuler, les organiser, les confronter à ses connaissances.', NULL, 1, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (4, 'S''engager dans une démarche scientifique, observer, questionner, manipuler, expérimenter (sur une feuille de papier, avec des objets, à l''aide de logiciels), émettre des hypothèses, chercher des exemples ou des contre-exemples, simplifier ou particulariser une situation, émettre une conjecture.', NULL, 1, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (5, 'Tester, essayer plusieurs pistes de résolution.', NULL, 1, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (6, 'Décomposer un problème en sous-problèmes.', NULL, 1, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (7, 'Modéliser', NULL, 0, 1, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (8, 'Reconnaître des situations de proportionnalité et résoudre les problèmes correspondants.', NULL, 7, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (9, 'Traduire en langage mathématique une situation réelle (par exemple à l''aide d''équations, de fonctions, de configurations géométriques, d''outils statistiques).', NULL, 7, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (10, 'Comprendre et utiliser une simulation numérique ou géométrique.', NULL, 7, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (11, 'Valider ou invalider un modèle, comparer une situation à un modèle connu (par exemple un modèle aléatoire).', NULL, 7, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (12, 'Acquérir des techniques spécifiques pour améliorer son efficience.', NULL, 2, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (13, 'Communiquer des intentions et des émotions avec son corps devant un groupe.', NULL, 2, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (14, 'Verbaliser les émotions et sensations ressenties.', NULL, 2, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (15, 'Utiliser un vocabulaire adapté pour décrire la motricité d''autrui et la sienne.', NULL, 2, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (16, 'S''approprier seul ou à plusieurs par la pratique, les méthodes et outils  pour apprendre', NULL, 0, 1, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (17, 'Préparer-planifier-se représenter une action avant de la réaliser.', NULL, 16, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (18, 'Répéter un geste sportif ou artistique pour le stabiliser et le rendre plus efficace.', NULL, 16, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (19, 'Construire et mettre en œuvre des projets d''apprentissage individuel ou collectif.', NULL, 16, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (20, 'Utiliser des outils numériques pour analyser et évaluer ses actions et celles des autres.', NULL, 16, 2, 2);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (36, 'Expérimenter, produire, créer', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (37, 'Mettre en œuvre un projet', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (38, 'S''exprimer, analyser sa pratique, celle de ses pairs ; établir une relation avec celle des artistes, s''ouvrir à l''altérité', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (39, 'Se repérer dans les domaines liés aux arts plastiques, être sensible aux questions de l''art', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (40, 'Choisir, mobiliser et adapter des langages et des moyens plastiques variés en fonction de leurs effets dans une intention artistique en restant attentif à l''inattendu.', NULL, 36, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (41, 'Concevoir, réaliser, donner à voir des projets artistiques, individuels ou collectifs', NULL, 37, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (42, 'Dire avec un vocabulaire approprié ce que l''on fait, ressent, imagine, observe, analyse ; s''exprimer pour soutenir des intentions artistiques ou une interprétation d''œuvre.', NULL, 38, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (43, 'Représenter', NULL, 0, 1, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (44, 'Choisir et mettre en relation des cadres (numérique, algébrique, géométrique) adaptés pour traiter un problème ou pour étudier un objet mathématique.', NULL, 43, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (45, 'Produire et utiliser plusieurs représentations des nombres.', NULL, 43, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (46, 'Représenter des données sous forme d''une série statistique.', NULL, 43, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (47, 'Utiliser, produire et mettre en relation des représentations de solides (par exemple perspective ou vue de dessus/de dessous) et de situations spatiales (schémas, croquis, maquettes, patrons, figures géométriques, photographies, plans, cartes, courbes de niveau).', NULL, 43, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (48, 'Raisonner', NULL, 0, 1, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (50, 'Résoudre des problèmes impliquant des grandeurs variées (géométriques, physiques, économiques): mobiliser les connaissances nécessaires, analyser et exploiter ses erreurs, mettre à l''essai plusieurs solutions.', NULL, 48, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (51, 'Mener collectivement une investigation en sachant prendre en compte le point de vue d''autrui.', NULL, 48, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (52, 'Démontrer : utiliser un raisonnement logique et des règles établies (propriétés, théorèmes, formules) pour parvenir à une conclusion.', NULL, 48, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (53, 'Fonder et défendre ses jugements en s''appuyant sur des résultats établis et sur sa maîtrise de l''argumentation.', NULL, 48, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (54, 'Calculer', NULL, 0, 1, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (55, 'Communiquer', NULL, 0, 1, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (56, 'Calculer avec des nombres rationnels, de manière exacte ou approchée, en combinant de façon appropriée le calcul mental, le calcul posé et le calcul instrumenté (calculatrice ou logiciel).', NULL, 54, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (57, 'Contrôler la vraisemblance de ses résultats, notamment en estimant des ordres de grandeur ou en utilisant des encadrements.', NULL, 54, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (58, 'Calculer en utilisant le langage algébrique (lettres, symboles, etc.).', NULL, 54, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (59, 'Faire le lien entre le langage naturel et le langage algébrique. Distinguer des spécificités du langage mathématique par rapport à la langue française.', NULL, 55, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (60, 'Expliquer à l''oral ou à l''écrit (sa démarche, son raisonnement, un calcul, un protocole de construction géométrique, un algorithme), comprendre les explications d''un autre et argumenter dans l''échange.', NULL, 55, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (61, 'Vérifier la validité d''une information et distinguer ce qui est objectif et ce qui est subjectif; lire, interpréter, commenter, produire des tableaux, des graphiques, des diagrammes.', NULL, 55, 2, 1);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (63, 'Comprendre le rôle de l''écriture', NULL, 62, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (62, 'Exploiter les principales fonctions de l''écrit', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (64, 'Utiliser l''écrit pour penser et pour apprendre', NULL, 62, 2, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (65, 'Adopter des stratégies et des procédures d''écriture efficaces', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (66, 'Pratiquer l''écriture d''invention', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (67, 'Exploiter des lectures pour enrichir son écrit', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (68, 'Passer du recours intuitif à l''argumentation à un usage plus maitrisé', NULL, 0, 1, 3);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (69, 'Pratiquer des démarches scientifiques et technologiques', NULL, 0, 1, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (70, 'Imaginer, synthétiser, formaliser et respecter une procédure, un protocole.', NULL, 69, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (71, 'Mesurer des grandeurs de manière directe ou indirecte.', NULL, 69, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (72, 'Rechercher des solutions techniques à un problème posé, expliciter ses choix et les communiquer en argumentant.', NULL, 69, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (73, 'Participer à l''organisation et au déroulement de projets.', NULL, 69, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (74, 'Concevoir, créer, réaliser', NULL, 0, 1, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (75, 'Identifier un besoin et énoncer un problème technique, identifier les conditions, contraintes (normes et règlements) et ressources correspondantes.', NULL, 74, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (76, 'Identifier le(s) matériau(x), les flux d''énergie et d''information dans le cadre d''une production technique sur un objet et décrire les transformations qui s''opèrent.', NULL, 74, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (77, 'S''approprier un cahier des charges.', NULL, 74, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (78, 'Associer des solutions techniques à des fonctions.', NULL, 74, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (79, 'Imaginer des solutions en réponse au besoin.', NULL, 74, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (80, 'Réaliser, de manière collaborative, le prototype de tout ou partie d''un objet pour valider une solution.', NULL, 74, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (81, 'Imaginer, concevoir et programmer des applications informatiques nomades.', NULL, 74, 2, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (82, 'S''approprier des outils et des méthodes', NULL, 0, 1, 4);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (83, 'Réagir et dialoguer', NULL, 0, 1, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (84, 'Gérer la communication non verbale élémentaire.', NULL, 83, 2, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (85, 'Épeler des mots familiers.', NULL, 83, 2, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (86, 'Établir un contact social.', NULL, 83, 2, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (87, 'Écouter et comprendre', NULL, 0, 1, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (88, 'Repérer des indices sonores simples.', NULL, 87, 2, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (89, 'Isoler des informations très simples dans un message.', NULL, 87, 2, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (90, 'Comprendre les points essentiels d''un message oral simple.', NULL, 87, 2, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (91, 'Comprendre un message oral pour pouvoir répondre à des besoins concrets ou réaliser une tâche.', NULL, 87, 2, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (92, 'Parler en continu', NULL, 0, 1, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (93, 'Lire', NULL, 0, 1, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (94, 'Écrire et réagir à l''écrit', NULL, 0, 1, 6);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (95, 'Se repérer dans le temps : construire des repères historiques', NULL, 0, 1, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (96, 'Situer un fait dans une époque ou une période donnée.', NULL, 95, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (97, 'Ordonner des faits les uns par rapport aux autres.', NULL, 95, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (98, 'Mettre en relation des faits d''une époque ou d''une période donnée.', NULL, 95, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (99, 'Identifier des continuités et des ruptures chronologiques pour s''approprier la périodisation de l''histoire et pratiquer de conscients allers-retours au sein de la chronologie.', NULL, 95, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (100, 'Se repérer dans l''espace : construire des repères géographiques', NULL, 0, 1, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (101, 'Nommer et localiser les grands repères géographiques.', NULL, 100, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (102, 'Nommer, localiser et caractériser un lieu dans un espace géographique.', NULL, 100, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (103, 'Nommer, localiser et caractériser des espaces plus complexes.', NULL, 100, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (104, 'Situer des lieux et des espaces les uns par rapport aux autres.', NULL, 100, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (105, 'Utiliser des représentations analogiques et numériques des espaces à différentes échelles ainsi que différents modes de projection.', NULL, 100, 2, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (106, 'Raisonner, justifier une démarche et les choix effectués', NULL, 0, 1, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (107, 'S''informer dans le monde du numérique', NULL, 0, 1, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (108, 'Analyser et comprendre un document', NULL, 0, 1, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (109, 'Pratiquer différents langages en histoire et en géographie', NULL, 0, 1, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (110, 'Coopérer et mutualiser', NULL, 0, 1, 8);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (111, 'Réaliser des projets musicaux d''interprétation ou de création', NULL, 0, 1, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (112, 'Définir les caractéristiques musicales d''un projet, puis en assurer la mise en œuvre en mobilisant les ressources adaptées.', NULL, 111, 2, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (113, 'Interpréter un projet devant d''autres élèves et présenter les choix artistiques effectués.', NULL, 111, 2, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (114, 'Écouter, comparer, construire une culture musicale commune', NULL, 0, 1, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (115, 'Analyser des œuvres musicales en utilisant un vocabulaire précis.', NULL, 114, 2, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (116, 'Situer et comparer des musiques de styles proches ou éloignés dans l''espace et/ou dans le temps pour construire des repères techniques et culturels.', NULL, 114, 2, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (117, 'Explorer, imaginer, créer et produire', NULL, 0, 1, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (118, 'Échanger, partager, argumenter et débattre', NULL, 0, 1, 10);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (119, 'Utiliser les médias et les informations de manière autonome', NULL, 0, 1, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (120, 'Utiliser des dictionnaires et encyclopédies sur tous supports.', NULL, 119, 2, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (121, 'Utiliser des documents de vulgarisation scientifique.', NULL, 119, 2, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (122, 'Exploiter le centre de ressources comme outil de recherche de l''information.', NULL, 119, 2, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (123, 'Avoir connaissance du fonds d''ouvrages en langue étrangère ou régionale disponible au CDI et les utiliser régulièrement.', NULL, 119, 2, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (124, 'Se familiariser avec les différents modes d''expression des médias en utilisant leurs canaux de diffusion.', NULL, 119, 2, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (125, 'Exploiter l''information de manière raisonnée', NULL, 0, 1, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (126, 'Utiliser les genres et les outils d''information à disposition adaptés à ses recherches.', NULL, 125, 2, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (127, 'Découvrir comment l''information est indexée et hiérarchisée, comprendre les principaux termes techniques associés.', NULL, 125, 2, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (128, 'Utiliser les médias de manière responsable', NULL, 0, 1, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (129, 'Produire, communiquer, partager des informations', NULL, 0, 1, 11);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (130, 'Expérimenter, produire, créer', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (131, 'Mettre en œuvre un projet', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (132, 'Choisir, mobiliser et adapter des langages et des moyens plastiques variés en fonction de leurs effets dans une intention artistique en restant attentif à l''inattendu.', NULL, 130, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (133, 'S''approprier des questions artistiques en prenant appui sur une pratique artistique et réflexive.', NULL, 130, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (134, 'Recourir à des outils numériques de captation et de réalisation à des fins de création artistique.', NULL, 130, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (135, 'Explorer l''ensemble des champs de la pratique plastique et leurs hybridations, notamment avec les pratiques numériques.', NULL, 130, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (136, 'Concevoir, réaliser, donner à voir des projets artistiques, individuels ou collectifs.', NULL, 131, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (137, 'Mener à terme une production individuelle dans le cadre d''un projet accompagné par le professeur.', NULL, 131, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (138, 'Se repérer dans les étapes de la réalisation d''une production plastique et en anticiper les difficultés éventuelles.', NULL, 131, 2, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (139, 'S''exprimer, analyser sa pratique, celle de ses pairs ; établir une relation avec celle des artistes, s''ouvrir à l''altérité', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (140, 'Se repérer dans les domaines liés aux arts plastiques, être sensible aux questions de l''art', NULL, 0, 1, 12);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (141, 'Pratiquer des démarches scientifiques', NULL, 0, 1, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (142, 'Formuler une question ou un problème scientifique.', NULL, 141, 2, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (143, 'Proposer une ou des hypothèses pour résoudre un problème ou une question. Concevoir des expériences pour la ou les tester.', NULL, 141, 2, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (144, 'Utiliser des instruments d''observation, de mesures et des techniques de préparation et de collecte.', NULL, 141, 2, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (145, 'Interpréter des résultats et en tirer des conclusions.', NULL, 141, 2, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (146, 'Concevoir, créer, réaliser', NULL, 0, 1, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (147, 'Concevoir et mettre en œuvre un protocole expérimental.', NULL, 146, 2, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (148, 'Utiliser des outils et mobiliser des méthodes pour apprendre', NULL, 0, 1, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (149, 'Pratiquer des langages', NULL, 0, 1, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (150, 'Utiliser des outils numériques', NULL, 0, 1, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (151, 'Adopter un comportement éthique et responsable', NULL, 0, 1, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (152, 'Se situer dans l''espace et dans le temps', NULL, 0, 1, 5);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (153, 'La sensibilité : soi et les autres', NULL, 0, 1, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (154, 'Identifier et exprimer en les régulant ses émotions et ses sentiments.', NULL, 153, 2, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (155, 'S''estimer et être capable d''écoute et d''empathie.', NULL, 153, 2, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (156, 'Se sentir membre d''une collectivité.', NULL, 153, 2, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (157, 'Le droit et la règle : des principes pour vivre avec les autres', NULL, 0, 1, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (158, 'Comprendre les raisons de l''obéissance aux règles et à la loi dans une société démocratique.', NULL, 157, 2, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (159, 'Comprendre les principes et les valeurs de la République française et des sociétés démocratiques.', NULL, 157, 2, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (160, 'Le jugement : penser par soi-même et avec les autres', NULL, 0, 1, 9);
INSERT INTO notes.competences (id, nom, description, id_parent, id_type, id_enseignement) VALUES (161, 'L''engagement : agir individuellement et collectivement', NULL, 0, 1, 9);
