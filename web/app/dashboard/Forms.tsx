"use client";

import React from "react";

export function CreateFamilyForm() {
  const [name, setName] = React.useState("");
  const [msg, setMsg] = React.useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setMsg(null);

    const res = await fetch("/api/family", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name }),
    });

    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      setMsg(data?.error ?? "Failed to create family");
      return;
    }

    setMsg("Family created ✅");
    window.location.reload();
  }

  return (
    <form onSubmit={submit} style={{ display: "flex", gap: 10, alignItems: "center" }}>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="e.g. Kotecha Family"
        style={{ flex: 1, padding: 10 }}
      />
      <button type="submit" disabled={!name.trim()}>
        Create
      </button>
      {msg && <span style={{ marginLeft: 8, color: "#555" }}>{msg}</span>}
    </form>
  );
}

export function AddChildForm() {
  const [name, setName] = React.useState("");
  const [msg, setMsg] = React.useState<string | null>(null);

  async function submit(e: React.FormEvent) {
    e.preventDefault();
    setMsg(null);

    const res = await fetch("/api/children", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ name }),
    });

    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      setMsg(data?.error ?? "Failed to add child");
      return;
    }

    setMsg("Added ✅");
    window.location.reload();
  }

  return (
    <form onSubmit={submit} style={{ display: "flex", gap: 10, alignItems: "center" }}>
      <input
        value={name}
        onChange={(e) => setName(e.target.value)}
        placeholder="Child name"
        style={{ flex: 1, padding: 10 }}
      />
      <button type="submit" disabled={!name.trim()}>
        Add
      </button>
      {msg && <span style={{ marginLeft: 8, color: "#555" }}>{msg}</span>}
    </form>
  );
}