"use client";

import { useEffect, useState } from "react";

type Reward = {
  id: string;
  threshold: number;
  reward: string;
  childId?: string | null;
};

export default function Rewards() {
  const [rewards, setRewards] = useState<Reward[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [threshold, setThreshold] = useState<number>(5);
  const [rewardText, setRewardText] = useState<string>("");
  const [msg, setMsg] = useState<string | null>(null);

  async function load() {
    setError(null);
    const r = await fetch("/api/rewards", { cache: "no-store" });
    const d = await r.json().catch(() => ({}));
    setRewards(d.rewards ?? []);
  }

  useEffect(() => {
    load().catch(() => setError("Failed to load rewards"));
  }, []);

  async function addReward(e: React.FormEvent) {
    e.preventDefault();
    setMsg(null);
    setError(null);

    const res = await fetch("/api/rewards", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ threshold, reward: rewardText }),
    });

    const data = await res.json().catch(() => ({}));

    if (res.status === 403) {
      setMsg("Viewer mode: cannot add rewards.");
      return;
    }
    if (!res.ok) {
      setError(data?.error ?? "Failed to add reward");
      return;
    }

    setRewardText("");
    setMsg("Reward added ✅");
    await load();
  }

  return (
    <section style={{ marginTop: 18, padding: 16, border: "1px solid #ddd", borderRadius: 12 }}>
      <h2 style={{ marginTop: 0 }}>Rewards</h2>

      <form onSubmit={addReward} style={{ display: "flex", gap: 10, alignItems: "center", marginBottom: 12 }}>
        <input
          type="number"
          min={1}
          value={threshold}
          onChange={(e) => setThreshold(parseInt(e.target.value || "1", 10))}
          style={{ width: 110, padding: 10 }}
          placeholder="Stars"
        />
        <input
          value={rewardText}
          onChange={(e) => setRewardText(e.target.value)}
          style={{ flex: 1, padding: 10 }}
          placeholder="Reward (e.g. Ice cream)"
        />
        <button type="submit" disabled={!rewardText.trim()}>
          Add
        </button>
      </form>

      {msg && <p style={{ marginTop: 0, color: "#555" }}>{msg}</p>}
      {error && <p style={{ marginTop: 0, color: "red" }}>{error}</p>}

      {rewards.length === 0 ? (
        <p style={{ color: "#777" }}>No rewards configured yet.</p>
      ) : (
        <ul style={{ margin: 0, paddingLeft: 18 }}>
          {rewards
            .slice()
            .sort((a, b) => a.threshold - b.threshold)
            .map((r) => (
              <li key={r.id}>
                ⭐ {r.threshold} — <strong>{r.reward}</strong>
              </li>
            ))}
        </ul>
      )}
    </section>
  );
}