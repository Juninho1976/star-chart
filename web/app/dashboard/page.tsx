import { cookies } from "next/headers";

export default async function DashboardPage() {
  const cookieHeader = cookies().toString();

  const familyRes = await fetch("http://localhost:3000/api/family", {
    cache: "no-store",
    headers: { cookie: cookieHeader },
  });
  const family = await familyRes.json().catch(() => ({}));

  const childrenRes = await fetch("http://localhost:3000/api/children", {
    cache: "no-store",
    headers: { cookie: cookieHeader },
  });
  const children = await childrenRes.json().catch(() => ({}));

  return (
    <main style={{ padding: 24, fontFamily: "system-ui" }}>
      <h1>Dashboard</h1>

      <section style={{ marginTop: 16 }}>
        <h2>Family</h2>
        <pre>{JSON.stringify(family, null, 2)}</pre>
        <p>Create family (POST) via curl for now (we’ll add UI next):</p>
        <code>{`curl -X POST http://localhost:8080/api/v1/family -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" -d '{"name":"Kotecha Family"}'`}</code>
      </section>

      <section style={{ marginTop: 16 }}>
        <h2>Children</h2>
        <pre>{JSON.stringify(children, null, 2)}</pre>
        <p>Add child (POST) via curl for now (we’ll add UI next):</p>
        <code>{`curl -X POST http://localhost:8080/api/v1/children -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" -d '{"name":"Arya"}'`}</code>
      </section>

      <section style={{ marginTop: 16 }}>
        <form action="/api/auth/logout" method="post">
          <button type="submit">Logout</button>
        </form>
      </section>
    </main>
  );
}