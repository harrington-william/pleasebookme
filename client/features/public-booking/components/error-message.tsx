export function ErrorMessage({ message }: { message: string }) {
  return (
    <p className="rounded-lg border border-destructive/40 bg-destructive/10 p-md text-body-md text-destructive">
      {message}
    </p>
  );
}
