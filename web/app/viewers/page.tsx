import Link from "next/link";
import { cookies } from "next/headers";
import ViewerForm from "./viewerForm";

type Viewer = { id: string; email: string };

async function apiFetch(path: string) {
  const jar = await cookies();
  const token = jar.get("access_token")?.value;
  const apiBase = process.env.API_BASE_URL;

  if (!apiBase) throw new Error("API_BASE_URL not set");

  if (!token) return { status: 401, data: { error: "Not logged in" } };

  const res = await fetch(`${apiBase}${path}`, {
    cache: "no-store",
    headers: { Authorization: `Bearer ${token}` },
  });

  const data = await res.json().catch(() => ({}));
  return { status: res.status, data };
}

export default async function ViewersPage() {
  const resp = await apiFetch("/api/v1/viewers");
  const viewers: Viewer[] = resp.data?.viewers ?? [];

  return (
    <main style={{ padding: 24, fontFamily: "system-ui", maxWidth: 900, margin: "0 auto" }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
        <Link href="/dashboard">← Back</Link>
        <h1 style={{ margin: 0 }}>Viewer accounts</h1>
        <span />
      </div>

      {resp.status === 401 && (
        <section style={{ marginTop: 16, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
          <p style={{ margin: 0 }}>
            You’re not logged in. Go to <Link href="/login">Login</Link>.
          </p>
        </section>
      )}

      {resp.status === 403 && (
        <section style={{ marginTop: 16, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
          <p style={{ margin: 0 }}>This account is read-only (viewer). Viewer management is parent-only.</p>
        </section>
      )}

      {resp.status === 200 && (
        <>
          <section style={{ marginTop: 16, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
            <h2 style={{ marginTop: 0 }}>Create a viewer</h2>
            <ViewerForm />
            <p style={{ marginTop: 10, color: "#666" }}>
              Viewers can log in and see stars/rewards, but can’t add or change anything.
            </p>
          </section>

          <section style={{ marginTop: 16, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
            <h2 style={{ marginTop: 0 }}>Existing viewers</h2>
            {viewers.length === 0 ? (
              <p style={{ color: "#777" }}>No viewers yet.</p>
            ) : (
              <ul style={{ margin: 0, paddingLeft: 18 }}>
                {viewers.map((v) => (
                  <li key={v.id}>{v.email}</li>
                ))}
              </ul>
            )}
          </section>
        </>
      )}
    </main>
  );
}