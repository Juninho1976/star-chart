create table if not exists reward_rule (
  id uuid primary key,
  family_account_id uuid not null,
  child_id uuid null,
  threshold int not null,
  reward text not null,
  active boolean not null default true,
  created_at timestamptz not null default now(),

  constraint chk_reward_threshold check (threshold > 0),
  constraint fk_reward_child foreign key (child_id) references children(id) on delete cascade
);

create index if not exists idx_reward_family on reward_rule(family_account_id);
create index if not exists idx_reward_child on reward_rule(child_id);
create index if not exists idx_reward_threshold on reward_rule(threshold);