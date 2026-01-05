import { cookies } from "next/headers";

export async function getAccessToken(): Promise<string | null> {
  const jar = await cookies();
  return jar.get("access_token")?.value ?? null;
}

export async function setAccessToken(token: string) {
  const jar = await cookies();
  jar.set("access_token", token, {
    httpOnly: true,
    sameSite: "lax",
    path: "/",
  });
}

export async function clearAccessToken() {
  const jar = await cookies();
  jar.set("access_token", "", { httpOnly: true, path: "/", maxAge: 0 });
}