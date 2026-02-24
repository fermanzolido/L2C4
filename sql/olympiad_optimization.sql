-- Optimization indices for Olympiad system
-- These indices are designed to speed up the most frequent queries in Olympiad.java

-- Optimizing queries filtering by class_id and sorting by points/wins
-- Used in: OLYMPIAD_GET_HEROS, GET_EACH_CLASS_LEADER_CURRENT
CREATE INDEX idx_olympiad_nobles_class_rank ON olympiad_nobles (class_id, olympiad_points, competitions_done, competitions_won);

-- Optimizing queries filtering by class_id and sorting by points/wins for EOM table
-- Used in: GET_EACH_CLASS_LEADER, GET_EACH_CLASS_LEADER_SOULHOUND
CREATE INDEX idx_olympiad_nobles_eom_class_rank ON olympiad_nobles_eom (class_id, olympiad_points, competitions_done, competitions_won);

-- Optimizing queries sorting all nobles by points/wins (without class filter)
-- Used in: GET_ALL_CLASSIFIED_NOBLESS
CREATE INDEX idx_olympiad_nobles_eom_rank ON olympiad_nobles_eom (olympiad_points, competitions_done, competitions_won);
