import Link from "next/link";
import { cookies } from "next/headers";
import StarButtons from "./StarButtons";

async function apiFetch(path: string) {
  const jar = await cookies();
  const token = jar.get("access_token")?.value;
  const apiBase = process.env.API_BASE_URL;

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

export default async function ChildPage(props: any) {
  const childId = props?.params?.childId as string;

  const starsResp = await apiFetch(`/api/v1/children/${childId}/stars`);
  const summaryResp = await apiFetch(`/api/v1/children/${childId}/summary`);
  const rewardsResp = await apiFetch(`/api/v1/rewards?childId=${encodeURIComponent(childId)}`);

  if (starsResp.status === 401) {
    return (
      <main style={{ padding: 24, fontFamily: "system-ui" }}>
        <p>
          You’re not logged in. Go to <Link href="/login">Login</Link>.
        </p>
      </main>
    );
  }

  const totalStars = starsResp.data?.totalStars ?? 0;
  const events = starsResp.data?.events ?? [];
  const nextReward = summaryResp.data?.nextReward ?? null;
  const rewards = rewardsResp.data?.rewards ?? [];

  return (
    <main style={{ padding: 24, fontFamily: "system-ui", maxWidth: 900, margin: "0 auto" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Link href="/dashboard">← Back</Link>
        <h1 style={{ margin: 0 }}>Child</h1>
        <span />
      </div>

      <section style={{ marginTop: 16, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
        <div style={{ fontSize: 22, fontWeight: 800 }}>⭐ {totalStars}</div>
        <div style={{ marginTop: 6, color: "#555" }}>
          {nextReward ? (
            <>
              Next: <strong>{nextReward.reward}</strong> at {nextReward.threshold} (
              {nextReward.starsRemaining} to go)
            </>
          ) : (
            <>No next reward set</>
          )}
        </div>

        <div style={{ marginTop: 14 }}>
          {/* client-side, uses BFF POST /api/children/:id/stars */}
          <StarButtons childId={childId} />
        </div>
      </section>

      <section style={{ marginTop: 16, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
        <h2 style={{ marginTop: 0 }}>Recent star events</h2>
        {events.length === 0 ? (
          <p style={{ color: "#777" }}>No events yet.</p>
        ) : (
          <div style={{ display: "grid", gap: 8 }}>
            {events.map((e: any) => (
              <div key={e.id} style={{ padding: 10, border: "1px solid #eee", borderRadius: 10 }}>
                <div style={{ fontWeight: 700 }}>
                  {e.delta === 1 ? "⭐ +1" : "⭐ -1"}{" "}
                  <span style={{ fontWeight: 400, color: "#666" }}>
                    {e.createdAt ? new Date(e.createdAt).toLocaleString() : ""}
                  </span>
                </div>
                {e.reason && <div style={{ color: "#555", marginTop: 4 }}>{e.reason}</div>}
              </div>
            ))}
          </div>
        )}
      </section>

      <section style={{ marginTop: 16, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
        <h2 style={{ marginTop: 0 }}>Rewards</h2>
        {rewards.length === 0 ? (
          <p style={{ color: "#777" }}>No rewards configured for this child (or family).</p>
        ) : (
          <ul style={{ margin: 0, paddingLeft: 18 }}>
            {rewards
              .slice()
              .sort((a: any, b: any) => (a.threshold ?? 0) - (b.threshold ?? 0))
              .map((r: any) => (
                <li key={r.id}>
                  {r.threshold} ⭐ — <strong>{r.reward}</strong>
                </li>
              ))}
          </ul>
        )}
      </section>
    </main>
  );
}