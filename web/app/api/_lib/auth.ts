import { cookies } from "next/headers";

export function getAccessToken(): string | null {
  return cookies().get("access_token")?.value ?? null;
}

export function setAccessToken(token: string) {
  cookies().set("access_token", token, {
    httpOnly: true,
    sameSite: "lax",
    path: "/"
  });
}

export function clearAccessToken() {
  cookies().set("access_token", "", { httpOnly: true, path: "/", maxAge: 0 });
}