import { NextResponse } from "next/server";
import { getAccessToken } from "../../../_lib/auth";

export async function GET(_req: Request, context: any) {
  const childId = context?.params?.childId as string;
  const apiBase = process.env.API_BASE_URL!;
  const token = await getAccessToken();
  if (!token) return NextResponse.json({ error: "Not logged in" }, { status: 401 });

  const res = await fetch(`${apiBase}/api/v1/children/${childId}/summary`, {
    headers: { Authorization: `Bearer ${token}` },
    cache: "no-store",
  });

  const data = await res.json().catch(() => ({}));
  return NextResponse.json(data, { status: res.status });
}