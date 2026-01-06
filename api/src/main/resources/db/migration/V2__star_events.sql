create table if not exists star_event (
  id uuid primary key,
  family_account_id uuid not null,
  child_id uuid not null,
  created_by_user_id uuid not null,
  delta int not null,
  reason text,
  created_at timestamptz not null default now(),

  constraint chk_star_event_delta check (delta in (1, -1)),
  constraint fk_star_event_child
    foreign key (child_id) references children(id) on delete cascade
);

create index if not exists idx_star_event_child_created_at
  on star_event(child_id, created_at desc);

create index if not exists idx_star_event_family
  on star_event(family_account_id);