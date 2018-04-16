CREATE TABLE IF NOT EXISTS user_selection (
  id UUID PRIMARY KEY,
  organisation_id UUID,
  name VARCHAR(255) NOT NULL,
  data JSONB NOT NULL,
  owner_user_id UUID,
  FOREIGN KEY (organisation_id, owner_user_id) REFERENCES mvp_user
);
