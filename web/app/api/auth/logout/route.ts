import { NextResponse } from "next/server";
import { clearAccessToken } from "../../_lib/auth";

export async function POST() {
  clearAccessToken();
  return NextResponse.redirect(new URL("/", "http://localhost:3000"));
}