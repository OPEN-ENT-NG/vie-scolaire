SELECT setval('viesco.type_sousmatiere_id_seq', (SELECT MAX(id) from viesco.type_sousmatiere), true);