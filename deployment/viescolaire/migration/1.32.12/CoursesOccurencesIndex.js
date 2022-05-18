db.runCommand(
    {
        createIndexes: "courses",
        indexes: [
            {
                key: {
                    "structureId": 1,
                    "endDate": 1,
                    "startDate": 1,
                    "deleted": 1,
                    "theoretical": 1
                },
                background: true,
                name: "courses_occurences_index"
            }
        ]
    });