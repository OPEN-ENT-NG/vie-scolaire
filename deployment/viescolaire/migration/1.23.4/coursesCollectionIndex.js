db.runCommand(
    {
        createIndexes: "courses",
        indexes: [
            {
                key: {
                    "structureId": 1,
                    "startDate": 1,
                    "endDate": 1,
                },
                background: true,
                name: "courses_fields_index"
            },
            {
                key: {
                    "teacherIds": 1
                },
                background: true,
                name: "courses_teacherIds_index"
            },
            {
                key: {
                    "groups": 1
                },
                background: true,
                name: "courses_groups_index"
            },
            {
                key: {
                    "classes": 1
                },
                background: true,
                name: "courses_classes_index"
            },
        ]
    }
)
