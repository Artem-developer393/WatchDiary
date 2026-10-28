# How WatchDiary works

This is a beginner-friendly explanation of the Kotlin project. You do **not** need to understand every file before working on it. Start with the main screen, follow one action into the ViewModel, and then into Room.

## 1. Where the app starts

1. Android starts `MainActivity.kt`.
2. `MainActivity` sets up the Compose UI, navigation, and screen ViewModels.
3. `WatchDiaryApp.kt` initializes the local Room database and the repository.

For your first reading session, open **`MainActivity.kt`**, then **`ui/screens/ListScreen.kt`**.

## 2. Four responsibilities

| Where | Main job | What should *not* happen here |
| --- | --- | --- |
| `ui/screens/` | Draw Compose UI, respond to taps, open Android file picker | Direct SQL queries |
| `viewmodel/` | Validate input, perform actions, provide `uiState` | Android screen navigation or sharing intents |
| `data/repository/` | Expose operations and coordinate database transactions | Compose UI rendering |
| `data/local/` | Define Room entities, queries, and SQLite access | UI decisions |

The `data/model/` package contains the entry data class and enums, while `ui/components/` contains reusable UI pieces (filters, rating stars, cards).

## 3. Example: saving a watched movie

Open these files in order:

1. **`ui/screens/AddScreen.kt`** — the user types the movie name, selects rating and taps Save.
2. **`viewmodel/AddEntryViewModel.kt`** — `saveEntry()` checks that the title is present and launches a coroutine.
3. **`data/model/DiaryEntry.kt`** — represents a saved item (title, type, date, rating, comment).
4. **`data/repository/DiaryRepository.kt`** — `insertEntry()` forwards the operation to the DAO.
5. **`data/local/DiaryEntryDao.kt`** — Room runs the database insert.
6. The ViewModel updates UI state. Compose observes it and shows the updated result.

The main idea: **UI tells the ViewModel what happened; the ViewModel changes data; the UI observes state.**

## 4. Example: filtering the diary

- `ListScreen.kt` renders counter cards, filter controls, and the visible list.
- `ListViewModel.kt` stores selected filters and sort order and computes the visible items.
- `FilterSection.kt` draws the filter controls; `StatsCardRow.kt` draws the counters.

Filtering doesn't delete records or change the underlying Room database.

## 5. Export and import

- `data/export/ExportManager.kt` gets diary entries from the repository and writes JSON.
- `util/ImportParser.kt` reads text lists and JSON files into entries.
- `DiaryRepository.kt` imports entries while skipping duplicates.
- Android's file picker and Downloads handling belong to `ui/screens/`, because those actions need UI interaction.

**Important:** Never test import/delete with data you cannot recover. Export a backup first.

## 6. Localization

- `res/values/strings.xml` contains English text.
- `res/values-ru/strings.xml` contains Russian text with the same resource names.
- Screens use Android string resources to display the selected language.
- `AppCompatDelegate` is responsible for changing and persisting the language.
- The old `AppLanguage` singleton no longer exists; no static field stores an Android `Context`.

For example, a screen resolves `R.string.save` to **Save** or **Сохранить**.

## 7. How to make a small change safely

1. Find the text, screen, or behavior to change.
2. If it is a **visual change**, start in `ui/screens/` or `ui/components/`.
3. If it is a **behavior or validation change**, start in `viewmodel/`.
4. If it changes **stored data**, review `data/model/`, `data/repository/`, and `data/local/` together.
5. Run the app and check both language modes.
6. If parsing or importing changed, run the unit tests.

### Suggested learning order

`DiaryEntry.kt` → `ListScreen.kt` → `ListViewModel.kt` → `DiaryRepository.kt` → `DiaryEntryDao.kt` → `MainActivity.kt`.

This order is easier than reading every package alphabetically. The project uses a common Android structure, but intentionally does not add Hilt, a separate domain layer, or multiple Gradle modules.
