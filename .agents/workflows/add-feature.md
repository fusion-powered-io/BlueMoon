---
description: How to add a new feature following Hexagonal Architecture
---

# Workflow: Adding a New Feature

When adding a new feature, follow these steps to maintain architectural integrity. Each feature should be treated as a **Domain Module** (similar to an Aggregate Root in DDD).

1.  **Define Domain Module**:
    -   Identify the feature as a self-contained module in `domain/[feature]/`.
    -   Ensure it has a clear boundary and a central Service interface.

2.  **Define Domain Models**:
    -   Create data classes in `domain/[feature]/model/`.
    -   Keep these classes pure and free of infrastructure dependencies.

3.  **Define Ports**:
    -   Create interfaces in `domain/[feature]/port/` for external interactions (Repositories, Parsers, etc.).

4.  **Implement Adapters**:
    -   Create concrete implementations in `domain/[feature]/adapter/[provider]/`.
    -   Follow `domain.[feature].adapter.[provider]` package naming.

5.  **Create Domain Service**:
    -   Define the main interface in `domain/[feature]/[Feature]Service.kt`.
    -   Implement logic in `domain/[feature]/application/[Feature]ServiceImpl.kt`.

6.  **Setup Dependency Injection**:
    -   Create `domain/[feature]/[Feature]Module.kt` using Koin annotations.

7.  **Build UI Component (MVI)**:
    -   Create an `object [Component]` in `presentation/components/`.
    -   Implement the nested `State`, `Presenter` (KoinPresenter), and `invoke()` operator.
    -   Follow the MVI pattern established in `ARCHITECTURE.md`.
