# Marky: Feature Specification

2026-10-02 · @Devrath

> Source: https://markdown-editor.notion.site/
>
> Revision 2 (2026-11-11): every feature now has exact behavior, edge cases, and acceptance criteria, plus UX, accessibility, performance, and platform standards. Scope is unchanged; see [Out of scope](#out-of-scope).

## Overview

Marky is a local-first notes app: people write notes in Markdown, read them nicely formatted, and find any note quickly through search.

It runs fully offline with no account, server, or sync. All notes live on the device, and users can import or export any single note as a plain `.md` file at any time.

**Why this app:** Markdown is a fast, portable way to write, but many notes apps either hide it behind rich-text editors or bury it under features. Marky keeps only the core: write in Markdown, read it formatted, and find any note in seconds.

**Platforms:** Android and iOS, from one Kotlin Multiplatform + Compose Multiplatform codebase. Each platform follows its own conventions where users expect them (see [Platform conventions](#platform-conventions)).

## Goals and principles

- **Writing comes first.** Opening the app and starting a note takes one tap. Nothing asks the user to organize before they write.
- **Formatting lives in the text.** Formatting is typed as Markdown directly into the note, not set through menus. This keeps notes portable and the workflow fast.
- **The user owns their data.** Notes work offline, never leave the device unless exported, and export as standard Markdown that opens in any other editor.
- **Never lose a word.** No crash, process kill, rotation, or backgrounding may lose text the user typed. This rule beats every other.
- **Fast feels calm.** Every interaction responds within one frame, and nothing the user does waits on a spinner.
- **Usable by everyone.** All features work with a screen reader, large text, and a hardware keyboard.

### Success criteria

| Measure | Target |
|---|---|
| Launch to typing in a new note | ≤ 2 taps, ≤ 1.5 s from cold start |
| Text lost to crashes or process death | 0 characters |
| Search results shown after a keystroke (10,000 notes) | ≤ 100 ms |
| Accessibility audit (TalkBack, VoiceOver, 200% text) | No blocking issues |

## Features

The app has four feature areas. Each item says what the feature does, why it's there, and how to tell that it works.

### 1. Managing notes

#### Create a note

- One action (the primary button on the notes list) creates an empty note, opens it in **edit mode**, focuses the body, and shows the keyboard.
- The note is not saved until it has any non-whitespace content in the title or body. A note left empty is discarded silently when the user navigates away, with no dialog. This keeps the list free of blank notes.
- *Why:* capturing a thought should never wait on setup.
- *Done when:* tapping create, typing one character, and going back produces exactly one note in the list; tapping create and going back immediately produces none.

#### Titles

- Each note has a separate, optional title field above the body.
- If the title is empty, the list and search show the first non-empty line of the body (with Markdown syntax stripped) as a fallback title. If that is also empty, they show "Untitled".
- Titles are single-line. Pressing Enter in the title moves focus to the body.
- Titles don't have to be unique; every note has its own ID.

#### Autosave

- Changes save automatically while typing, debounced to at most once per 500 ms of inactivity. Pending changes also save immediately when the note screen leaves the foreground: navigating back, switching apps, locking the screen, or a configuration change.
- Unsaved text survives process death: the in-progress draft is restored through saved state if the app is killed before a save completes.
- There is no save button and no "saved" toast. An unobtrusive "Saved" status may appear in the toolbar, but it must never move content.
- *Why:* losing writing is the worst thing a notes app can do.
- *Done when:* killing the process right after typing, then reopening the note, shows every character typed.

#### Notes list

- All notes appear in one list: pinned notes first, then unpinned notes, each group sorted by last edited (newest first).
- Each entry shows:
  - the title (or fallback title), on one line, truncated with an ellipsis;
  - a preview of up to 2 lines of body text, with Markdown syntax stripped (no `#`, `**`, `- [ ]` markers) so it reads as plain prose;
  - the edit time: relative for recent edits ("Just now", "5 min ago", "Yesterday"), then a short date ("Mar 4"), with the year included for earlier years;
  - a pin indicator for pinned notes.
- "Last edited" changes only when the title or body text changes. Opening, reading, pinning, or ticking nothing does not reorder the list. Ticking a checklist item in preview does count as an edit.
- The list keeps its scroll position when the user returns from a note.
- *Why:* recent work is usually what people return to.

#### Pin notes

- Users pin or unpin a note from the note screen's actions, or with a long-press menu on a list entry.
- Pinning moves the note to the top group straight away, with a short reorder animation (none if reduce-motion is on).
- Pinning doesn't change the note's last-edited time.
- *Why:* a few notes (an index, a to-do list) are opened far more than the rest.

#### Delete a note

- Users delete from the note screen's actions or from the long-press menu on a list entry.
- Deleting always shows the delete confirmation dialog (see [Dialog](#dialog)). Confirming removes the note for good and returns to (or stays on) the notes list.
- *Why:* there is no undo or trash in scope, so the confirmation is the only safety net.

### 2. Writing in Markdown

#### Markdown editor

- Notes are plain Markdown text, supporting the CommonMark core plus these GitHub Flavored Markdown extensions:

  | Element | Syntax |
  |---|---|
  | Headings (levels 1–3 in the toolbar; 1–6 parsed) | `#`, `##`, `###` |
  | Bold, italic | `**bold**`, `*italic*` |
  | Bulleted and numbered lists, nested | `- item`, `1. item` |
  | Checklists | `- [ ] task`, `- [x] done` |
  | Quotes | `> quote` |
  | Inline code and fenced code blocks | `` `code` ``, ```` ``` ```` |
  | Web links and bare URLs | `[text](https://…)`, `https://…` |
  | Strikethrough | `~~text~~` |

- The text is stored exactly as typed. The app never rewrites, reformats, or normalizes the user's Markdown, apart from line endings (see [Import a note](#import-a-note)).
- The raw editor uses a readable proportional font with light syntax highlighting: muted Markdown markers, bold headings, and a monospace face for code. The highlighting doesn't change the text itself.
- **List continuation:** pressing Enter at the end of a list item, checklist item, or quote line continues it on the next line (`- `, `- [ ] `, the next number, `> `). Pressing Enter on an empty continued item removes the marker and ends the list. This matches how every serious Markdown editor behaves.
- Standard text editing works: selection handles, cut/copy/paste, platform undo/redo (in-session, for typing), spell check, and autocorrect. Autocorrect never changes Markdown markers or text inside code.
- *Why:* Markdown is fast to type, readable as plain text, and portable to other tools.

#### Formatting toolbar

- In edit mode, a toolbar sits directly above the keyboard and moves with it. It's hidden when the keyboard is hidden.
- Buttons, in order: Heading, Bold, Italic, Bulleted list, Numbered list, Checklist, Quote, Inline code, Code block, Link. The toolbar scrolls horizontally on narrow screens, without hiding any button behind a menu.
- Each button inserts the same Markdown the user could type by hand:
  - **Inline styles (bold, italic, inline code):** with a selection, wrap it; tapping again on already-wrapped text unwraps it. Without a selection, insert the empty pair and place the cursor between the markers.
  - **Line styles (heading, lists, checklist, quote):** apply to every line the selection touches. Tapping again removes the marker. Heading cycles `#` → `##` → `###` → none.
  - **Code block:** wrap the selected lines in fences, or insert an empty fenced block with the cursor inside.
  - **Link:** with a selection, produce `[selection](|)` with the cursor in the URL. Without one, insert `[](|)`. If the clipboard holds a URL, use it as the target.
- Every toolbar action is one undoable step.
- Buttons have 48 dp (Android) / 44 pt (iOS) touch targets, an accessible name such as "Bold", and a tooltip on long-press.
- *Why:* characters like `#`, `*`, and `` ` `` take several taps on a phone keyboard.

#### Preview mode

- A single toggle in the note's top bar switches between **Edit** and **Preview**. Its icon and accessible label always describe the mode it switches *to*.
- New notes open in edit mode. Existing notes open in preview mode, since most visits are for reading. Double-tapping in preview switches to edit with the cursor placed at the tapped position (or as close as can be determined).
- The scroll position carries over between modes, so the user stays looking at the same part of the note.
- Preview renders with comfortable reading typography: a maximum line length of about 70 characters on wide screens, 1.5× line height, clear heading hierarchy, and code blocks that scroll horizontally instead of wrapping.
- Raw HTML in a note is shown as plain text, never rendered or run.
- *Why:* writing and reading are different activities; reading is easier without the syntax visible.

#### Interactive preview

- Tapping a checklist item in preview toggles it, updates the underlying Markdown (`[ ]` ↔ `[x]`), and saves straight away. Ticked items are shown visually completed (muted, with a check) but stay in place.
- Tapping a web link opens it in the system browser (an in-app browser tab is acceptable). Only `http`, `https`, and `mailto` links open; any other scheme is shown as text.
- Long-pressing a link offers Copy link and Open.
- *Why:* preview is where people use their notes, not just look at them.

### 3. Search

#### Full-text search

- Search covers all titles and note bodies. Results update as the user types, debounced by about 150 ms, with no submit step.
- Matching:
  - case- and diacritic-insensitive ("cafe" finds "Café");
  - every search word must match (AND), in any order;
  - words match by prefix ("meet" finds "meeting");
  - Markdown markers are ignored, so "**plan**" in a note is found by "plan".
- Ranking: title matches rank above body-only matches. Within each group, more recently edited notes come first.
- Search is backed by a full-text index (for example SQLite FTS) so it meets the 100 ms target with 10,000 notes.
- The query is kept if the user opens a result and comes back.
- *Why:* as the number of notes grows, search becomes the main way to find things.

#### Highlighted matches

- Each result shows the title and a one-to-two-line snippet centered on the first body match, with every matched word highlighted. The highlight uses a background color plus bold, so it doesn't rely on color alone.
- If only the title matches, the snippet shows the start of the body.
- Opening a result opens the note in preview, scrolled to the first match.
- *Why:* users can pick the right result without opening several notes.

### 4. Import and export

#### Import a note

- From the notes list, an icon button (accessible name "Import Markdown file") opens the system file picker, filtered to Markdown and plain-text files (`.md`, `.markdown`, `.txt`). The user picks exactly one file; there is no bulk import.
- The note's title is the file name without its extension. The file's contents become the note's body, unchanged except that:
  - the text is decoded as UTF-8, with a UTF-8 byte-order mark removed if present;
  - Windows (`\r\n`) and old Mac (`\r`) line endings become `\n`.
- Limits and failures: files over 5 MB, files that aren't valid UTF-8 text, and files that can't be read are rejected.
- Cancelling the picker does nothing and shows nothing.
- *Why:* users can bring a note in from another editor, including one they exported earlier.

#### After importing

- The user stays on the notes list. The imported note counts as created and edited at the time of import, so it appears at the top of the unpinned notes, and the list scrolls to show it.
- A snackbar confirms "Imported "<title>"", with an **Open** action.
- *Why:* the user sees the result right away without leaving the list.

#### Import errors

- If the file can't be imported, a snackbar on the notes list says why, in plain language: "This file is too large to import (max 5 MB)", "This file isn't a text file", or "Couldn't read this file". No note is created.
- Snackbars are announced by screen readers and stay visible long enough to read (at least 4 s, longer when accessibility settings ask for it).
- *Why:* a failed import should be visible but shouldn't block the user.

#### Duplicate titles on import

- If a note with the same title already exists, the imported note keeps the file name as its title unchanged, so two notes can both be called "Ideas".
- *Why:* every note has its own ID, so importing never overwrites an existing note, and the imported note is easy to spot at the top of the list.

#### Export a note

- From the note screen's actions, **Export as Markdown** opens the system save/share flow: the share sheet or Save to Files on iOS, the document creator (Storage Access Framework) on Android.
- The file is named after the note's title (or fallback title), with characters that are illegal in file names (`/ \ : * ? " < > |` and control characters) replaced by `-`, trimmed to 100 characters, plus `.md`.
- The file holds exactly the note's body as UTF-8 with `\n` line endings. The title isn't added to the body, so importing the file again gives back the same title and body.
- Success is confirmed with a snackbar where the platform doesn't already confirm it. Failure shows a snackbar explaining what went wrong.
- There is no bulk export.
- *Why:* users own their writing and can take any note to another editor.

### General

#### Sample notes on first launch

- On first launch only, the app creates 3 short sample notes: "Welcome to Marky" (pinned), "Markdown cheat sheet" (shows every supported element, including a tickable checklist), and "Tips" (search, pin, import/export).
- They're ordinary notes the user can edit or delete. Once deleted, they never come back, including after app updates.
- *Why:* an empty app doesn't show what's possible; examples do.

#### Light and dark theme

- The theme follows the system setting and switches live. On Android 12+ it may use dynamic color, as long as contrast targets are still met.
- Both themes meet WCAG 2.2 AA contrast: 4.5:1 for body text and 3:1 for large text, icons, and focus indicators. This includes muted Markdown markers and search highlights.
- *Why:* people write at all hours.

#### Helpful empty states

- Every empty screen has a short headline, one sentence of guidance, and the action that resolves it:
  - **No notes:** "No notes yet". "Write your first note, or import a Markdown file." With Create and Import actions.
  - **Search, nothing typed:** "Search your notes". "Find any word in titles and text."
  - **Search, no results:** "No results for "<query>"". "Check the spelling or try fewer words."
- *Why:* a blank screen gives a beginner no direction.

## Screens

The app has three screens and one dialog. The notes list is the home screen; every other screen is reachable from it in one step.

### Notes list (home)

Where users land and pick what to work on.

- Top app bar: app name, a search entry point (an icon or a tappable search field), and the import icon button
- Pinned notes on top (under a quiet "Pinned" label when any exist), then all other notes by last edited
- Each entry: title, preview, time since last edit, and pin indicator; tap to open, long-press for Pin/Unpin, Export, Delete
- Primary button to create a new note (floating action button on Android; a toolbar button that matches the platform style on iOS), always reachable with one thumb
- Snackbar host for import results and errors, placed above the create button so it never covers it
- Empty state for a user with no notes
- Large screens (tablets, foldables, landscape): list and note side by side, master–detail style

### Note

Where users read and write a note.

- Top bar: back, the Edit/Preview toggle, and an overflow menu with Pin/Unpin, Export as Markdown, and Delete
- Title, editable in place, single-line
- Edit mode: the raw Markdown text with light syntax highlighting, and the formatting toolbar above the keyboard
- Preview mode: formatted text with tickable checklists and links that open
- Content respects safe areas and the keyboard: the cursor line is never hidden behind the keyboard or toolbar
- Edge-to-edge layout, with system bars drawn over the app's background

### Search

Finding a note fast.

- Search field focused, with the keyboard shown, when the screen opens; a clear (×) button once there is text
- Results with title and a snippet with matches highlighted, in a scrollable list; scrolling hides the keyboard
- Empty states for "start typing" and "no results"
- Back closes search and returns to the notes list

### Dialog

- **Delete confirmation:** title "Delete "<note title>"?", body "This note will be deleted permanently. This can't be undone." Actions: **Cancel** (default focus) and **Delete** (styled as destructive). Dismissing the dialog in any other way counts as Cancel.

## Quality standards

These apply to every feature above.

### Accessibility

- Every interactive element has an accessible name, role, and state (for example "Checklist item, Buy milk, not checked").
- Screen reader (TalkBack, VoiceOver) navigation follows the visual order; headings in preview are exposed as headings.
- Text scales with the system font size up to 200% without clipping or overlap. Layouts reflow instead of truncating important content.
- Touch targets are at least 48 × 48 dp (Android) / 44 × 44 pt (iOS).
- Information is never carried by color alone (pins, highlights, checked state).
- Animations respect reduce-motion settings.
- With a hardware keyboard, everything is reachable by Tab or arrow keys, with a visible focus indicator, and these shortcuts work: new note (⌘/Ctrl+N), search (⌘/Ctrl+F), toggle preview (⌘/Ctrl+E), bold (⌘/Ctrl+B), italic (⌘/Ctrl+I), link (⌘/Ctrl+K).

### Performance

| Interaction | Budget |
|---|---|
| Cold start to interactive notes list | ≤ 1.5 s on a mid-range device |
| Open a note (≤ 100 KB) | ≤ 300 ms to first content |
| Typing latency in the editor, including highlighting | One frame (16 ms at 60 Hz); no dropped frames |
| Notes list scroll with 10,000 notes | No dropped frames |
| Search update with 10,000 notes | ≤ 100 ms |
| Toggle Edit ↔ Preview (≤ 100 KB) | ≤ 200 ms |

- Database, file, and parsing work never runs on the main thread.
- Very large notes (up to 5 MB) must stay editable without freezing the UI. A degraded mode, such as turning off syntax highlighting above a size threshold, is acceptable.

### Reliability and data integrity

- Notes are stored in a local database with transactional writes; a save is all-or-nothing.
- Screen state (open note, mode, scroll, search query, draft text) survives rotation, backgrounding, and process death.
- The database schema is versioned, and upgrading the app migrates data without losing it. Migrations are covered by tests.
- Notes are included in the platform's standard device backup (Android Auto Backup, iCloud device backup). This is the operating system's backup, not sync, so it stays within scope.

### Privacy and security

- No network permission is requested or used, except handing links to the system browser.
- No analytics, tracking, or third-party SDKs that send data off the device.
- Imported content is treated as untrusted text: HTML is never rendered or run, and only safe link schemes open.

### Content and localization

- All user-facing text lives in string resources, ready for translation; nothing is hard-coded.
- Dates, times, and relative times use the device locale and 12/24-hour setting.
- Layouts support right-to-left languages, and right-to-left text renders correctly in both editor and preview.
- Microcopy is short, plain, and in sentence case. Error messages say what happened and what to do next, never just "Error".

### Platform conventions

- **Android:** Material 3 components, system back and predictive back gesture, edge-to-edge, and a floating action button for create.
- **iOS:** swipe-back navigation, safe areas, Dynamic Type, native share sheet and file picker, and iOS-style alerts for the delete dialog.
- Shared look and behavior everywhere else, so a user switching devices feels at home.

## Out of scope

These are deliberately left out to keep the app focused. Several make good extensions later.

- Accounts, sync, sharing, and collaboration (the app has no backend)
- Linking between notes (wiki links, backlinks)
- Bulk import (import is one note at a time)
- Bulk export of all notes (export is per note)
- Images and file attachments in notes
- Daily notes and note templates
- Version history and undoing deletions (in-session typing undo in the editor is still supported)
- Tags (`#tag` syntax, a tag browser, filtering by tag)
- Folders
- Encryption or app lock
- Custom sort orders, multi-select, and note-level settings
