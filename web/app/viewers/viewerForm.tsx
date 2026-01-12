"use client";

import React from "react";

export default function ViewerForm() {
  const [email, setEmail] = React.useState("");
  const [password, setPassword] = React.useState("");
  const [msg, setMsg] = React.useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setMsg(null);

    const res = await fetch("/api/viewers", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email, password }),
    });

    const data = await res.json().catch(() => ({}));

    if (res.status === 403) {
      setMsg("This account is read-only (viewer).");
      return;
    }
    if (!res.ok) {
      setMsg(data?.error ?? "Failed to create viewer");
      return;
    }

    setEmail("");
    setPassword("");
    setMsg("Viewer created ✅");
    window.location.reload();
  }

  return (
    <form onSubmit={submit} style={{ display: "grid", gap: 10, maxWidth: 420 }}>
      <input
        value={email}
        onChange={(e) => setEmail(e.target.value)}
        placeholder="viewer email"
        style={{ padding: 10 }}
      />
      <input
        value={password}
        onChange={(e) => setPassword(e.target.value)}
        placeholder="temporary password"
        type="password"
        style={{ padding: 10 }}
      />
      <button type="submit" disabled={!email.trim() || !password.trim()}>
        Create viewer
      </button>
      {msg && <div style={{ color: "#555" }}>{msg}</div>}
    </form>
  );
}