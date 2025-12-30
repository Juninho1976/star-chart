import Link from "next/link";

export default function Home() {
  return (
    <main style={{ padding: 24, fontFamily: "system-ui" }}>
      <h1>Star Chart</h1>
      <p>Simple v0: register/login, create family, add children.</p>
      <div style={{ display: "flex", gap: 12 }}>
        <Link href="/register">Register</Link>
        <Link href="/login">Login</Link>
        <Link href="/dashboard">Dashboard</Link>
      </div>
    </main>
  );
}