# Resource Type

## Definition

A **resource type** is a category an organization uses to classify its assets — Room, Chair, Vehicle, Equipment, Court.

It is a small, per-organization vocabulary, defined once during setup and rarely revisited. Its purpose is to make an inventory of dozens or hundreds of assets scannable and filterable.

## Why Types Are Owned By Organizations

Types are not a platform-wide taxonomy. A barbershop's meaningful categories ("Chair", "Basin") have nothing in common with a logistics company's ("Van", "Forklift"). Forcing a shared vocabulary would make it useless to both.

The cost of this choice is that every new organization starts with an empty vocabulary — see the prerequisite discussed in [[Resource Lifecycle]].

## Properties

| Property | Meaning | Required |
|---|---|---|
| Name | Display name, unique within the organization | Yes |
| Description | What belongs in this category | No |
| Icon | A visual marker for the category | No |

The icon is optional because it is decoration. Requiring it would block an organization from classifying its estate over a choice that carries no meaning.

## Invariants

- Name is unique within an organization.
- Belongs to exactly one organization.
- A type may classify many resources; a resource has exactly one type.

**Known gap:** the uniqueness of the name is currently guaranteed only by the persistence layer. A duplicate name is rejected, but as a raw storage failure rather than a considered response — see [[Resource Failure Handling]].

## Deletion Cascades To Resources

Removing a type removes **every resource classified by it**.

This is the most destructive operation the service exposes, and it does not currently announce itself as such. An organization deleting a type it believes is unused will silently lose every asset in that category. Anything built on top of this service should treat type deletion as a high-consequence action and confirm it against the resources it would take with it.

## Relationships

- Belongs to one **organization**.
- Classifies many **[[Resource]]** records.

**Known gap:** when a resource is created, the type is not verified to belong to the caller's own organization. The classification of a resource can therefore reference another organization's vocabulary. This is recorded in [[Resource Authorization Rules]].
