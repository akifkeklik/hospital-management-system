import { test, beforeEach, afterEach } from 'node:test';
import assert from 'node:assert/strict';
import { fetchAPI, DepartmentService } from '../src/services/api.js';

const realFetch = globalThis.fetch;
let calls;

function respondWith(status, body, contentType = 'application/json') {
  globalThis.fetch = async (url, config) => {
    calls.push({ url, config });
    const text = body === undefined ? null : typeof body === 'string' ? body : JSON.stringify(body);
    return new Response(status === 204 ? null : text, { status, headers: { 'content-type': contentType } });
  };
}

beforeEach(() => {
  calls = [];
});

afterEach(() => {
  globalThis.fetch = realFetch;
});

test('sends cookies and JSON headers to the /api base', async () => {
  respondWith(200, { ok: true });
  assert.deepEqual(await fetchAPI('/ping', { headers: { 'X-Trace': '1' } }), { ok: true });
  const { url, config } = calls[0];
  assert.match(url, /\/api\/ping$/);
  assert.equal(config.credentials, 'include');
  assert.equal(config.headers['Content-Type'], 'application/json');
  assert.equal(config.headers['X-Trace'], '1');
});

test('joins validation errors from the backend into one message', async () => {
  respondWith(400, { validationErrors: [{ message: 'Ad zorunludur.' }, { message: 'TC 11 haneli olmalı.' }] });
  await assert.rejects(fetchAPI('/patients', { method: 'POST' }), { message: 'Ad zorunludur.\nTC 11 haneli olmalı.' });
});

test('uses the backend message, or the raw text when the body is not JSON', async () => {
  respondWith(409, { message: 'Bu saat dolu.' });
  await assert.rejects(fetchAPI('/appointments'), { message: 'Bu saat dolu.' });

  respondWith(502, 'Bad gateway from proxy', 'text/plain');
  await assert.rejects(fetchAPI('/appointments'), { message: 'Bad gateway from proxy' });
});

test('returns null for 204 and for non-JSON success bodies', async () => {
  respondWith(204);
  assert.equal(await fetchAPI('/departments/1', { method: 'DELETE' }), null);

  respondWith(200, 'ok', 'text/plain');
  assert.equal(await fetchAPI('/health'), null);
});

test('a 401 outside the browser is an error, not a silent hang', async () => {
  respondWith(401, { message: 'Oturum gerekli.' });
  await assert.rejects(fetchAPI('/auth/me'), { message: 'Oturum gerekli.' });
});

test('search queries are URL-encoded', async () => {
  respondWith(200, { content: [] });
  await DepartmentService.search('Kulak & Burun', 0, 5);
  assert.match(calls[0].url, /\/departments\/search\?query=Kulak%20%26%20Burun&page=0&size=5$/);
});
