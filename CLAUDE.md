# Community Parking Occupancy Local Notes

This project runs locally on Windows and no longer uses Docker.

## Services

| Service | Path | Command |
| --- | --- | --- |
| Backend | `backend/` | `java -jar .\ruoyi-admin\target\RuoyiSpringBoot3.jar` |
| Frontend | `frontend/` | `npm.cmd run dev -- --host 127.0.0.1 --port 5173` |
| AI | `ai-service/` | `conda run -n python39 python -m uvicorn main_api:app --host 0.0.0.0 --port 8000` |

## One-command local start

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\scripts\start-demo-local.ps1
```

## Database

Use local MySQL database `smart_parking` on port `3306`. Do not operate on unrelated databases such as `finpilot_dev`.
