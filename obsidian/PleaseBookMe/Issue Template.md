# ISSUE-0042 — Missing Resource Assignment Validation

## Priority

High

## Summary

Resource assignment does not currently validate whether the assigned staff member belongs to the resource's organization.

## Location

- `resource/ResourceAssignmentService`
- `resource/ResourceAssignmentRepository`

## Current Behavior

The assignment is currently accepted without organization membership
validation.

## Expected Behavior

Assignment must be rejected when the staff member does not belong to the resource's organization.

## Suggested Follow-up

Add organization membership validation to the resource assignment flow.