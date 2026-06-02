# Frontend

This folder contains a standalone frontend demo and automated tests.

## Run page locally

```powershell
cd frontend
python -m http.server 8080
```

Then open `http://localhost:8080`.

## Run tests

```powershell
cd frontend
npm install
npm test
npm test -- --coverage
```

- `npm test`: run all frontend tests
- `npm test -- --coverage`: generate coverage report in `frontend/coverage/`

## Test summary against homework

- Component render/interaction tests: 8
- Mock API tests (including failure): 4
- Total frontend tests: 12
- Core file coverage (`app.js` lines): 91.83%
# Frontend Source of Truth

- 页面/样式/前端脚本源码维护在 `frontend/web/`。
- 后端运行目录 `backend/src/main/resources/static/` 作为发布产物目录，不手工修改。
- 每次前端改动后执行：

```bash
npm run sync:backend-static
```

将 `frontend/web/` 自动同步到后端 static 目录，确保 `http://localhost:9099` 页面与前端源码一致。
