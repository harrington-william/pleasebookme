# pleasebookme — Cloud Platform Research (replacing the $400 AWS spec)

[[trung|Trung]]'s brief, in his words: *"Find a different cloud provider, which can provide a similar computing power like this AWS configuration with more cost effective."* This page is the answer, and it **disputes the brief's premise** — the computing power was never where the money went.

## Core Finding

> [!danger] 🔴 **~60% of the $400 buys AWS-shaped plumbing, not computing power.**
> Reconstructed from AWS's published unit prices, the compute itself is **$106–180/month**. The rest — **NAT Gateway, load balancer, Secrets Manager, public IPv4 addresses, CloudWatch** — is **$79–109/month of networking and secrets management that a $12 VPS includes for free.**
>
> **So a like-for-like provider swap on the same architecture captures only part of the saving.** The saving is in deleting the plumbing, and the plumbing exists because the spec is shaped like a production AWS deployment rather than like a five-shop beta.

## The Spec as Sent (2026-09-10)

| # | Component | Trung's note |
|---|---|---|
| 1 | **Secrets Manager** — 20 secrets | Manage `.env` variables |
| 2 | **1 VPC** | The whole networking layer (prerequisite) |
| 3 | **ECS** — Linux Ubuntu x86, **5 processes**, 24/7, 2GB allocated | Next.js FE · Spring Boot BE · Postgres · Redis *(can exclude)* · RabbitMQ *(can exclude)* |
| 4 | **AWS RDS** — 20GB storage | Automatic backup, reboot on failure |

Trung's own framing: *"The above configuration is currently considered redundant, you can ask agents to exclude some components."* ⭐ **He is telling you the fat is there. He is right, and there is more of it than he flagged.**

## Where the ~$400 Goes

Reconstructed from published `us-east-1` on-demand prices (retrieved 2026-09-11). ⚠️ Estimate, not Trung's actual bill — **ask him for the AWS Cost Explorer breakdown to replace this table with the real one.**

| Line                               | Basis                                                        | Monthly        |
| ---------------------------------- | ------------------------------------------------------------ | -------------- |
| **ECS Fargate — 5 tasks**          | $0.04048/vCPU-hr + $0.004445/GB-hr; 5 × (1 vCPU, 2GB) × 730h | **~$180**      |
| *(same at 0.5 vCPU/task)*          |                                                              | *(~$106)*      |
| **NAT Gateway**                    | $0.045/hr + $0.045/GB processed                              | **~$33+**      |
| **Application Load Balancer**      | $0.0225/hr + LCU charges                                     | **~$20–50**    |
| **RDS `db.t4g.micro` + 20GB**      | $0.016/hr + storage                                          | **~$14**       |
| **Secrets Manager**                | 20 secrets × $0.40                                           | **$8**         |
| **Public IPv4**                    | $0.005/hr × 5 tasks                                          | **~$18**       |
| **CloudWatch Logs · ECR · egress** | usage-based                                                  | **~$15–25**    |
|                                    |                                                              | **≈ $290–330** |

Push RDS to Multi-AZ, or size the tasks generously, and **$400 is exactly where this lands.** The number is real and reconstructible — this is not an estimate to argue with.

## The Three Problems in the Spec

> [!danger] 🔴 **1. The database is provisioned twice.**
> Item 3 lists **Postgres as one of the five ECS processes.** Item 4 is **AWS RDS**. Those are two databases, and the bill pays for both. Either Postgres runs as a container (**then RDS is redundant**) or RDS is the database (**then it is not an ECS process**). ⚠️ **This is the first question for Trung, and it is not rhetorical** — it may simply be that the spec lists the dev-compose stack and the prod database in one list. But as written, it is ~$36/month of duplicated compute plus a whole RDS bill.

> [!warning] 🔴 **2. The plumbing costs more than the application.**
> **NAT Gateway ($33) + ALB ($20–50) + Secrets Manager ($8) + public IPv4 ($18) ≈ $79–109/month.** On a single VPS, every one of those is either free or a config file:
> - **NAT Gateway** → a server with a public IP needs no NAT.
> - **ALB** → Caddy or nginx, free, auto-renewing TLS included.
> - **Secrets Manager** → an `.env` file with locked permissions, or the provider's own free secret store. **$8/month to hold 20 strings is not a service, it is a subscription to a text file.**
> - **Public IPv4 per task** → one IP for one box.

