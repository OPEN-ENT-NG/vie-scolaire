db.runCommand(
    {
        createIndexes: "courses",
        indexes: [
            {
                key: {
                    "recurrence": 1,
                    "deleted": 1,
                    "theoretical": 1
                },
                background: true,
                name: "courses_recurrence_index"
            }
        ]
    });