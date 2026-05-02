# Smart Parking 前端

本目录是 Smart Parking 管理端前端，基于 Vue3、Element Plus 和 Vite。前端提供系统管理、车位状态、区域/相机/ROI 配置、AI 事件和告警管理入口。

## 技术栈

| 组件 | 当前版本 |
| --- | --- |
| Vue | 3.5.33 |
| Vite | 6.4.2 |
| Element Plus | 2.13.7 |
| Pinia | 3.0.2 |
| Vue Router | 4.5.1 |
| Axios | 1.15.2 |
| ECharts | 5.6.0 |
| Sass | `sass-embedded` 1.99.0 |

## 常用命令

```powershell
npm.cmd install
npm.cmd run dev
npm.cmd run build:prod
npm.cmd run build:stage
npm.cmd audit
```

开发模式默认监听 80，代理配置在 `vite.config.js`：

```text
/prod-api -> http://localhost:8087
```

Docker/Nginx 访问路径：

```text
http://localhost/admin/
```

## 安全依赖说明

当前 `npm audit` 为 0。

为修复依赖漏洞，项目做了这些调整：

- 升级 `vite` 到 6.4.2。
- 升级 `sass-embedded` 到 1.99.0。
- 使用 `overrides` 固定 `follow-redirects`、`minimatch`、`brace-expansion`、`picomatch`、`rollup`、`immutable`。
- 移除未维护的 `vite-plugin-svg-icons`，改用本地 `vite/plugins/svg-icon.js` 实现 `virtual:svg-icons-register`。

注意：`package-lock.json` 当前未纳入 Git，依赖安全约束依赖 `package.json` 中的精确版本和 `overrides`。

## 目录结构

```text
src/
├─ api/          接口封装
├─ assets/       样式、图片、SVG 图标
├─ components/   通用组件
├─ layout/       后台布局
├─ plugins/      全局插件
├─ router/       路由
├─ store/        Pinia 状态
├─ utils/        请求、鉴权、格式化等工具
└─ views/        页面

vite/
└─ plugins/      Vite 插件配置
```

## SVG 图标

入口仍然保持：

```js
import 'virtual:svg-icons-register'
```

本地插件会扫描 `src/assets/icons/svg`，生成 `symbol` 注入页面。组件使用方式不变：

```vue
<svg-icon icon-class="dashboard" />
```

## 构建验证

```powershell
npm.cmd audit
npm.cmd run build:prod
npm.cmd run build:stage
```

生产构建只生成 `dist`，不会自动 FTP 上传。需要部署时单独运行：

```powershell
npm.cmd run deploy:ftp
```
