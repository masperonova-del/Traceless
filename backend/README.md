# TraceLess API (prototype)

Backend scaffold for the optional remote-analysis layer. The Android app does not call an AI provider directly.

## Run locally
```bash
python -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Open `/docs` for the local API schema and `/health` for a health check.

## Privacy and security baseline
- Send only a user-approved, minimal diagnostic summary; never upload raw device identifiers or the public IP by default.
- Do not persist request bodies or diagnostic payloads.
- Keep provider credentials in server environment variables, never in the Android app.
- The analysis route currently returns a clear “not configured” response; no remote AI provider is connected yet.
- Before deployment: add authentication, rate limiting, request-size limits, abuse monitoring, and HTTPS at the hosting layer.
