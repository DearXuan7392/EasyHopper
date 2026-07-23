## CodeGraph

In repositories indexed by CodeGraph (a `.codegraph/` directory exists at the repo root), reach for it BEFORE grep/find
or reading files when you need to understand or locate code:

- **Initialization check**: At the start of each conversation, check if `.codegraph/` directory exists at the repo root.
    - If it does NOT exist: Run `codegraph init` to create and initialize the index.
    - If it EXISTS: Run `codegraph sync` to update the index with recent changes (preferred for speed), or
      `codegraph index` to rebuild from scratch (if significant changes occurred).

- **MCP tool** (when available): `codegraph_explore` answers most code questions in one call — the relevant symbols'
  verbatim source plus the call paths between them, including dynamic-dispatch hops grep can't follow. Name a file or
  symbol in the query to read its current line-numbered source. If it's listed but deferred, load it by name via tool
  search.

- **Shell** (always works): `codegraph explore "<symbol names or question>"` prints the same output.
