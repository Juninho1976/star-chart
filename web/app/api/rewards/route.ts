import { NextResponse } from "next/server";
import { getAccessToken } from "../_lib/auth";

export async function GET(req: Request) {
  const apiBase = process.env.API_BASE_URL!;
  const token = await getAccessToken();
  if (!token) return NextResponse.json({ error: "Not logged in" }, { status: 401 });

  const url = new URL(req.url);
  const childId = url.searchParams.get("childId");

  const target = childId
    ? `${apiBase}/api/v1/rewards?childId=${encodeURIComponent(childId)}`
    : `${apiBase}/api/v1/rewards`;

  const res = await fetch(target, {
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store",
  });

  const data = await res.json().catch(() => ({}));
  return NextResponse.json(data, { status: res.status });
}

import { getAccessToken } from "../_lib/auth";
import { NextResponse } from "next/server";

export async function POST(req: Request) {
  const apiBase = process.env.API_BASE_URL!;
  const token = await getAccessToken();
  if (!token) return NextResponse.json({ error: "Not logged in" }, { status: 401 });

  const body = await req.json();
  const res = await fetch(`${apiBase}/api/v1/rewards`, {
    method: "POST",
    headers: { "Content-Type": "application/json", Authorization: `Bearer ${token}` },
    body: JSON.stringify(body),
  });

  const data = await res.json().catch(() => ({}));
  return NextResponse.json(data, { status: res.status });
}