# Onyx Platform UI

React-based frontend for the Onyx Platform.

## Structure

```
platform-ui/
├── vaadin-legacy/   # Legacy Vaadin frontend — to be removed once React rewrite is complete
└── README.md
```

## Vaadin Legacy

The `vaadin-legacy/` directory contains the original Java Vaadin Flow frontend that was built alongside the early version of the platform backend. It serves as a reference for the feature set, user flows, and domain logic that the React rewrite must cover.

It is preserved here temporarily and will be removed once the React application reaches feature parity and is ready for production.

## React Rewrite

The new frontend will be built with React and will replace `vaadin-legacy/` entirely. It will be a fully decoupled single-page application consuming the platform's REST API.

The React application will be structured here at the root of this repository once scaffolding begins.

## Related Repositories

- [platform](../platform) — Core backend API
- [payments](../payments) — Payment processing service (WiPay)

## License

Proprietary — All rights reserved by Onyx Softworks.
