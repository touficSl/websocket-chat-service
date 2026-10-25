/*
 * Mock of the external authentication service - LOCAL DEMO ONLY, never deploy it.
 * Implements the three calls the messenger depends on:
 *   POST /getCustomerByName      (web client)  -> customerId / customerKey
 *   POST /token/generateToken    (web client)  -> token
 *   POST /ncs/token/validateToken (messenger)  -> username (= customerId) of the token
 * Tokens are random and kept in memory.
 */
const http = require('http');
const crypto = require('crypto');

const PORT = 9099;
const ALLOWED_ORIGIN = process.env.ALLOWED_ORIGIN || 'http://localhost:8080';
const CUSTOMERS = { demohotel: { customerId: 'demo', customerKey: 'demo-key' } };
const tokens = new Map();

const send = (res, status, body) => {
  res.writeHead(status, {
    'content-type': 'application/json',
    'access-control-allow-origin': ALLOWED_ORIGIN,
    'access-control-allow-headers': 'content-type',
    'access-control-allow-methods': 'POST, OPTIONS',
    'vary': 'origin'
  });
  res.end(JSON.stringify(body));
};

const fail = (res, message) => send(res, 200, { success: false, errors: [{ errorCode: 815, errorMessage: message }] });

http.createServer((req, res) => {
  if (req.method === 'OPTIONS') return send(res, 204, {});
  if (req.method !== 'POST') return send(res, 405, {});

  let body = '';
  req.on('data', chunk => { body += chunk; if (body.length > 100000) req.destroy(); });
  req.on('end', () => {
    try {
      if (req.url === '/getCustomerByName') {
        const match = /name="customerName"\r\n\r\n([^\r\n]*)/.exec(body);
        const customer = match && CUSTOMERS[match[1]];
        return customer ? send(res, 200, { success: true, data: customer }) : fail(res, 'Unknown customer');
      }

      const json = body ? JSON.parse(body) : {};

      if (req.url === '/token/generateToken') {
        const customer = Object.values(CUSTOMERS).find(c => c.customerId === json.customerId && c.customerKey === json.customerKey);
        if (!customer) return fail(res, 'Invalid customer credentials');
        const token = crypto.randomBytes(32).toString('base64url');
        tokens.set(token, customer.customerId);
        return send(res, 200, { success: true, data: { token } });
      }

      if (req.url === '/ncs/token/validateToken') {
        const username = tokens.get(json.token);
        return username ? send(res, 200, { success: true, data: { username } }) : fail(res, 'Invalid token');
      }

      send(res, 404, {});
    } catch (e) {
      fail(res, 'Bad request');
    }
  });
}).listen(PORT, () => console.log(`mock auth service listening on ${PORT}`));
