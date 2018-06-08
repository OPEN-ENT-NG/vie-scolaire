CREATE OR REPLACE FUNCTION array_sort(anyarray) RETURNS anyarray AS $$
SELECT array_agg(x order by x) FROM unnest($1) x;
$$ LANGUAGE SQL;