# cardinalitydb

This is a thin "database" service on top of redis designed to keep track of set cardinalities, with expiry for the whole set.

It's built using tapir and currently depends on a SNAPSHOT build of redis4cats that I contributed hyperloglog support to, once that's in a full release we can upgrade to stable.
