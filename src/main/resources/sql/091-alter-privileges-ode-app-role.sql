GRANT USAGE ON SCHEMA viesco TO "apps";
GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON ALL TABLES IN SCHEMA viesco TO "apps";
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA viesco TO "apps";
GRANT USAGE, SELECT, UPDATE ON ALL SEQUENCES IN SCHEMA viesco TO "apps";

ALTER DEFAULT PRIVILEGES IN SCHEMA viesco GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON TABLES TO "apps";
ALTER DEFAULT PRIVILEGES IN SCHEMA viesco GRANT EXECUTE ON FUNCTIONS TO "apps";
ALTER DEFAULT PRIVILEGES IN SCHEMA viesco GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO "apps";

ALTER DEFAULT PRIVILEGES IN SCHEMA memento GRANT SELECT, INSERT, UPDATE, DELETE, TRUNCATE ON TABLES TO "apps";
ALTER DEFAULT PRIVILEGES IN SCHEMA memento GRANT EXECUTE ON FUNCTIONS TO "apps";
ALTER DEFAULT PRIVILEGES IN SCHEMA memento GRANT USAGE, SELECT, UPDATE ON SEQUENCES TO "apps";
