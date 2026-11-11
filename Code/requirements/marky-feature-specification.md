# Marky: Feature Specification

2026-10-02 · @Devrath

> Source: https://markdown-editor.notion.site/

## Overview

The app is a local-first notes app: people write notes in Markdown, read them nicely formatted, and find any note quickly through search.

It runs fully offline with no account, server, or sync. All notes live on the device, and users can import or export any single note as a plain `.md` file at any time.

**Why this app:** Markdown is a fast, portable way to write, but many notes apps either hide it behind rich-text editors or bury it under features. This app keeps only the core: write in Markdown, read it formatted, and find any note in seconds.

## Goals and principles

- **Writing comes first.** Opening the app and starting a note takes one tap. Nothing should ask the user to organize before they write.
- **Formatting lives in the text.** Formatting is typed as Markdown directly into the note, not set through menus. This keeps notes portable and the workflow fast.
- **The user owns their data.** Notes work offline, never leave the device unless exported, and export as standard Markdown that opens in any other editor.

## Features

The app has four feature areas. Each item says what the feature does and why it's there.

### 1. Managing notes

- **Create a note.** One action creates an empty note and opens it for writing. *Why:* capturing a thought should never wait on setup.
- **Autosave.** Changes save automatically while typing; there is no save button. *Why:* losing writing is the worst thing a notes app can do.
- **Notes list.** All notes appear in a list sorted by last edited, each showing its title, a short preview of the text, and when it was edited. *Why:* the most recent work is usually what people return to.
- **Pin notes.** Users can pin notes so they stay at the top of the list. *Why:* a few notes (an index, a to-do list) are opened far more than the rest.
- **Delete a note.** Deleting asks for confirmation. *Why:* deletion can't be undone.

### 2. Writing in Markdown

- **Markdown editor.** Notes are written as plain Markdown text: headings, bold, italic, lists, checklists, quotes, code blocks, and web links. *Why:* Markdown is fast to type, readable as plain text, and portable to other tools.
- **Formatting toolbar.** In edit mode, a toolbar above the keyboard inserts Markdown syntax for headings, bold, italic, lists, checklists, quotes, code blocks, and web links. It types the same Markdown the user could type by hand and wraps selected text where that applies. *Why:* characters like `#`, `*` and `` ` `` take several taps on a phone keyboard.
- **Preview mode.** Users can switch between editing the raw text and reading the formatted version. *Why:* writing and reading are different activities; reading is easier without the syntax visible.
- **Interactive preview.** In preview, checklist items can be ticked and web links open in the browser. *Why:* preview is where people use their notes, not just look at them.

### 3. Search

- **Full-text search.** Searching looks through all titles and note text. Title matches rank above text matches. *Why:* as the number of notes grows, search becomes the main way to find things.
- **Highlighted matches.** Results show a snippet with the search words highlighted. *Why:* users can pick the right result without opening several notes.

### 4. Import and export

- **Import a note.** From the notes list, users can pick a single `.md` file to import as a new note. The note's title is the file name without the `.md` extension, and the file's contents become the note's text. There is no bulk import. *Why:* users can bring a note in from another editor, including one they exported earlier.
- **After importing.** The user stays on the notes list. The imported note counts as created and edited at the time of import, so it appears at the top of the unpinned notes. *Why:* the user sees the result right away without leaving the list.
- **Import errors.** If the file can't be imported, a snackbar on the notes list says so. *Why:* a failed import should be visible but shouldn't block the user.
- **Duplicate titles on import.** Titles don't have to be unique. If a note with the same title already exists, the imported note keeps the file name as its title unchanged, so two notes can both be called "Ideas". *Why:* every note has its own ID, so importing never overwrites an existing note, and the imported note is easy to spot at the top of the list.
- **Export a note.** From the note screen, any note can be exported as a single `.md` file named after its title. There is no bulk export. *Why:* users own their writing and can take any note to another editor.

### General

- **Sample notes on first launch.** New users start with a few example notes that show Markdown formatting and how the app works. *Why:* an empty app doesn't show what's possible; examples do.
- **Light and dark theme.** Follows the system setting. *Why:* people write at all hours.
- **Helpful empty states.** Empty screens (no notes, no search results) explain what to do next. *Why:* a blank screen gives a beginner no direction.

## Screens

The app has three screens and one dialog. The notes list is the home screen; every other screen is reachable from it in one step.

### Notes list (home)

Where users land and pick what to work on.

- Pinned notes on top, then all other notes by last edited
- Each entry: title, text preview, time since last edit, pin indicator
- Button to create a new note
- Button to import a `.md` file as a new note (icon only)
- Snackbar for import errors
- Entry point to search
- Empty state for a user with no notes

### Note

Where users read and write a note.

- Title, editable in place
- Edit mode: the raw Markdown text, with the formatting toolbar above the keyboard
- Preview mode: formatted text with tickable checklists
- Switch between edit and preview
- Actions: pin or unpin, export as `.md`, delete

### Search

Finding a note fast.

- Search field, focused when the screen opens
- Results with title and a snippet with matches highlighted
- Empty states for "start typing" and "no results"

### Dialog

- **Delete confirmation:** names the note being deleted

## Out of scope

These are deliberately left out to keep the app focused. Several make good extensions later.

- Accounts, sync, sharing, and collaboration (the app has no backend)
- Linking between notes (wiki links, backlinks)
- Bulk import (import is one note at a time)
- Bulk export of all notes (export is per note)
- Images and file attachments in notes
- Daily notes and note templates
- Version history and undoing deletions
- Tags (`#tag` syntax, a tag browser, filtering by tag)
- Folders
- Encryption or app lock
