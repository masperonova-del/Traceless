from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(
    title="TraceLess API",
    version="0.1.0",
    description="Privacy-first API scaffold; remote analysis is not enabled yet.",
)


class DiagnosticSummary(BaseModel):
    """Minimal, user-approved summary; do not include IPs or device identifiers."""

    good: int = Field(ge=0, le=100)
    warning: int = Field(ge=0, le=100)
    unavailable: int = Field(ge=0, le=100)
    checks: list[str] = Field(default_factory=list, max_length=30)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "ok", "service": "traceless-api"}


@app.post("/v1/analyze")
def analyze(summary: DiagnosticSummary) -> dict[str, str]:
    # Deliberately fail closed until a provider and safeguards are configured.
    _ = summary
    raise HTTPException(
        status_code=503,
        detail="Remote analysis is not configured. No diagnostic data was processed.",
    )
