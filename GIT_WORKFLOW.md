# Git Workflow Guide — Eventra Backend

This document explains how we manage branches and collaborate on the project.
Read this before writing any code.

---

## Branch Structure

```
main                        ← stable, working code only. Never touch directly.
└── dev                     ← integration branch. All features merge here first.
    ├── feature/m1-auth-user
    ├── feature/m2-event
    ├── feature/m3-booking
    ├── feature/m4-notification
    ├── feature/m5-gamification
    └── feature/m6-analytics
```

---

## The Golden Rules

- **Never commit directly to `main` or `dev`**
- Always work on your assigned feature branch
- Always pull from `dev` before starting work each day
- Always open a Pull Request to merge — never merge locally into dev

---

## Module Assignments

| Module | Branch | 
|--------|--------|
| M1 — Auth & User | `feature/m1-auth-user` |
| M2 — Event Management | `feature/m2-event` |
| M3 — Booking & Payment | `feature/m3-booking` |
| M4 — Notification | `feature/m4-notification` |
| M5 — Gamification | `feature/m5-gamification` |
| M6 — AI & Analytics | `feature/m6-analytics` |

---

## First Time Setup

Clone the repo and set up your local branches:

```bash
git clone https://github.com/YOUR_USERNAME/eventra-backend.git
cd eventra-backend

# Fetch all remote branches
git fetch --all

# Switch to your assigned branch (example: m2-event)
git checkout feature/m2-event
```

Then follow the installation steps in the **README.md** to set up PostgreSQL, Redis, and your `application.properties`.

---

## Daily Workflow

### 1. Start of day — sync with dev

Always do this before writing any code:

```bash
git checkout feature/your-branch-name
git merge dev
```

If there are conflicts, fix them before continuing.

### 2. Write your code

Work only inside your module folder:
```
src/main/java/com/eventra/backend/module/YOUR_MODULE/
```

### 3. Commit your work

```bash
git add .
git commit -m "feat: short description of what you did"
git push
```

### 4. End of day — push your work

```bash
git push origin feature/your-branch-name
```

---

## Commit Message Format

Use this format for all commits:

```
type: short description
```

| Type | When to use |
|------|-------------|
| `feat` | Adding new functionality |
| `fix` | Fixing a bug |
| `refactor` | Restructuring code without changing behavior |
| `docs` | Updating documentation or comments |
| `test` | Adding or updating tests |
| `chore` | Config changes, dependency updates |

**Examples:**
```
feat: add event creation endpoint
fix: resolve null pointer in booking service
refactor: simplify JWT token validation logic
docs: add comments to AuthService methods
test: add unit tests for UserService
```

---

## Merging Into Dev (Pull Request)

When your module feature is complete and tested:

1. Push your branch one final time:
```bash
git push origin feature/your-branch-name
```

2. Go to the GitHub repo

3. Click **"Compare & pull request"** on your branch

4. Set the base branch to **`dev`** (not `main`)

5. Write a short description of what you implemented

6. Request a review from at least one teammate

7. Wait for approval before merging

---

## Keeping Your Branch Up To Date

If a teammate merges something into `dev` and you need those changes:

```bash
git checkout feature/your-branch-name
git merge dev
```

Resolve any conflicts if they appear, then continue working.

---

## What NOT To Do

```bash
# NEVER do these:
git push origin main
git push origin dev
git merge feature/someone-elses-branch
git commit -m "update"          # always write a proper message
git add .  && git commit --amend  # don't rewrite pushed history
```

---

## If Something Goes Wrong

**Accidentally committed to the wrong branch:**
```bash
# Undo last commit but keep your changes
git reset HEAD~1
# Then switch to the correct branch and re-commit
```

**Want to discard all local changes and start fresh:**
```bash
git checkout .
```

**Merge conflict you can't resolve:**
Don't force it — message the team and figure it out together.

---

## Quick Reference Card

```bash
# Switch to your branch
git checkout feature/your-branch-name

# Pull latest from dev into your branch
git merge dev

# Check what files you changed
git status

# Stage all changes
git add .

# Commit
git commit -m "feat: your message here"

# Push to GitHub
git push

# Check branch list
git branch
```
