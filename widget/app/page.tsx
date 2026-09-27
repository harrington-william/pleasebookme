import Link from "next/link";

export default function Page() {
  return (
    <main className="mx-auto w-full max-w-3xl p-xl">
      <h1 className="text-headline-lg">PleaseBookMe widgets</h1>
      <p className="mt-xs text-body-lg text-muted-foreground">
        Choose an ecosystem and presentation to preview.
      </p>
      <Link
        href="/barbershop/kinetic"
        className="mt-xl block cursor-pointer rounded-xl border border-border bg-surface p-lg shadow-sm transition-colors hover:bg-surface-hover"
      >
        <h2 className="text-headline-sm">Barbershop · Kinetic</h2>
        <p className="mt-xs text-body-md text-muted-foreground">
          Six-step appointment booking, also used for General services.
        </p>
      </Link>
    </main>
  );
}
