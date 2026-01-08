"use client";

import React from "react";

export default function StarButtons({ childId }: { childId: string }) {
  const [reason, setReason] = React.useState("");
  const [msg, setMsg] = React.useState<string | null>(null);

  async function apply(delta: 1 | -1) {
    setMsg(null);
    const res = await fetch(`/api/children/${childId}/stars`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ delta, reason }),
    });

    if (res.status === 403) {
      setMsg("Read-only account (viewer).");
      return;
    }
    if (!res.ok) {
      const data = await res.json().catch(() => ({}));
      setMsg(data?.error ?? "Failed");
      return;
    }

    setReason("");
    window.location.reload();
  }

  return (
    <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
      <button onClick={() => apply(1)}>+1 star</button>
      <button onClick={() => apply(-1)}>-1 star</button>
      <input
        value={reason}
        onChange={(e) => setReason(e.target.value)}
        placeholder="Reason (optional)"
        style={{ flex: 1, padding: 10 }}
      />
      {msg && <span style={{ marginLeft: 8, color: "#555" }}>{msg}</span>}
    </div>
  );
}