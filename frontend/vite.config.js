import { defineConfig, loadEnv } from 'vite'
import path from 'path'
import createVitePlugins from './vite/plugins'

// https://vitejs.dev/config/
export default defineConfig(({ mode, command }) => {
  const env = loadEnv(mode, process.cwd())
  const { VITE_APP_ENV } = env
  const apiProxyTarget = env.VITE_API_BASE_URL || process.env.VITE_API_BASE_URL || 'http://localhost:8087'
  const devServerPort = Number(env.VITE_DEV_SERVER_PORT || process.env.VITE_DEV_SERVER_PORT || 5173)

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
      port: devServerPort,
      host: true,
      open: true,
      proxy: {
        '/dev-api': {
          target: apiProxyTarget,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/dev-api/, '')
        },
        '/prod-api': {
          target: apiProxyTarget,
          changeOrigin: true,
          rewrite: (p) => p.replace(/^\/prod-api/, '')
        },
        '^/v3/api-docs/(.*)': {
          target: apiProxyTarget,
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
