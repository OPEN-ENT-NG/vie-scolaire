ALTER TABLE presences.punishment
drop CONSTRAINT fk_incident_id;
ALTER TABLE presences.punishment
drop CONSTRAINT fk_presence_period_id;

ALTER TABLE presences.punishment
  ADD CONSTRAINT fk_incident_id
  FOREIGN KEY (incident_id)
  REFERENCES presences.incident(id) ON DELETE SET NULL;

ALTER TABLE presences.punishment
  ADD CONSTRAINT fk_presence_period_id
  FOREIGN KEY (presence_period_id)
  REFERENCES presences.presence_period(id) ON DELETE SET NULL;