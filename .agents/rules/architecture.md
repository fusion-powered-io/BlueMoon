---
trigger: always_on
---

# Architecture Guide

This project follows a **Hexagonal Architecture (Ports and Adapters)** pattern, heavily inspired by **Spring Modulith** principles, even though it is an Android/CMP application.

## Core Principles

1.  **Dependency Rule**: Dependencies always point inwards toward the Domain layer.
2.  **Ports and Adapters**: 
    *   **Ports (Interfaces)** define the contract for what the application needs.
    *   **Adapters (Implementations)** provide the actual logic for interacting with external systems.
3.  **No Adapter-to-Adapter Communication**: Adapters should never call other adapters directly. All communication must go through domain ports.
4.  **Cross-Module Communication**: Domain services in the `application` layer can call services in other modules, but *only* via the target module's interface ports (located at the module root).

## Project Structure

The project is organized into the following layers under the project namespace/packageName (which can be found in the build.gradle.kts file):

### 1. Domain Layer (`/domain/[module]`)
The Domain layer is organized into **Modules** (representing Aggregates in DDD). They are independent of specific UI features and provide the business logic needed by the application.

-   **`port/`**: Interfaces that define external requirements (e.g., `GameRepository`).
-   **`model/`**: Pure data classes representing domain entities (e.g., `GameMetadata`).
-   **`application/`**: Business logic that coordinates ports and models.
-   **`adapter/`**: Concrete implementations of the ports. Each provider should have its own sub-package: `domain.[module].adapter.[provider]` (e.g., `domain.catalog.adapter.retroachievements`).
-   **`[Module]Service.kt`**: The main entry point for the module's logic, exposing flows or suspend functions.
-   **`[Module]Module.kt`**: Koin module for dependency injection within the domain module.

#### [Adapters]
Adapters may have their own model and for mappings to and from the domain should be done as follows:

Domain to Model:
 Create a [AdapterModel]Mapper.kt file that contains the DomainModel.to[AdapterModel]() extension functions.

Model to Domain:
 Create a [Model]Mapper.kt file that contains the AdapterModel.to[DomainModel]() extension functions.

### 2. Presentation Layer (`/presentation/`)
This is where **Features** live. A feature is a UI-driven capability that coordinates one or more Domain Modules to provide value to the user.

#### [MVI Pattern]
We use a custom MVI implementation centered around the `KoinPresenter`. Each UI component is defined as an `object` that encapsulates its state, presenter, and UI.

**Component Structure:**
- **`object [ComponentName]`**: The wrapper for the entire component.
- **`data class State`**: Represents the immutable UI state. Use a `sealed interface` if there are multiple distinct states.
- **`Presenter`**: 
    - Annotated with `@Qualifier(State::class)` and `@Factory`.
    - Implements `KoinPresenter<State>`.
    - Overrides `@Composable fun present(): State` to coordinate logic and returns the state.
- **`invoke()`**:
    - A `@Composable operator fun invoke(...)`.
    - Injects the presenter using `injectPresenter<State>()`.
    - Calls `presenter.present()` to get the current state.
    - Renders the UI based on the state.

#### [Structure]
-   **`components/`**: UI component objects (e.g., `CatalogGrid.kt`).
-   **`UiEntryPoint.kt`**: Main entry point for the UI.
-   **`PresentationModule.kt`**: Koin module for presentation-specific dependencies.

### 3. Bootstrap Layer (`/bootstrap`)
Contains the application initialization logic and global configuration, including the `KoinPresenter` infrastructure.

## Dependency Injection (Koin)
We use Koin for dependency injection. Most services and repositories should be annotated with `@Single` or `@Factory` and included in their respective feature's Koin module.

## UI Patterns
-   **Adaptive UI**: Use the custom `AdaptiveSizing.kt` and `Modifier` extensions to ensure the UI looks great on various screen sizes.
-   **State Management**: Use `StateFlow` in services to expose UI state.
