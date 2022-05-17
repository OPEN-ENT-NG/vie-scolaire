db.runCommand(
    {
        createIndexes: "courses",
        indexes: [
            {
                key: {
                    "structureId": 1,
                    "tagIds": 1
                },
                background: true,
                name: "courses_tags_index"
            }
        ]
    });