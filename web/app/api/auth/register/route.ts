import { NextResponse } from "next/server";
import { setAccessToken } from "../../_lib/auth";

export async function POST(req: Request) {
  const body = await req.json();
  const apiBase = process.env.API_BASE_URL!;

  const res = await fetch(`${apiBase}/api/v1/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body)
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) return NextResponse.json(data, { status: res.status });

  setAccessToken(data.accessToken);
  return NextResponse.json({ ok: true });
}