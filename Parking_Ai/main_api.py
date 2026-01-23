# main_api.py
from fastapi import FastAPI
from app.api import router

app = FastAPI(
    title="Parking AI Service",
    version="1.0.0"
)

app.include_router(router)


if __name__ == "__main__":
    import uvicorn

    uvicorn.run(
        "main_api:app",
        host="0.0.0.0",
        port=8000,
        reload=True
    )
