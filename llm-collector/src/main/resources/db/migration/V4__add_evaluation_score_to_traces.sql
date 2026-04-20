ALTER TABLE traces
ADD COLUMN IF NOT EXISTS evaluation_score DOUBLE PRECISION;

UPDATE traces
SET evaluation_score = quality_score
WHERE evaluation_score IS NULL
  AND quality_score IS NOT NULL;
