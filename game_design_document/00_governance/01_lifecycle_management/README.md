# Lifecycle Management

## Lifecycle States
- **Draft**: working design content exists, but it is still being shaped or challenged.
- **In Review**: the content is coherent enough to be evaluated as a candidate truth.
- **Approved**: this is the accepted GDD direction unless a later decision changes it.
- **Implemented**: the live game meaningfully reflects the approved design.
- **Superseded**: a newer design or decision has replaced this content.

## State Change Rule
Moving a topic between states should happen deliberately. A topic is not "approved" just because text exists. It is approved when the project is willing to treat it as the reference point for future implementation and review.

## Important Distinction
`Approved` does not mean `Implemented`.
`Implemented` does not automatically mean `still correct`.

This matters because the repo and the GDD can drift unless both sides are checked.

## Supersession Rule
If a topic is replaced, do not silently overwrite the old meaning. Mark the old design as superseded and point toward:
- the replacement topic
- the relevant decision log entry
- any implementation consequence if relevant
