"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { LoaderCircle } from "lucide-react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";

import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { AuthFormAlert } from "@/features/auth/components/auth-form-alert";
import {
  createResourceFormSchema,
  DEFAULT_CREATE_RESOURCE_VALUES,
  slugifyResourceName,
  type CreateResourceFormValues,
} from "@/features/resources/schemas/resource-schema";
import { createResource } from "@/features/resources/services/resource-api";
import {
  RESOURCE_STATUSES,
  type ResourceType,
} from "@/features/resources/types/resource";
import { ApiRequestError } from "@/lib/api-error";
import { cn } from "@/lib/utils";

const fieldClassName =
  "h-9 w-full rounded-lg border-border bg-background px-md text-body-md";

const labelClassName =
  "mb-sm text-label-md tracking-wider text-muted-foreground uppercase";

export function CreateResourceForm({
  resourceTypes,
}: {
  resourceTypes: ResourceType[];
}) {
  const router = useRouter();
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [slugEdited, setSlugEdited] = useState(false);

  const {
    control,
    register,
    handleSubmit,
    setError,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<CreateResourceFormValues>({
    resolver: zodResolver(createResourceFormSchema),
    mode: "onBlur",
    defaultValues: DEFAULT_CREATE_RESOURCE_VALUES,
  });

  if (resourceTypes.length === 0) {
    return (
      <section className="rounded-xl border border-dashed border-border bg-surface p-lg text-left">
        <h2 className="text-headline-md text-foreground">
          Create a resource type first
        </h2>

        <p className="mx-auto mt-xs text-body-md text-left text-muted-foreground">
          Every resource needs a type, and this organization does not have one
          yet. Add a type such as Room, Chair, or Vehicle before creating the
          resource.
        </p>

        <Link
          href="/dashboard/resources/types/new"
          className="mt-xl inline-flex rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90"
        >
          Create Resource Type
        </Link>
      </section>
    );
  }

  const nameField = register("name");
  const slugField = register("slug");

  async function onSubmit(values: CreateResourceFormValues) {
    setSubmitError(null);

    try {
      await createResource(values);
      router.push("/dashboard/resources");
      router.refresh();
    } catch (error) {
      if (error instanceof ApiRequestError && error.detail.status === 409) {
        setError(
          "slug",
          {
            type: "server",
            message: "A resource with this slug already exists.",
          },
          { shouldFocus: true }
        );
        return;
      }

      setSubmitError(
        error instanceof ApiRequestError
          ? error.message
          : "Could not create this resource. Please try again."
      );
    }
  }

  return (
    <form
      onSubmit={handleSubmit(onSubmit)}
      noValidate
      className="space-y-lg"
    >
      {submitError ? <AuthFormAlert message={submitError} /> : null}

      <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
        <h2 className="text-headline-md text-foreground">
          Basic Information
        </h2>

        <div className="space-y-base">
          <Label htmlFor="name" className={labelClassName}>
            Resource Name
          </Label>
          <Input
            id="name"
            placeholder="Conference Room A"
            aria-invalid={errors.name ? true : undefined}
            className={fieldClassName}
            {...nameField}
            onChange={(event) => {
              nameField.onChange(event);
              if (!slugEdited) {
                setValue("slug", slugifyResourceName(event.target.value), {
                  shouldValidate: true,
                });
              }
            }}
          />
          {errors.name ? (
            <p className="text-label-md text-destructive">
              {errors.name.message}
            </p>
          ) : null}
        </div>

        <div className="space-y-base">
          <Label htmlFor="slug" className={labelClassName}>
            Slug
          </Label>
          <Input
            id="slug"
            placeholder="conference-room-a"
            aria-invalid={errors.slug ? true : undefined}
            className={fieldClassName}
            {...slugField}
            onChange={(event) => {
              setSlugEdited(true);
              slugField.onChange(event);
            }}
          />
          <p className="text-label-md text-muted-foreground">
            Generated from the name and editable before saving.
          </p>
          {errors.slug ? (
            <p className="text-label-md text-destructive">
              {errors.slug.message}
            </p>
          ) : null}
        </div>

        <div className="space-y-base">
          <Label htmlFor="description" className={labelClassName}>
            Description
          </Label>
          <textarea
            id="description"
            rows={4}
            placeholder="Describe how this resource is used..."
            className="w-full resize-none rounded-lg border border-border bg-background px-md py-sm text-body-md text-foreground placeholder:text-muted-foreground focus-visible:border-primary focus-visible:ring-2 focus-visible:ring-ring focus-visible:outline-none"
            {...register("description")}
          />
        </div>

        <div className="space-y-base">
          <Label className={labelClassName}>Resource Type</Label>
          <Controller
            control={control}
            name="resourceTypeId"
            render={({ field }) => (
              <Select
                name={field.name}
                value={field.value}
                onValueChange={(value) => field.onChange(value as string)}
              >
                <SelectTrigger
                  onBlur={field.onBlur}
                  aria-invalid={errors.resourceTypeId ? true : undefined}
                  className={cn(
                    fieldClassName,
                    "justify-between dark:bg-background dark:hover:bg-background"
                  )}
                >
                  <SelectValue>
                    {(selected: string | null) =>
                      resourceTypes.find(
                        (resourceType) =>
                          String(resourceType.resourceTypeId) === selected
                      )?.name ?? "Select a resource type"
                    }
                  </SelectValue>
                </SelectTrigger>
                <SelectContent align="start">
                  {resourceTypes.map((resourceType) => (
                    <SelectItem
                      key={resourceType.resourceTypeId}
                      value={String(resourceType.resourceTypeId)}
                    >
                      {resourceType.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          />
          {errors.resourceTypeId ? (
            <p className="text-label-md text-destructive">
              {errors.resourceTypeId.message}
            </p>
          ) : null}
        </div>
      </section>

      <section className="space-y-md rounded-xl border border-border bg-surface p-lg">
        <h2 className="text-headline-md text-foreground">Specifications</h2>

        <div className="space-y-base">
          <Label htmlFor="capacity" className={labelClassName}>
            Capacity
          </Label>
          <Input
            id="capacity"
            type="number"
            placeholder="Optional"
            aria-invalid={errors.capacity ? true : undefined}
            className={fieldClassName}
            {...register("capacity")}
          />
          {errors.capacity ? (
            <p className="text-label-md text-destructive">
              {errors.capacity.message}
            </p>
          ) : null}
        </div>

        <div className="space-y-base">
          <Label className={labelClassName}>Status</Label>
          <Controller
            control={control}
            name="status"
            render={({ field }) => (
              <Select
                name={field.name}
                value={field.value}
                onValueChange={(value) => field.onChange(value as string)}
              >
                <SelectTrigger
                  onBlur={field.onBlur}
                  className={cn(
                    fieldClassName,
                    "justify-between dark:bg-background dark:hover:bg-background"
                  )}
                >
                  <SelectValue>
                    {(selected: string | null) =>
                      selected
                        ? selected.charAt(0) + selected.slice(1).toLowerCase()
                        : "Select a status"
                    }
                  </SelectValue>
                </SelectTrigger>
                <SelectContent align="start">
                  {RESOURCE_STATUSES.map((status) => (
                    <SelectItem key={status} value={status}>
                      {status.charAt(0) + status.slice(1).toLowerCase()}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            )}
          />
        </div>
      </section>

      <div className="flex flex-col-reverse gap-sm sm:flex-row sm:justify-end">
        <Link
          href="/dashboard/resources"
          className="rounded-lg border border-border px-md py-sm text-center text-label-md text-muted-foreground transition-colors hover:bg-surface-hover"
        >
          Cancel
        </Link>
        <button
          type="submit"
          disabled={isSubmitting}
          className="inline-flex items-center justify-center gap-xs rounded-lg bg-primary px-md py-sm text-label-md text-primary-foreground transition-colors hover:bg-primary/90 disabled:cursor-not-allowed disabled:opacity-70"
        >
          {isSubmitting ? (
            <>
              <LoaderCircle className="size-4 animate-spin" aria-hidden="true" />
              Saving…
            </>
          ) : (
            "Create Resource"
          )}
        </button>
      </div>
    </form>
  );
}
