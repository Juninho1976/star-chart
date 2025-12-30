CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE family_accounts (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE users (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  email TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  role TEXT NOT NULL,
  family_account_id UUID NULL REFERENCES family_accounts(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE children (
  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  family_account_id UUID NOT NULL REFERENCES family_accounts(id),
  name TEXT NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);