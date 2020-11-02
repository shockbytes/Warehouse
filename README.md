# Warehouse

TODO docs...

## Warehouse & Boxes

#### Use Case: Online/Offline data management
Describe Dante Use Case, especially when data needs to be removed

# Features

### Multi data source management
TODO Explain feature

### Initial box synchronization
TODO Explain feature

* Add new box and link to the sync box
* Warehouse initiates syncing between boxes ONCE
* The sync applies a SynchronizationStrategy
  * Default implementation forms a union of both datasets and writes it to the new box
* The box marks the sync as complete
* From this point on, the boxes should be synchronized

### Dynamic box management at runtime
TODO

## What's missing for the first beta release?

There are a bunch of things that make this library not even suitable
for a hobby project.

### General issues
- Test coverage
- Working linting rules (warehouse-rules) module

### Box issues
- Make Box an abstract class rather than an interface
- Initial box synchronization (with Lint warning if content is not a data class)
- FileBox implementation
- SQLiteBox implementation
- A working RealmBox implementation

### Warehouse issues
- Final Warehouse API
- `idSelector` can handle any data type as `id`
- READ/WRITE atomicity
- Dynamically add boxes

### Supported Boxes
| **Supported** | **Planned** | **Experimental** |
|:---------:|:------------:|:--------:|
| Android Log          | FireStore             | ProtoDataStore        |
| InMemory         | Room             | PreferencesDataStore        |
| Realm          | SQLite             |         |
| Firebase Database          | FireStore             |         |
| Android File          | REST             |         |

## Warehouse 2.0

The initial version aims to provide an easy to use API for simple use cases, where
performance is not a driving factor. The main goal is to provide a durable and
migration-friendly storage API. Version 2.0 aims to go further in this direction
and wants to improve performance by adding queries and logs for boxes. In addition,
it should be possible to use Coroutines/Flow instead of solely relying on RxJava.

### Proposed V2.0 changes
- Box-independent query language on top of boxes
- Each box has an own Log and allows rollbacks to previous versions
- WarehouseAdapter (like Retrofit) to use either Flow/Coroutines or RxJava
