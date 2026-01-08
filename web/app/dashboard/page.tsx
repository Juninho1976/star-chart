import Link from "next/link";
import { cookies } from "next/headers";
import { CreateFamilyForm, AddChildForm } from "./Forms";

type Child = { id: string; name: string };

async function apiFetch(path: string) {
  const jar = await cookies();
  const token = jar.get("access_token")?.value;
  const apiBase = process.env.API_BASE_URL; // e.g. http://api:8080 (docker compose)

  if (!apiBase) throw new Error("API_BASE_URL not set");

  if (!token) {
    return { status: 401, data: { error: "Not logged in" } };
  }

  const res = await fetch(`${apiBase}${path}`, {
    cache: "no-store",
    headers: { Authorization: `Bearer ${token}` },
  });

  const data = await res.json().catch(() => ({}));
  return { status: res.status, data };
}

export default async function DashboardPage() {
  const familyResp = await apiFetch("/api/v1/family");
  const childrenResp = await apiFetch("/api/v1/children");

  const family = familyResp.data?.family ?? null;
  const children: Child[] = childrenResp.data?.children ?? [];

  const summaries: Record<string, any> = {};
  for (const c of children) {
    const s = await apiFetch(`/api/v1/children/${c.id}/summary`);
    summaries[c.id] = s.data;
  }

  return (
    <main style={{ padding: 24, fontFamily: "system-ui", maxWidth: 900, margin: "0 auto" }}>
      <header style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <div>
          <h1 style={{ margin: 0 }}>Dashboard</h1>
          <p style={{ margin: "6px 0 0", color: "#555" }}>
            {family ? `Family: ${family.name}` : "No family created yet"}
          </p>
        </div>

        <form action="/api/auth/logout" method="post">
          <button type="submit">Logout</button>
        </form>
      </header>

      {familyResp.status === 401 && (
        <section style={{ marginTop: 18, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
          <p style={{ margin: 0 }}>
            You’re not logged in. Go to <Link href="/login">Login</Link>.
          </p>
        </section>
      )}

      {!family && familyResp.status !== 401 && (
        <section style={{ marginTop: 18, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
          <h2 style={{ marginTop: 0 }}>Create your family</h2>
          {/* client form uses BFF POST /api/family */}
          <CreateFamilyForm />
        </section>
      )}

      <section style={{ marginTop: 18, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
        <h2 style={{ marginTop: 0 }}>Children</h2>

        {family && (
          <div style={{ marginBottom: 12 }}>
            {/* client form uses BFF POST /api/children */}
            <AddChildForm />
          </div>
        )}

        {!family && familyResp.status !== 401 && <p style={{ color: "#777" }}>Create a family before adding children.</p>}

        {children.length === 0 ? (
          <p style={{ color: "#777" }}>No children yet.</p>
        ) : (
          <div style={{ display: "grid", gap: 12 }}>
            {children.map((c) => {
              const summary = summaries[c.id] ?? {};
              const totalStars = summary.totalStars ?? 0;
              const nextReward = summary.nextReward ?? null;

              return (
                <div
                  key={c.id}
                  style={{
                    padding: 14,
                    border: "1px solid #e3e3e3",
                    borderRadius: 12,
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center",
                  }}
                >
                  <div>
                    <div style={{ fontSize: 18, fontWeight: 700 }}>{c.name}</div>
                    <div style={{ marginTop: 4, color: "#555" }}>
                      ⭐ {totalStars}{" "}
                      {nextReward ? (
                        <>
                          • Next: <strong>{nextReward.reward}</strong> at {nextReward.threshold} (
                          {nextReward.starsRemaining} to go)
                        </>
                      ) : (
                        <>• No next reward set</>
                      )}
                    </div>
                  </div>

                  <Link href={`/child/${c.id}`}>Open</Link>
                </div>
              );
            })}
          </div>
        )}
      </section>
    </main>
  );
}