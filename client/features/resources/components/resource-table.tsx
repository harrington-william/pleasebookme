import { ResourceStatusBadge } from "@/features/resources/components/resource-status-badge";
import type { ResourceListEntry } from "@/features/resources/types/resource";

export function ResourceTable({
  entries,
  filtered = false,
}: {
  entries: ResourceListEntry[];
  filtered?: boolean;
}) {
  if (entries.length === 0) {
    return (
      <div className="rounded-xl border border-dashed border-border px-md py-2xl text-center">
        <p className="text-body-md text-muted-foreground">
          {filtered
            ? "No resources match these filters. Try clearing them."
            : "No resources yet. Create one to start assigning assets to services."}
        </p>
      </div>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-border bg-surface">
      <table className="w-full min-w-3xl border-collapse text-left">
        <thead>
          <tr className="border-b border-border text-label-md tracking-wider text-muted-foreground uppercase">
            <th className="px-md py-sm font-medium">Resource</th>
            <th className="px-md py-sm font-medium">Type</th>
            <th className="px-md py-sm font-medium">Capacity</th>
            <th className="px-md py-sm font-medium">Status</th>
            <th className="px-md py-sm font-medium">Assigned Services</th>
          </tr>
        </thead>
        <tbody>
          {entries.map((entry) => (
            <tr
              key={entry.resource.resourceId}
              className="border-b border-border last:border-b-0"
            >
              <td className="px-md py-sm">
                <p className="text-body-md font-medium text-foreground">
                  {entry.resource.name}
                </p>
                <p className="font-mono text-mono-label text-muted-foreground">
                  {entry.resource.slug}
                </p>
              </td>
              <td className="px-md py-sm text-body-md text-foreground">
                {entry.resourceTypeName}
              </td>
              <td className="px-md py-sm font-mono text-mono-label text-foreground">
                {entry.resource.capacity ?? "N/A"}
              </td>
              <td className="px-md py-sm">
                <ResourceStatusBadge status={entry.resource.status} />
              </td>
              <td className="px-md py-sm text-body-md text-muted-foreground">
                {entry.assignedServiceNames.length > 0
                  ? entry.assignedServiceNames.join(", ")
                  : "None"}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