> [!note] ⚠️ **3. Trung already flagged the fat, and it is bigger than he flagged.**
> He marked **Redis** and **RabbitMQ** as excludable — correct: a five-shop beta has no cache-pressure problem and no queue-depth problem, and RabbitMQ in particular is infrastructure for a scale that does not exist. ⭐ **But dropping them takes 5 processes to 3, and the remaining three fit comfortably on one machine.** The question is not *which processes to remove* but *why each process needs its own billed container.*

## What the Beta Actually Needs

**One server, ~4GB RAM, running three containers via `docker-compose`:** Next.js front end · Spring Boot back end · PostgreSQL. Caddy in front for TLS. Nightly `pg_dump` to object storage. **Cloudflare's free tier in front of all of it** for DNS, CDN, WAF and DDoS.

That is not a downgrade — it is **the same application** with the AWS-specific scaffolding removed.

## Candidate Providers

⚠️ **Prices retrieved 2026-09-11 and to be confirmed on each vendor's own page before they enter the sheet.** Configuration target: ~2 vCPU / 4GB / ~80GB SSD.

| Provider | Plan | ~Monthly | Why it is on the list |
|---|---|---|---|
| ⭐ **FPT Cloud** | 2 vCPU / 4GB | **~300,000₫ (~$11.5)** | **VN datacenter, billed in đồng, issues a hóa đơn VAT** |
| **Viettel IDC · VNPT · BizFly** | equivalent tier | to price | Same VN advantages; Viettel entry tier from ~129,000₫ |
| **Hetzner (Singapore)** | CPX21 · 3 vCPU / 4GB | ~$10–44 (⚠️ verify) | Cheapest per GB; **Singapore region since 2024**, so the old latency objection is gone |
| **DigitalOcean (Singapore)** | 2 vCPU / 4GB | **~$24** | Best documentation; managed Postgres available later without migrating |
| **Oracle Cloud Free Tier** | 4 ARM cores / 24GB | **$0** | Genuinely free and genuinely capable; ⚠️ capacity is not guaranteed |

**Even the most expensive option here is ~$24 against ~$400 — a ~94% cut**, and the cheapest VN option is ~$11.50.

## On Cloudflare

Sonny's preference, and it deserves a straight answer: ⚠️ **Cloudflare cannot host this stack, and should absolutely still be used.**

- **Why it cannot host it:** Workers run JS/WASM, not a 24/7 Spring Boot JVM. Cloudflare **Containers** do support Java, but per Cloudflare's own documentation they use **ephemeral storage and a lifecycle that does not suit long-running stateful services** — they are not a drop-in for an always-on PaaS. There is **no managed PostgreSQL** (D1 is SQLite). Running Postgres on Containers is a demo, not a production database.
- **Cloudflare's own recommended pattern** is the shape to copy: **persistent database on an external provider, bursty compute on Cloudflare, joined by a Worker.**
- ✅ **What it should do here, on the free tier:** DNS, CDN, WAF, DDoS protection, SSL — and **R2** for backups and assets if object storage is ever needed, since R2 charges **no egress fees**.

⭐ **This is the vault's 2026-09-09 conclusion holding up exactly as written: *AWS hosts; Cloudflare is the security layer on top.*** The hosting half changes; Cloudflare's job does not.

## ⭐ The Accounting Argument (the half Trung cannot make)

This is where Sonny's lane earns its keep, and it is **not** a rounding-error consideration:

- A **Vietnamese provider issues a hóa đơn VAT**, which the [[founder-agreement-position|HKD]] can book as a deductible business expense with no argument.
- A **USD card charge to Hetzner or DigitalOcean** is a foreign-supplier transaction: no VN VAT invoice, messier substantiation, FX exposure, and a foreign-contractor withholding question that a student-run HKD does not want to answer at year-end.
- The venture is about to register an entity and start invoicing. **Choosing infrastructure that produces clean, bookable Vietnamese invoices is worth paying a small premium for** — and on these numbers there is no premium to pay, because FPT is already the cheapest candidate on the list.

**So the recommendation is not merely "cheaper." It is cheaper *and* produces better books.**

## Recommendation

1. **For the beta — one VN VPS (FPT or Viettel), ~300,000₫/month**, three containers, Caddy for TLS, Cloudflare free tier in front, nightly `pg_dump`. **From ~10,400,000₫/month to ~300,000₫/month: a ~97% reduction**, and the 6,000,000₫ float goes from **~17 days of runway to ~20 months**.
2. **Keep a managed-Postgres upgrade in reserve**, not now. When real bookings exist and a lost database would cost customers, move Postgres to the provider's managed tier for ~$15–25/month. **Do not buy failover for zero users.**
3. **Revisit only on a trigger, not a feeling:** sustained CPU above ~70%, or the first paying customer whose downtime costs money.