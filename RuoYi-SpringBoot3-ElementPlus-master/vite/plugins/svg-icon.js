import fs from 'fs'
import path from 'path'

const SVG_REGISTER_ID = 'virtual:svg-icons-register'
const RESOLVED_SVG_REGISTER_ID = `\0${SVG_REGISTER_ID}`

function readSvgFiles(dir) {
  if (!fs.existsSync(dir)) {
    return []
  }

  return fs.readdirSync(dir, { withFileTypes: true }).flatMap((entry) => {
    const fullPath = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      return readSvgFiles(fullPath)
    }
    return entry.isFile() && entry.name.endsWith('.svg') ? [fullPath] : []
  })
}

function getSymbolId(file, iconDir) {
  const relativeName = path
    .relative(iconDir, file)
    .replace(/\\/g, '/')
    .replace(/\.svg$/i, '')

  return `icon-${relativeName.replace(/\//g, '-')}`
}

function toSymbol(svg, symbolId) {
  const openTag = svg.match(/<svg\s+([^>]*)>/i)
  const attrs = openTag?.[1]
    ?.replace(/\s?(xmlns|version|width|height)="[^"]*"/gi, '')
    .trim()

  const body = svg
    .replace(/<\?xml[\s\S]*?\?>/g, '')
    .replace(/<!DOCTYPE[\s\S]*?>/gi, '')
    .replace(/<svg[^>]*>/i, '')
    .replace(/<\/svg>\s*$/i, '')

  return `<symbol id="${symbolId}" ${attrs || ''}>${body}</symbol>`
}

function buildSprite(iconDir) {
  const symbols = readSvgFiles(iconDir)
    .map((file) => toSymbol(fs.readFileSync(file, 'utf-8'), getSymbolId(file, iconDir)))
    .join('')

  return `<svg xmlns="http://www.w3.org/2000/svg" style="position:absolute;width:0;height:0;overflow:hidden" aria-hidden="true">${symbols}</svg>`
}

export default function createSvgIcon() {
  const iconDir = path.resolve(process.cwd(), 'src/assets/icons/svg')

  return {
    name: 'local-svg-icons',
    resolveId(id) {
      if (id === SVG_REGISTER_ID) {
        return RESOLVED_SVG_REGISTER_ID
      }
    },
    load(id) {
      if (id !== RESOLVED_SVG_REGISTER_ID) {
        return null
      }

      const sprite = buildSprite(iconDir)
      return `
const sprite = ${JSON.stringify(sprite)}
if (typeof document !== 'undefined' && !document.getElementById('__svg__icons__dom__')) {
  const container = document.createElement('div')
  container.id = '__svg__icons__dom__'
  container.innerHTML = sprite
  document.body.insertBefore(container.firstChild, document.body.firstChild)
}
`
    }
  }
}
