import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

// Docker: frontend requests /prod-api/ through nginx to backend.
// Local dev: vite proxy forwards /prod-api to http://localhost:8087.
const BACKEND_URL = process.env.VITE_API_BASE_URL || '/prod-api'
const baseUrl = BACKEND_URL

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  const { VITE_APP_ENV } = env

  return {
    base: VITE_APP_ENV === 'production' ? '/admin/' : '/admin/',
    plugins: createVitePlugins(env, command === 'build'),
    resolve: {
      alias: {
        '~': path.resolve(__dirname, './'),
        '@': path.resolve(__dirname, './src')
      },
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.vue']
    },
    build: {
      copyPublicDir: false,
      sourcemap: command === 'build' ? false : 'inline',
      outDir: 'dist',
      assetsDir: 'assets',
      chunkSizeWarningLimit: 2000,
      minify: 'terser',
      terserOptions: {
        compress: {
          drop_console: true,
          drop_debugger: true,
          pure_funcs: ['console.log', 'console.info']
        },
        format: {
          comments: false
        }
      },
      rollupOptions: {
        output: {
          chunkFileNames: 'static/js/[name]-[hash].js',
          entryFileNames: 'static/js/[name]-[hash].js',
          assetFileNames: 'static/[ext]/[name]-[hash].[ext]',
          manualChunks: {
            vendor: ['lodash-es', 'axios', 'vue', 'vue-router', 'pinia', 'element-plus', '@element-plus/icons-vue']
          }
        }
      }
    },
    server: {
      port: 80,
      host: true,
      open: true,
      proxy: {
        '/prod-api': {
          target: 'http://localhost:8087',
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/prod-api/, '')
        },
        '^/v3/api-docs/(.*)': {
          target: baseUrl,
          changeOrigin: true
        }
      }
    },
    css: {
      postcss: {
        plugins: [
          {
            postcssPlugin: 'internal:charset-removal',
            AtRule: {
              charset: (atRule) => {
                if (atRule.name === 'charset') {
                  atRule.remove()
                }
              }
            }
          }
        ]
      }
    }
  }
})
