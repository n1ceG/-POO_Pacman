// Verificación lógica sin dependencias. No sustituye la prueba visual en navegador.
const fs = require('node:fs');
const vm = require('node:vm');
const assert = require('node:assert/strict');
const html = fs.readFileSync(__dirname + '/estado-tpo.html', 'utf8');
const catalog = JSON.parse(html.match(/<script id="catalog" type="application\/json">([\s\S]*?)<\/script>/)[1]);
const script = html.match(/<script>([\s\S]*?)<\/script>/)[1];
const ids = [...html.matchAll(/\bid="([^"]+)"/g)].map(m => m[1]);
const tasks = catalog.objects.flatMap(o => o.groups.flatMap(g => g.items));
assert.equal(new Set(tasks.map(i => i[0])).size, tasks.length);
assert.equal(new Set(ids).size, ids.length);
let assertions = 2;
function check(actual, expected) { assert.deepEqual(actual, expected); assertions++; }
function boot({ stored = null, failStorage = false, confirm = true } = {}) {
  const nodes = new Map();
  let lastBlob, downloadClicked = false, storageWrites = 0;
  class Node {
    constructor(tag) { this.tag = tag; this.children = []; this.value = ''; this.events = {}; this.checked = false; this.hidden = false; }
    set id(id) { this._id = id; nodes.set(id, this); } get id() { return this._id; }
    append(...children) { this.children.push(...children); }
    replaceChildren(...children) { this.children = children; }
    setAttribute(key, value) { this[key] = value; }
    addEventListener(event, callback) { this.events[event] = callback; }
    focus() {} scrollIntoView() {} remove() {}
    click() { if (this.tag === 'a' && this.download) downloadClicked = true; if (this.events.click) return this.events.click(); }
  }
  for (const id of ids) { const n = new Node('div'); n.id = id; }
  nodes.get('catalog').textContent = JSON.stringify(catalog);
  nodes.get('filter').value = 'all';
  const document = { getElementById: id => nodes.get(id), createElement: tag => new Node(tag), createTextNode: text => ({ text }), body: new Node('body') };
  const sandbox = { document, localStorage: { getItem() { if (failStorage) throw Error('Storage unavailable'); return stored; }, setItem(key, value) { if (failStorage) throw Error('Storage unavailable'); stored = value; storageWrites++; } },
    URL: { createObjectURL(blob) { lastBlob = blob; return 'blob:test'; }, revokeObjectURL() {} }, Blob,
    setTimeout() { return 1; }, clearTimeout() {}, window: { confirm: () => confirm, addEventListener() {} }, console };
  vm.runInNewContext(script, sandbox, { filename: 'estado-tpo.html' });
  return { nodes, get stored() { return stored; }, get lastBlob() { return lastBlob; }, get downloadClicked() { return downloadClicked; }, get storageWrites() { return storageWrites; } };
}
async function run() {
  let app = boot();
  const baseDone = tasks.filter(i => i[4]).length;
  check(app.nodes.get('global-progress').max, tasks.length);
  check(app.nodes.get('global-progress').value, baseDone);
  const box = app.nodes.get('task-pa-score'); box.checked = true; box.events.change();
  check(app.nodes.get('global-progress').value, baseDone + 1);
  check(JSON.parse(app.stored).done['pa-score'], true);
  check(boot({ stored: app.stored }).nodes.get('task-pa-score').checked, true);
  app.nodes.get('export').click();
  check(app.downloadClicked, true);
  const exported = JSON.parse(await app.lastBlob.text());
  check(exported.project, catalog.project);
  check(exported.done['pa-score'], true);
  check(Object.keys(exported.done).length, tasks.length);
  const restored = boot();
  await restored.nodes.get('file').events.change({ target: { files: [{ size: 20000, text: async () => JSON.stringify(exported) }], value: 'import' } });
  check(restored.nodes.get('task-pa-score').checked, true);
  const previous = restored.stored;
  await restored.nodes.get('file').events.change({ target: { files: [{ size: 10, text: async () => '{"schema":1}' }], value: 'bad' } });
  check(restored.stored, previous);
  check(restored.nodes.get('toast').textContent.startsWith('No se importó:'), true);
  const cancelled = boot({ confirm: false });
  await cancelled.nodes.get('file').events.change({ target: { files: [{ size: 20000, text: async () => JSON.stringify(exported) }], value: 'cancel' } });
  check(cancelled.storageWrites, 0);
  const corrupt = boot({ stored: 'invalid json' });
  const corruptBox = corrupt.nodes.get('task-pa-score'); corruptBox.checked = true; corruptBox.events.change();
  check(corrupt.stored, 'invalid json');
  check(corrupt.nodes.get('save-title').textContent, 'Guardado automático no disponible');
  check(boot({ failStorage: true }).nodes.get('global-progress').value, baseDone);
  app.nodes.get('search').value = 'serComido'; app.nodes.get('search').events.input();
  check(app.nodes.get('content').children.length > 0, true);
  app.nodes.get('search').value = ''; app.nodes.get('filter').value = 'pending'; app.nodes.get('filter').events.change();
  check(app.nodes.get('content').children.length > 0, true);
  for (const o of catalog.objects) for (const relation of o.relations) assert.ok(catalog.objects.some(other => other.id === relation[0]));
  for (const t of tasks) for (const c of t[5].split(' ').filter(Boolean)) assert.ok(catalog.cards[c]);
  console.log(`OK: ${assertions} comprobaciones; ${catalog.objects.length} secciones, ${tasks.length} objetivos, ${baseDone} marcados inicialmente.`);
  console.log('Incluye cambio de marca, recarga, exportación/importación, importación inválida/cancelada, almacenamiento inaccesible y conservación de datos corruptos.');
  console.log('La prueba usa un DOM simulado; no verifica apariencia ni descargas reales del navegador.');
}
run().catch(error => { console.error(error); process.exitCode = 1; });
