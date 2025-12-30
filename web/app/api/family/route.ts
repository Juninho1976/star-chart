import { NextResponse } from "next/server";
import { getAccessToken } from "../_lib/auth";

export async function GET() {
  const apiBase = process.env.API_BASE_URL!;
  const token = getAccessToken();
  if (!token) return NextResponse.json({ error: "Not logged in" }, { status: 401 });

  const res = await fetch(`${apiBase}/api/v1/family`, {
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store"
  });

  const data = await res.json().catch(() => ({}));
  return NextResponse.json(data, { status: res.status });
}

export async function POST(req: Request) {
  const apiBase = process.env.API_BASE_URL!;
  const token = getAccessToken();
  if (!token) return NextResponse.json({ error: "Not logged in" }, { status: 401 });

  const body = await req.json();
  const res = await fetch(`${apiBase}/api/v1/family`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify(body)
  });

  const data = await res.json().catch(() => ({}));
  return NextResponse.json(data, { status: res.status });
}