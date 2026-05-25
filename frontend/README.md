# 前端管理端

Vue3 + Element Plus + Vite frontend.

## Install

```powershell
cd D:\IDEA\smart-parking\frontend
npm.cmd install
```

## Run

```powershell
npm.cmd run dev -- --host 127.0.0.1 --port 5173
```

Open:

```text
http://127.0.0.1:5173/admin/
```

The Vite proxy forwards `/dev-api` and `/prod-api` to `http://127.0.0.1:8087`.

## Build

```powershell
npm.cmd run build:prod
```
