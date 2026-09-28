"use client";

import { PleaseBookMeProvider } from "../../../pleasebookme/core/auth/PleaseBookMeProvider";
import { widgetForEcosystem } from "../../../pleasebookme/ecosystems/registry";

const Widget = widgetForEcosystem("BARBERSHOP")!;

export default function Page() {
  const apiUrl = process.env.NEXT_PUBLIC_PBM_API_URL;
  const publicKey = process.env.NEXT_PUBLIC_PBM_PUBLIC_KEY;
  const secretKey = process.env.NEXT_PUBLIC_PBM_SECRET_KEY;

  if (!apiUrl || !publicKey || !secretKey) {
    return (
      <main className="flex flex-1 flex-col items-center justify-center p-xl text-center">
        <div className="w-full max-w-[42rem]">
          <h1 className="text-headline-md">Configure the widget harness</h1>

          <p className="mt-sm text-body-md text-muted-foreground">
            Add the three values below to this project&rsquo;s <code>.env.local</code>, then
            restart the dev server. They come from a widget created in the dashboard.
          </p>

          <pre className="mt-lg overflow-x-auto rounded-lg border border-border bg-surface p-md text-left text-body-md">
          {
            `NEXT_PUBLIC_PBM_API_URL=http://localhost:8080
            NEXT_PUBLIC_PBM_PUBLIC_KEY=
            NEXT_PUBLIC_PBM_SECRET_KEY=`
          }
          </pre>
        </div>
      </main>
    );
  }

  return (
    <PleaseBookMeProvider apiUrl={apiUrl} publicKey={publicKey} secretKey={secretKey}>
      <Widget />
    </PleaseBookMeProvider>
  );
}
