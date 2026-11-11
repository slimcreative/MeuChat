// Proxy: guarda a ANTHROPIC_API_KEY no servidor, fora do APK.
// Requer Node 18+ (fetch nativo). Sem dependências.
import http from 'node:http';

const KEY = process.env.ANTHROPIC_API_KEY;
const PORT = process.env.PORT || 3000;
const MAX_BODY = 6 * 1024 * 1024; // ~6 MB, cobre o JPEG de 1080px
const TONES = new Set(['formal', 'casual', 'engraçado', 'carinhoso', 'direto', 'negociação']);

function send(res, code, obj) {
  res.writeHead(code, { 'content-type': 'application/json; charset=utf-8' });
  res.end(JSON.stringify(obj));
}

const server = http.createServer(async (req, res) => {
  if (req.method !== 'POST' || req.url !== '/reply-screen') return send(res, 404, { error: 'not found' });

  let raw = '';
  for await (const chunk of req) {
    raw += chunk;
    if (raw.length > MAX_BODY) return send(res, 413, { error: 'payload grande demais' });
  }

  let image, tone;
  try {
    ({ image, tone } = JSON.parse(raw));
  } catch {
    return send(res, 400, { error: 'JSON inválido' });
  }
  if (!image || !TONES.has(tone)) return send(res, 400, { error: 'imagem ou tom inválido' });

  const prompt = `Esta é a tela de um app de mensagens.
1) Identifique quem enviou a última mensagem e o que essa pessoa quer.
2) Gere 3 respostas no tom "${tone}", em português do Brasil, curtas e naturais.
Responda APENAS com JSON: {"options": ["...", "...", "..."]}`;

  try {
    const up = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: {
        'x-api-key': KEY,
        'anthropic-version': '2023-06-01',
        'content-type': 'application/json',
      },
      body: JSON.stringify({
        model: 'claude-haiku-5-5',
        max_tokens: 500,
        messages: [{
          role: 'user',
          content: [
            { type: 'image', source: { type: 'base64', media_type: 'image/jpeg', data: image } },
            { type: 'text', text: prompt },
          ],
        }],
      }),
    });
    if (!up.ok) return send(res, 502, { error: `upstream ${up.status}` });

    const data = await up.json();
    const text = data.content?.[0]?.text ?? '';
    const m = text.match(/\{[\s\S]*\}/);
    const options = m ? JSON.parse(m[0]).options ?? [] : [];
    send(res, 200, options.slice(0, 3));
  } catch (e) {
    send(res, 500, { error: String(e) });
  }
});

server.listen(PORT, () => console.log(`proxy na porta ${PORT}`));
