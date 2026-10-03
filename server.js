const http = require('http');
const https = require('https');
const fs = require('fs');
const path = require('path');
const url = require('url');

const PORT = 3000;
const GEMINI_API_KEY = process.env.GEMINI_API_KEY || '';
const MODEL_NAME = 'gemini-3.8-flash';

// Curated Books Catalog for Digital Library
const BOOKS_CATALOG = [
  {
    id: 'meditations',
    title: 'Meditations',
    author: 'Marcus Aurelius',
    category: 'Philosophy',
    year: '180 AD',
    pages: 184,
    rating: 4.9,
    description: 'Timeless stoic reflections written by the Roman Emperor on personal discipline, resilience, ethics, inner peace, and mastering one\'s own mind.',
    chapters: [
      {
        number: 1,
        title: 'Debts and Lessons from Mentors',
        content: `From my grandfather Verus, I learned good morals and the government of my temper. From the reputation and remembrance of my father, modesty and a manly character.\n\nFrom my mother, piety and beneficence, and abstinence, not only from evil deeds, but even from evil thoughts; and further, simplicity in my way of living, far removed from the habits of the rich.\n\nFrom Rusticus I received the impression that my character required improvement and discipline; and from him I learned not to be led astray to sophistic emulation, nor to writing on speculative subjects, nor to showing myself off as a man who does much discipline, or does benevolent acts in order to make a display.`,
        takeaways: [
          'Gratitude towards teachers and parents shapes foundational character.',
          'Avoid vanity, theatrical moralizing, and unconstructive gossip.',
          'Endurance and simplicity are essential buffers against adversity.'
        ]
      },
      {
        number: 2,
        title: 'On the Morning Attitude and Human Nature',
        content: `When you wake up in the morning, tell yourself: The people I deal with today will be meddling, ungrateful, arrogant, dishonest, jealous, and surly. They are like this because they cannot distinguish good from evil.\n\nBut I have seen the beauty of good, and the ugliness of evil, and have recognized that the wrongdoer has a nature related to my own—not of the same blood or birth, but the same mind, and possessing a share of the divine. And so none of them can hurt me. No one can implicate me in ugliness.\n\nNor can I feel angry at my fellow human, nor hate him. We were made to work together like feet, like hands, like the rows of the upper and lower teeth. To obstruct each other is contrary to nature.`,
        takeaways: [
          'Prepare your mind each morning for interpersonal friction without resentment.',
          'Other people\'s ignorance cannot damage your internal virtue unless you permit it.',
          'Humans are designed for cooperation, not adversarial sabotage.'
        ]
      }
    ]
  },
  {
    id: 'calculus_essentials',
    title: 'Calculus & Mathematical Analysis',
    author: 'Academic Study Series',
    category: 'Science & Math',
    year: 'Modern Edition',
    pages: 340,
    rating: 4.8,
    description: 'A rigorous, intuitive guide covering limits, differentiation, Riemann integration, multivariable calculus, and real-world physical applications.',
    chapters: [
      {
        number: 1,
        title: 'Limits, Continuity, and the ε-δ Foundation',
        content: `Calculus begins with the foundational problem of continuous change. Before Newton and Leibniz, mathematics could only measure static quantities. The breakthrough of calculus is the rigorous notion of the 'Limit'.\n\nFormally, the limit of f(x) as x approaches c is L if for every real number ε > 0, there exists a corresponding real number δ > 0 such that whenever 0 < |x - c| < δ, then |f(x) - L| < ε.\n\nContinuity on closed intervals guarantees the Intermediate Value Theorem, which ensures that continuous physical models reflect reality without sudden gaps.`,
        takeaways: [
          'Limits evaluate behavior where functions are algebraically indeterminate.',
          'Epsilon-delta provides the rigorous foundation of mathematical analysis.',
          'Continuity guarantees extreme values and intermediate states.'
        ]
      },
      {
        number: 2,
        title: 'The Derivative and the Chain Rule',
        content: `Geometrically, the derivative is the exact slope of the tangent line to the graph of a curve at any specific point. Physically, it represents instantaneous velocity and rate of change.\n\nThe Chain Rule is the linchpin of multivariable optimization and modern neural network backpropagation:\ndy/dx = (dy/du) * (du/dx)\n\nCritical points occur where f'(x) = 0, determining maximum efficiency and equilibrium.`,
        takeaways: [
          'Derivatives compute instantaneous rates of change.',
          'The Chain Rule breaks down complex composite functions.',
          'Critical points identify local maxima and minima.'
        ]
      }
    ]
  },
  {
    id: 'algorithms_illustrated',
    title: 'Algorithms & Data Structures',
    author: 'CS Core Series',
    category: 'CS & Coding',
    year: '2026 Edition',
    pages: 290,
    rating: 4.9,
    description: 'Master time complexity, recursive problem solving, dynamic programming, graph traversals, and algorithmic system design.',
    chapters: [
      {
        number: 1,
        title: 'Asymptotic Complexity & Big-O Notation',
        content: `In computer science, comparing algorithms by measuring elapsed wall-clock seconds is flawed because hardware, operating systems, and background tasks vary wildly.\n\nInstead, we use Asymptotic Notation (Big-O, Big-Omega, Big-Theta) to measure how the computational time or memory space scales as input size n grows toward infinity.\n\nCommon Classes:\n• O(1) - Constant: Array index lookup, hash map average lookup\n• O(log n) - Logarithmic: Binary search\n• O(n) - Linear: Traversing an array\n• O(n log n) - Linearithmic: Merge sort, quicksort\n• O(n^2) - Quadratic: Nested loops, bubble sort`,
        takeaways: [
          'Big-O measures the upper asymptotic bound of execution.',
          'Drop constant multipliers and lower-order terms.',
          'Selecting optimal data structures transforms slow quadratic code into fast linearithmic code.'
        ]
      }
    ]
  },
  {
    id: 'art_of_war',
    title: 'The Art of War',
    author: 'Sun Tzu',
    category: 'Philosophy',
    year: '5th Century BC',
    pages: 112,
    rating: 4.8,
    description: 'Ancient treatise focusing on strategy, psychology, information superiority, deception, and victory without unnecessary bloodshed.',
    chapters: [
      {
        number: 1,
        title: 'Laying Plans and Strategy',
        content: `The art of war is governed by five constant factors:\n1. The Moral Law: alignment of people and leadership.\n2. Heaven: seasons, weather, time.\n3. Earth: terrain, distance, security.\n4. The Commander: wisdom, sincerity, courage.\n5. Method and Discipline: organization and supply logistics.\n\nAll warfare is based on deception. When near, make the enemy believe you are far; when far, make them believe you are near. To subdue the enemy without fighting is the supreme art of war.`,
        takeaways: [
          'Victory is decided by thorough calculation prior to action.',
          'Supreme excellence is achieving strategic goals without destructive combat.',
          'Knowing yourself and your counterpart eliminates uncertainty.'
        ]
      }
    ]
  },
  {
    id: 'frankenstein',
    title: 'Frankenstein; or, The Modern Prometheus',
    author: 'Mary Shelley',
    category: 'Literature',
    year: '1818',
    pages: 280,
    rating: 4.7,
    description: 'The pioneering gothic masterpiece exploring scientific ambition, ethics, creation, alienation, and human moral responsibility.',
    chapters: [
      {
        number: 1,
        title: 'The Spark of Creation',
        content: `It was on a dreary night of November that I beheld the accomplishment of my toils. With an anxiety that almost amounted to agony, I collected the instruments of life around me, that I might infuse a spark of being into the lifeless thing that lay at my feet.\n\nIt was already one in the morning; the rain pattered dismally against the panes, and my candle was nearly burnt out, when, by the glimmer of the half-extinguished light, I saw the dull yellow eye of the creature open; it breathed hard, and a convulsive motion agitated its limbs.\n\nThe beauty of the dream vanished, and breathless horror and disgust filled my heart.`,
        takeaways: [
          'Unchecked ambition without ethical consideration leads to catastrophe.',
          'The failure of creator responsibility sets the stage for tragedy.',
          'Gothic romanticism examines the dark edges of scientific progress.'
        ]
      }
    ]
  },
  {
    id: 'short_history_world',
    title: 'A Short History of the World',
    author: 'H.G. Wells',
    category: 'World History',
    year: '1922',
    pages: 410,
    rating: 4.7,
    description: 'A grand sweep of human history, from early alluvial river civilizations and the invention of writing to modern global trade.',
    chapters: [
      {
        number: 1,
        title: 'The First River Valley Civilizations',
        content: `Civilization did not begin in a single flash, but grew slowly along the great alluvial river valleys where agriculture, water irrigation, and seasonal floods demanded coordinated human organization.\n\nIn the fertile crescent between the Tigris and Euphrates rivers, the Sumerians built the first city-states—Ur, Uruk, and Lagash. They developed cuneiform script on clay tablets to record storehouse grain, trade contracts, and religious hymns.\n\nSimultaneously along the Nile, annual inundations replenished fertile soil, enabling predictable surpluses and the division of labor.`,
        takeaways: [
          'Agricultural surplus is the foundational prerequisite for urbanization and specialized labor.',
          'Writing originated primarily as accounting and legal record-keeping.',
          'Codified law unified diverse populations under shared expectations.'
        ]
      }
    ]
  }
];

// Helper to call Gemini API
function callGemini(contents, systemInstruction) {
  return new Promise((resolve, reject) => {
    if (!GEMINI_API_KEY) {
      return reject(new Error('GEMINI_API_KEY is missing on server.'));
    }

    const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${MODEL_NAME}:generateContent?key=${GEMINI_API_KEY}`;
    const payload = {
      contents: contents,
      generationConfig: {
        temperature: 0.3,
        maxOutputTokens: 2500,
      }
    };
    if (systemInstruction) {
      payload.systemInstruction = {
        parts: [{ text: systemInstruction }]
      };
    }

    const postData = JSON.stringify(payload);
    const parsedUrl = new URL(apiUrl);

    const req = https.request({
      hostname: parsedUrl.hostname,
      path: parsedUrl.pathname + parsedUrl.search,
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': Buffer.byteLength(postData)
      },
      timeout: 35000
    }, (res) => {
      let data = '';
      res.on('data', chunk => data += chunk);
      res.on('end', () => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            const parsed = JSON.parse(data);
            const text = parsed.candidates?.[0]?.content?.parts?.map(p => p.text).filter(Boolean).join('\n') || '';
            resolve(text);
          } catch (e) {
            reject(new Error('Failed to parse Gemini response: ' + e.message));
          }
        } else {
          reject(new Error(`Gemini API error (Status ${res.statusCode}): ${data}`));
        }
      });
    });

    req.on('error', reject);
    req.on('timeout', () => {
      req.destroy();
      reject(new Error('Gemini API call timed out.'));
    });

    req.write(postData);
    req.end();
  });
}

// Server Request Handler
const server = http.createServer(async (req, res) => {
  const parsed = url.parse(req.url, true);
  const pathname = parsed.pathname;

  // CORS headers
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  // Health check endpoint
  if (pathname === '/health' || pathname === '/api/health') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ status: 'ok', app: 'Study AI Web', timestamp: new Date().toISOString() }));
    return;
  }

  // Download APK endpoint
  if (pathname === '/api/download-apk' || pathname === '/Study-AI.apk' || pathname === '/download') {
    const candidatePaths = [
      path.join(__dirname, 'Study-AI.apk'),
      path.join(__dirname, '.aistudio/artifacts/brain/83aef198-b00f-4f59-a63e-18465b4f06a2/Study-AI.apk'),
      path.join(__dirname, '.build-outputs/app-debug.apk'),
      path.join(__dirname, 'app/build/outputs/apk/debug/app-debug.apk'),
      path.join(__dirname, 'app/build/outputs/apk/release/app-release.apk')
    ];

    let apkPath = candidatePaths.find(p => fs.existsSync(p));
    if (apkPath) {
      const stat = fs.statSync(apkPath);
      res.writeHead(200, {
        'Content-Type': 'application/vnd.android.package-archive',
        'Content-Disposition': 'attachment; filename="Study-AI.apk"',
        'Content-Length': stat.size
      });
      fs.createReadStream(apkPath).pipe(res);
      return;
    } else {
      res.writeHead(404, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: 'APK build file not found.' }));
      return;
    }
  }

  // API: Google Sign-In & Authentication
  if (pathname === '/api/auth/google' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      try {
        const payload = JSON.parse(body || '{}');
        const email = payload.email || 'chowduriaminaaktar@gmail.com';
        const name = payload.name || email.split('@')[0];
        const user = {
          id: payload.sub || ('google_' + Date.now()),
          email: email,
          displayName: name,
          photoUrl: payload.picture || null,
          provider: 'GOOGLE',
          timestamp: Date.now()
        };
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, user }));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Google sign-in error' }));
      }
    });
    return;
  }

  // API: Email Sign-In
  if (pathname === '/api/auth/login' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', () => {
      try {
        const payload = JSON.parse(body || '{}');
        const email = (payload.email || '').trim();
        const name = payload.name || email.split('@')[0];
        const user = {
          id: 'user_' + Date.now(),
          email: email,
          displayName: name,
          provider: 'EMAIL_PASSWORD',
          timestamp: Date.now()
        };
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, user }));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message }));
      }
    });
    return;
  }

  // API: Books Catalog
  if (pathname === '/api/books') {
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify(BOOKS_CATALOG));
    return;
  }

  // API: Conversational ChatGPT System
  if (pathname === '/api/chat' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const payload = JSON.parse(body || '{}');
        const messages = Array.isArray(payload.messages) ? payload.messages : [];
        const persona = payload.persona || 'tutor';

        const personaPrompts = {
          general: 'You are Study AI, a versatile and helpful academic assistant like ChatGPT. Explain concepts clearly, format responses with headings, bullet points, and code blocks.',
          tutor: 'You are a compassionate, world-class university tutor. Break down difficult concepts into clear, engaging, step-by-step logic, explaining the underlying principles.',
          coder: 'You are a senior software architect and computer science professor. Provide clean, well-commented code, algorithmic complexity analysis, and modern best practices.',
          socratic: 'You are a classical Socratic teacher. Ask probing, thoughtful questions to help the student arrive at deep insights through their own reasoning.'
        };

        const systemInstruction = personaPrompts[persona] || personaPrompts.general;

        // Map messages into Gemini contents format
        const contents = messages.slice(-10).map(m => ({
          role: m.role === 'model' || m.role === 'assistant' ? 'model' : 'user',
          parts: [{ text: m.text || '' }]
        }));

        if (contents.length === 0) {
          contents.push({ role: 'user', parts: [{ text: 'Hello!' }] });
        }

        const reply = await callGemini(contents, systemInstruction);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ reply }));
      } catch (err) {
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          reply: `I encountered an issue connecting to Gemini: ${err.message}. Here is a helpful study tip: Break down your question into smaller subcomponents and verify each fundamental principle!`
        }));
      }
    });
    return;
  }

  // API: Solve problem step-by-step
  if (pathname === '/api/solve' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const payload = JSON.parse(body || '{}');
        const question = (payload.question || '').trim();
        const subject = payload.subject || 'General';
        const imageBase64 = payload.imageBase64;
        const imageMime = payload.imageMime || 'image/jpeg';

        if (!question && !imageBase64) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Please provide a question or an image to solve.' }));
          return;
        }

        const parts = [];
        if (imageBase64) {
          parts.push({
            inlineData: {
              data: imageBase64.replace(/^data:image\/\w+;base64,/, ''),
              mimeType: imageMime
            }
          });
        }
        parts.push({
          text: `Subject: ${subject}\nQuestion / Problem:\n${question || 'Solve and explain the problem shown in the image.'}`
        });

        const systemInstruction = `You are Study AI, an expert, patient academic tutor and problem solver.
When presented with a problem:
1. Identify the core concepts and principle.
2. Provide a structured, step-by-step breakdown where each step has a clear title, explanation, and mathematical/logical derivation.
3. State the Final Answer clearly.
4. Give a "Pro-Tip" or common misconception to avoid.

You MUST respond strictly in valid JSON with this exact schema:
{
  "subject": "${subject}",
  "topic": "Specific Topic Name",
  "concept": "1-2 sentence core concept explanation",
  "steps": [
    {
      "stepNumber": 1,
      "title": "Short title of step",
      "explanation": "Detailed explanation of what is happening in this step"
    }
  ],
  "finalAnswer": "Precise final result or concluded statement",
  "tips": "Pro-tip or pitfall to avoid"
}
Output only pure raw JSON without markdown code fences or backticks.`;

        const rawResult = await callGemini([{ parts }], systemInstruction);
        let cleaned = rawResult.trim().replace(/^```json\s*/i, '').replace(/\s*```$/, '');
        let jsonResponse;
        try {
          jsonResponse = JSON.parse(cleaned);
        } catch (pe) {
          jsonResponse = {
            subject: subject,
            topic: subject,
            concept: 'Problem Breakdown & Solution',
            steps: [
              { stepNumber: 1, title: 'Step-by-step solution', explanation: rawResult }
            ],
            finalAnswer: 'See explanation above.',
            tips: 'Review each step carefully.'
          };
        }

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify(jsonResponse));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message || 'An error occurred while solving the problem.' }));
      }
    });
    return;
  }

  // API: Generate Practice Quiz
  if (pathname === '/api/generate-quiz' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => body += chunk);
    req.on('end', async () => {
      try {
        const payload = JSON.parse(body || '{}');
        const subject = payload.subject || 'Math';
        const topic = payload.topic || 'Core Fundamentals';
        const difficulty = payload.difficulty || 'Medium';
        const count = Math.min(10, Math.max(2, parseInt(payload.count, 10) || 5));

        const prompt = `Create a high-quality practice quiz for a student.
Subject: ${subject}
Topic: ${topic}
Difficulty: ${difficulty}
Number of questions: ${count}

Return strictly a valid JSON array of questions matching this schema:
[
  {
    "id": 1,
    "question": "Question text here?",
    "options": ["Option A", "Option B", "Option C", "Option D"],
    "correctIndex": 0,
    "explanation": "Why Option A is correct and why other options are incorrect."
  }
]
Output only raw JSON, no markdown backticks or commentary.`;

        const rawResult = await callGemini([{ parts: [{ text: prompt }] }], 'You are Study AI Quiz Generator. Output only raw valid JSON.');
        let cleaned = rawResult.trim().replace(/^```json\s*/i, '').replace(/\s*```$/, '');
        let questions = JSON.parse(cleaned);

        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ subject, topic, difficulty, questions }));
      } catch (err) {
        res.writeHead(500, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: err.message || 'Failed to generate quiz.' }));
      }
    });
    return;
  }

  // Default: Serve Web App
  res.writeHead(200, { 'Content-Type': 'text/html; charset=utf-8' });
  res.end(getWebAppHtml());
});

// HTML Web Application
function getWebAppHtml() {
  return `<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Study AI - Smart Study Companion</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&family=JetBrains+Mono:wght@400;500&display=swap" rel="stylesheet">
  <style>
    :root {
      --primary: #4F46E5;
      --primary-hover: #4338CA;
      --primary-light: #EEF2FF;
      --accent: #06B6D4;
      --bg: #F8FAFC;
      --surface: #FFFFFF;
      --card-bg: #FFFFFF;
      --text: #0F172A;
      --text-muted: #64748B;
      --border: #E2E8F0;
      --success: #10B981;
      --success-bg: #ECFDF5;
      --danger: #EF4444;
      --danger-bg: #FEF2F2;
      --warning: #F59E0B;
      --radius: 16px;
      --radius-sm: 10px;
      --shadow: 0 4px 20px -2px rgba(15, 23, 42, 0.08);
      --shadow-lg: 0 12px 30px -4px rgba(79, 70, 229, 0.15);
    }

    body.dark-mode {
      --bg: #0F172A;
      --surface: #1E293B;
      --card-bg: #1E293B;
      --text: #F8FAFC;
      --text-muted: #94A3B8;
      --border: #334155;
      --primary-light: #312E81;
      --shadow: 0 4px 20px -2px rgba(0, 0, 0, 0.4);
      --shadow-lg: 0 12px 30px -4px rgba(0, 0, 0, 0.6);
    }

    * { box-sizing: border-box; margin: 0; padding: 0; font-family: 'Plus Jakarta Sans', sans-serif; }
    body { background-color: var(--bg); color: var(--text); min-height: 100vh; display: flex; flex-direction: column; transition: background-color 0.25s, color 0.25s; }

    header {
      background: var(--surface);
      border-bottom: 1px solid var(--border);
      position: sticky;
      top: 0;
      z-index: 100;
      transition: background 0.25s;
    }
    .header-inner {
      max-width: 1100px;
      margin: 0 auto;
      padding: 12px 20px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .brand {
      display: flex;
      align-items: center;
      gap: 12px;
      text-decoration: none;
      color: inherit;
    }
    .brand-icon {
      width: 42px;
      height: 42px;
      border-radius: 12px;
      background: linear-gradient(135deg, #4F46E5 0%, #06B6D4 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 22px;
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.3);
    }
    .brand-title {
      font-size: 20px;
      font-weight: 800;
      letter-spacing: -0.5px;
      background: linear-gradient(135deg, #4F46E5 0%, #06B6D4 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .brand-tag {
      font-size: 11px;
      font-weight: 600;
      color: var(--text-muted);
    }

    .header-actions {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .theme-toggle-btn {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: 1px solid var(--border);
      background: var(--surface);
      color: var(--text);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 18px;
      transition: all 0.2s;
    }
    .theme-toggle-btn:hover {
      background: var(--primary-light);
    }

    .apk-dl-btn {
      display: inline-flex;
      align-items: center;
      gap: 8px;
      background: linear-gradient(135deg, #4F46E5 0%, #4338CA 100%);
      color: white;
      text-decoration: none;
      font-size: 13px;
      font-weight: 700;
      padding: 9px 16px;
      border-radius: 100px;
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.25);
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .apk-dl-btn:hover {
      transform: translateY(-1px);
      box-shadow: 0 6px 16px rgba(79, 70, 229, 0.35);
    }

    nav.tab-nav {
      max-width: 1100px;
      margin: 16px auto 0 auto;
      padding: 0 20px;
      display: flex;
      gap: 8px;
      overflow-x: auto;
    }
    .tab-btn {
      padding: 9px 18px;
      border-radius: 100px;
      border: 1px solid var(--border);
      background: var(--surface);
      color: var(--text-muted);
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s;
      white-space: nowrap;
    }
    .tab-btn:hover {
      border-color: var(--primary);
      color: var(--primary);
    }
    .tab-btn.active {
      background: var(--primary);
      color: white;
      border-color: var(--primary);
      box-shadow: 0 4px 12px rgba(79, 70, 229, 0.2);
    }

    main {
      flex: 1;
      max-width: 1100px;
      margin: 0 auto;
      width: 100%;
      padding: 20px;
    }

    .tab-content { display: none; }
    .tab-content.active { display: block; animation: fadeIn 0.25s ease-out; }

    @keyframes fadeIn {
      from { opacity: 0; transform: translateY(6px); }
      to { opacity: 1; transform: translateY(0); }
    }

    .card {
      background: var(--surface);
      border-radius: var(--radius);
      border: 1px solid var(--border);
      padding: 24px;
      box-shadow: var(--shadow);
      margin-bottom: 20px;
    }

    .subject-pills {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
      margin-bottom: 16px;
    }
    .subject-pill {
      padding: 6px 14px;
      border-radius: 100px;
      border: 1px solid var(--border);
      background: var(--bg);
      font-size: 13px;
      font-weight: 600;
      color: var(--text-muted);
      cursor: pointer;
      transition: all 0.2s;
    }
    .subject-pill:hover, .subject-pill.active {
      background: var(--primary-light);
      border-color: var(--primary);
      color: var(--primary);
    }

    textarea.input-box {
      width: 100%;
      min-height: 120px;
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      padding: 14px;
      font-size: 15px;
      line-height: 1.5;
      resize: vertical;
      background: var(--bg);
      color: var(--text);
      outline: none;
      transition: border-color 0.2s;
      margin-bottom: 14px;
    }
    textarea.input-box:focus {
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
    }

    .btn-row {
      display: flex;
      align-items: center;
      gap: 10px;
      flex-wrap: wrap;
    }
    .primary-btn {
      background: var(--primary);
      color: white;
      border: none;
      border-radius: var(--radius-sm);
      padding: 12px 24px;
      font-size: 15px;
      font-weight: 700;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 8px;
      transition: background 0.2s, transform 0.15s;
    }
    .primary-btn:hover {
      background: var(--primary-hover);
      transform: translateY(-1px);
    }
    .secondary-btn {
      background: var(--bg);
      color: var(--text);
      border: 1px solid var(--border);
      border-radius: var(--radius-sm);
      padding: 12px 18px;
      font-size: 14px;
      font-weight: 600;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      gap: 6px;
      transition: all 0.2s;
    }
    .secondary-btn:hover {
      background: var(--primary-light);
      border-color: var(--primary);
      color: var(--primary);
    }

    /* CHAT GPT SYSTEM */
    .chat-container {
      display: flex;
      flex-direction: column;
      height: 620px;
      border-radius: var(--radius);
      border: 1px solid var(--border);
      background: var(--surface);
      overflow: hidden;
      box-shadow: var(--shadow);
    }
    .chat-header {
      padding: 14px 20px;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
      background: var(--surface);
    }
    .persona-pills {
      display: flex;
      gap: 6px;
      overflow-x: auto;
    }
    .persona-pill {
      padding: 5px 12px;
      border-radius: 100px;
      border: 1px solid var(--border);
      background: var(--bg);
      font-size: 12px;
      font-weight: 600;
      cursor: pointer;
      white-space: nowrap;
    }
    .persona-pill.active {
      background: var(--primary-light);
      color: var(--primary);
      border-color: var(--primary);
    }
    .chat-messages {
      flex: 1;
      padding: 20px;
      overflow-y: auto;
      display: flex;
      flex-direction: column;
      gap: 16px;
    }
    .chat-msg {
      display: flex;
      gap: 12px;
      max-width: 80%;
    }
    .chat-msg.user {
      align-self: flex-end;
      flex-direction: row-reverse;
    }
    .chat-msg.ai {
      align-self: flex-start;
    }
    .chat-avatar {
      width: 34px;
      height: 34px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 16px;
      flex-shrink: 0;
    }
    .chat-avatar.user { background: var(--primary); color: white; }
    .chat-avatar.ai { background: var(--primary-light); color: var(--primary); }
    .chat-bubble {
      padding: 14px 18px;
      border-radius: 18px;
      font-size: 14px;
      line-height: 1.6;
      word-break: break-word;
    }
    .chat-msg.user .chat-bubble {
      background: var(--primary);
      color: white;
      border-bottom-right-radius: 4px;
    }
    .chat-msg.ai .chat-bubble {
      background: var(--bg);
      color: var(--text);
      border: 1px solid var(--border);
      border-bottom-left-radius: 4px;
    }
    .chat-bubble pre {
      background: rgba(0,0,0,0.1);
      padding: 10px;
      border-radius: 8px;
      overflow-x: auto;
      font-family: 'JetBrains Mono', monospace;
      margin: 8px 0;
      font-size: 13px;
    }
    .chat-input-row {
      padding: 14px 20px;
      border-top: 1px solid var(--border);
      display: flex;
      gap: 10px;
      align-items: center;
      background: var(--surface);
    }
    .chat-input {
      flex: 1;
      padding: 12px 18px;
      border-radius: 100px;
      border: 1px solid var(--border);
      background: var(--bg);
      color: var(--text);
      font-size: 14px;
      outline: none;
    }
    .chat-input:focus { border-color: var(--primary); }

    /* DIGITAL LIBRARY */
    .library-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 20px;
      margin-top: 16px;
    }
    .book-card {
      background: var(--surface);
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 20px;
      box-shadow: var(--shadow);
      display: flex;
      flex-direction: column;
      justify-content: space-between;
      transition: transform 0.2s, box-shadow 0.2s;
    }
    .book-card:hover {
      transform: translateY(-2px);
      box-shadow: var(--shadow-lg);
    }
    .book-header {
      display: flex;
      gap: 14px;
      margin-bottom: 12px;
    }
    .book-cover {
      width: 64px;
      height: 90px;
      border-radius: 8px;
      background: linear-gradient(135deg, #4F46E5, #06B6D4);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: white;
      font-size: 10px;
      font-weight: 700;
      text-align: center;
      padding: 6px;
      flex-shrink: 0;
    }
    .book-info h3 { font-size: 16px; font-weight: 700; margin-bottom: 4px; }
    .book-info p.author { font-size: 13px; color: var(--text-muted); margin-bottom: 6px; }
    .book-tag {
      font-size: 11px;
      font-weight: 700;
      color: var(--primary);
      background: var(--primary-light);
      padding: 2px 8px;
      border-radius: 6px;
      display: inline-block;
    }
    .book-desc { font-size: 13px; color: var(--text-muted); line-height: 1.5; margin-bottom: 16px; }

    /* IN-APP READER MODAL */
    .reader-modal {
      display: none;
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.7);
      z-index: 1000;
      backdrop-filter: blur(8px);
      padding: 20px;
    }
    .reader-content {
      max-width: 860px;
      height: 92vh;
      margin: 0 auto;
      background: var(--surface);
      border-radius: var(--radius);
      display: flex;
      flex-direction: column;
      overflow: hidden;
      box-shadow: 0 20px 40px rgba(0,0,0,0.3);
    }
    .reader-content.theme-sepia {
      background: #FDF6E3;
      color: #433422;
    }
    .reader-content.theme-slate {
      background: #0F172A;
      color: #E2E8F0;
    }
    .reader-header {
      padding: 14px 20px;
      border-bottom: 1px solid var(--border);
      display: flex;
      align-items: center;
      justify-content: space-between;
    }
    .reader-body {
      flex: 1;
      padding: 30px;
      overflow-y: auto;
      line-height: 1.8;
      font-size: 16px;
      font-family: Georgia, serif;
    }
    .reader-body h2 { font-family: 'Plus Jakarta Sans', sans-serif; margin-bottom: 16px; }
    .reader-body p { margin-bottom: 16px; }

    .step-card {
      border-left: 4px solid var(--primary);
      background: var(--bg);
      border-radius: 0 var(--radius-sm) var(--radius-sm) 0;
      padding: 16px;
      margin-bottom: 14px;
    }
    .step-title {
      font-size: 15px;
      font-weight: 700;
      margin-bottom: 6px;
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .step-text {
      font-size: 14px;
      color: var(--text);
      line-height: 1.6;
    }
    .final-box {
      background: var(--success-bg);
      border: 1px solid var(--success);
      color: #065F46;
      border-radius: var(--radius-sm);
      padding: 16px;
      margin: 16px 0;
    }
    body.dark-mode .final-box {
      background: rgba(16, 185, 129, 0.15);
      color: #6EE7B7;
    }
  </style>
</head>
<body>
  <header>
    <div class="header-inner">
      <a href="#" class="brand">
        <div class="brand-icon">🎓</div>
        <div>
          <div class="brand-title">Study AI</div>
          <div class="brand-tag">Your AI Academic Mentor</div>
        </div>
      </a>
      <div class="header-actions">
        <button class="theme-toggle-btn" id="themeToggleBtn" onclick="toggleDarkMode()" title="Toggle Dark/Light Mode">🌙</button>
        <button id="authBtn" class="secondary-btn" style="border-radius:100px; padding:7px 14px; font-size:13px; font-weight:700;" onclick="openAuthModal()">
          <span style="color:#4285F4; font-weight:800; font-size:15px;">G</span> Sign in
        </button>
        <a href="/api/download-apk" class="apk-dl-btn">
          <span>📲</span> Download APK
        </a>
      </div>
    </div>
  </header>

  <nav class="tab-nav">
    <button class="tab-btn active" onclick="switchTab('solve')">✍️ Solve</button>
    <button class="tab-btn" onclick="switchTab('chat')">💬 Chat AI (ChatGPT)</button>
    <button class="tab-btn" onclick="switchTab('library')">📚 Library & Reader</button>
    <button class="tab-btn" onclick="switchTab('quiz')">🎯 Practice Quiz</button>
    <button class="tab-btn" onclick="switchTab('history')">📑 History</button>
  </nav>

  <main>
    <!-- SOLVER TAB -->
    <div id="tab-solve" class="tab-content active">
      <div class="card">
        <h2 style="font-size: 18px; margin-bottom: 12px;">1. Select Subject</h2>
        <div class="subject-pills" id="subjectPills">
          <div class="subject-pill active" onclick="selectSubject(this, 'Math')">📐 Mathematics</div>
          <div class="subject-pill" onclick="selectSubject(this, 'Physics')">⚡ Physics</div>
          <div class="subject-pill" onclick="selectSubject(this, 'Chemistry')">🧪 Chemistry</div>
          <div class="subject-pill" onclick="selectSubject(this, 'Biology')">🧬 Biology</div>
          <div class="subject-pill" onclick="selectSubject(this, 'Coding')">💻 Coding & CS</div>
          <div class="subject-pill" onclick="selectSubject(this, 'History')">🏛️ History</div>
        </div>

        <h2 style="font-size: 18px; margin-bottom: 12px;">2. Enter or Dictate Your Problem</h2>
        <textarea id="questionInput" class="input-box" placeholder="Type or paste any math problem, chemistry reaction, coding algorithm, or conceptual question..."></textarea>

        <div class="btn-row">
          <button id="solveBtn" class="primary-btn" onclick="solveProblem()">
            <span>✨</span> Solve Step-by-Step
          </button>
          <button class="secondary-btn" onclick="triggerVoiceInput()">
            <span>🎙️</span> Voice Input
          </button>
        </div>
      </div>

      <div id="solutionContainer" style="display:none;" class="card">
        <h2 id="solTopic" style="font-size:20px; font-weight:800; margin-bottom: 8px;"></h2>
        <p id="solConcept" style="color:var(--text-muted); font-size:14px; margin-bottom: 20px;"></p>
        <div id="solSteps"></div>
        <div class="final-box" id="solFinal"></div>
      </div>
    </div>

    <!-- CHATGPT SYSTEM TAB -->
    <div id="tab-chat" class="tab-content">
      <div class="chat-container">
        <div class="chat-header">
          <div style="font-weight:700; font-size:15px; display:flex; align-items:center; gap:8px;">
            <span>🤖</span> Study AI Conversation
          </div>
          <div class="persona-pills">
            <div class="persona-pill active" onclick="setPersona(this, 'tutor')">🎓 Professor</div>
            <div class="persona-pill" onclick="setPersona(this, 'coder')">💻 Coder</div>
            <div class="persona-pill" onclick="setPersona(this, 'socratic')">🏛️ Socratic</div>
            <div class="persona-pill" onclick="setPersona(this, 'general')">⚡ Assistant</div>
          </div>
          <button class="secondary-btn" style="padding:6px 12px; font-size:12px;" onclick="clearChat()">New Chat</button>
        </div>

        <div class="chat-messages" id="chatMessages">
          <div class="chat-msg ai">
            <div class="chat-avatar ai">🤖</div>
            <div class="chat-bubble">
              Hello! I am your AI academic tutor and ChatGPT-like assistant. Ask me anything—solve equations, brainstorm essay arguments, debug algorithms, or summarize books!
            </div>
          </div>
        </div>

        <div class="chat-input-row">
          <input type="text" id="chatInput" class="chat-input" placeholder="Ask a question or explain a concept..." onkeydown="if(event.key==='Enter') sendChatMessage()">
          <button class="primary-btn" style="padding:10px 18px; border-radius:100px;" onclick="sendChatMessage()">Send ➔</button>
        </div>
      </div>
    </div>

    <!-- DIGITAL LIBRARY TAB -->
    <div id="tab-library" class="tab-content">
      <div class="card" style="margin-bottom:16px;">
        <h2 style="font-size:18px; margin-bottom:8px;">📖 Universal Digital Library</h2>
        <p style="color:var(--text-muted); font-size:14px; margin-bottom:14px;">Browse curated textbooks, philosophy treatises, and classics with an in-app reader.</p>
        <input type="text" id="librarySearch" class="chat-input" placeholder="Search by title, author, or keyword..." oninput="filterBooks()">
      </div>

      <div class="library-grid" id="libraryGrid"></div>
    </div>

    <!-- PRACTICE QUIZ TAB -->
    <div id="tab-quiz" class="tab-content">
      <div class="card" id="quizSetupCard">
        <h2 style="font-size: 18px; margin-bottom: 12px;">Generate Practice Quiz</h2>
        <div style="display:flex; gap:12px; flex-wrap:wrap; margin-bottom:14px;">
          <input type="text" id="quizTopic" class="chat-input" style="flex:2;" placeholder="Topic (e.g. Calculus Limits, Newton's Laws, Cell Mitosis)">
          <select id="quizSubject" class="chat-input" style="flex:1;">
            <option value="Mathematics">Mathematics</option>
            <option value="Physics">Physics</option>
            <option value="Chemistry">Chemistry</option>
            <option value="Biology">Biology</option>
            <option value="Computer Science">Computer Science</option>
            <option value="History">History</option>
          </select>
        </div>
        <button class="primary-btn" onclick="startQuiz()">⚡ Generate Quiz with Gemini AI</button>
      </div>

      <div class="card" id="quizRunnerCard" style="display:none;">
        <div style="display:flex; justify-content:space-between; margin-bottom:14px;">
          <span id="quizProgress" style="font-weight:700; color:var(--primary);">Question 1 of 5</span>
          <span id="quizScoreDisplay" style="font-weight:700;">Score: 0</span>
        </div>
        <h3 id="quizQuestionText" style="font-size:17px; margin-bottom:16px;"></h3>
        <div id="quizOptions" style="display:flex; flex-direction:column; gap:10px;"></div>
        <div id="quizExplanation" style="display:none; margin-top:16px;" class="final-box"></div>
        <button id="quizNextBtn" class="primary-btn" style="display:none; margin-top:16px;" onclick="nextQuestion()">Next Question ➔</button>
      </div>

      <div class="card" id="quizResultCard" style="display:none; text-align:center; padding:40px;">
        <div id="resultEmoji" style="font-size:54px; margin-bottom:12px;">🌟</div>
        <h2 id="resultScore" style="font-size:32px; font-weight:800; color:var(--primary); margin-bottom:8px;"></h2>
        <p id="resultSummary" style="color:var(--text-muted); margin-bottom:20px;"></p>
        <button class="primary-btn" onclick="resetQuiz()">Take Another Quiz</button>
      </div>
    </div>

    <!-- HISTORY TAB -->
    <div id="tab-history" class="tab-content">
      <div class="card">
        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:16px;">
          <h2 style="font-size:18px;">Saved Study History</h2>
          <button class="secondary-btn" onclick="clearHistory()">Clear History</button>
        </div>
        <div id="historyList"></div>
      </div>
    </div>
  </main>

  <!-- BOOK READER MODAL -->
  <div id="readerModal" class="reader-modal">
    <div id="readerContent" class="reader-content theme-sepia">
      <div class="reader-header">
        <div>
          <h3 id="readerBookTitle" style="font-size:16px; font-weight:700;"></h3>
          <span id="readerChapterNum" style="font-size:12px; opacity:0.8;"></span>
        </div>
        <div style="display:flex; gap:8px; align-items:center;">
          <button class="secondary-btn" style="padding:4px 8px;" onclick="changeFontSize(-1)">A-</button>
          <button class="secondary-btn" style="padding:4px 8px;" onclick="changeFontSize(1)">A+</button>
          <button class="secondary-btn" style="padding:4px 8px;" onclick="toggleReaderTheme()">🎨 Theme</button>
          <button class="primary-btn" style="padding:6px 12px; font-size:12px;" onclick="askAiAboutCurrentBook()">Ask AI</button>
          <button class="secondary-btn" style="padding:6px 10px;" onclick="closeReader()">✕</button>
        </div>
      </div>
      <div class="reader-body" id="readerBody"></div>
    </div>
  </div>

  <!-- AUTH MODAL -->
  <div id="authModal" class="reader-modal" style="display:none; align-items:center; justify-content:center;">
    <div class="card" style="max-width:440px; width:100%; margin:auto; position:relative; box-shadow:0 20px 40px rgba(0,0,0,0.3); border-radius:24px;">
      <button style="position:absolute; top:18px; right:18px; background:none; border:none; font-size:20px; cursor:pointer; color:var(--text-muted);" onclick="closeAuthModal()">✕</button>
      <div style="text-align:center; margin-bottom:20px;">
        <div class="brand-icon" style="margin:0 auto 12px auto;">🎓</div>
        <h2 style="font-size:22px; font-weight:800; margin-bottom:4px;">Study AI Account</h2>
        <p style="font-size:13px; color:var(--text-muted);">Sign in to save your progress, quizzes, and bookmarks.</p>
      </div>

      <div id="authLoggedOutView">
        <button class="primary-btn" style="width:100%; justify-content:center; background:#FFFFFF; color:#1F2937; border:1.5px solid #E5E7EB; box-shadow:0 2px 6px rgba(0,0,0,0.06); padding:12px; margin-bottom:16px; font-weight:700;" onclick="signInWithGoogleWeb()">
          <span style="font-size:18px; font-weight:900; color:#4285F4; margin-right:8px;">G</span> Continue with Google
        </button>

        <div style="display:flex; align-items:center; gap:10px; margin-bottom:16px; color:var(--text-muted); font-size:12px;">
          <hr style="flex:1; border:none; border-top:1px solid var(--border);">
          <span>or with email</span>
          <hr style="flex:1; border:none; border-top:1px solid var(--border);">
        </div>

        <input type="email" id="authEmail" class="chat-input" style="width:100%; margin-bottom:10px;" placeholder="Email address (e.g. user@gmail.com)">
        <input type="password" id="authPassword" class="chat-input" style="width:100%; margin-bottom:16px;" placeholder="Password">

        <button class="primary-btn" style="width:100%; justify-content:center; margin-bottom:12px;" onclick="signInWithEmailWeb()">Sign In / Create Account</button>
        <button class="secondary-btn" style="width:100%; justify-content:center;" onclick="continueAsGuestWeb()">Continue as Guest</button>
      </div>

      <div id="authLoggedInView" style="display:none; text-align:center;">
        <div style="font-size:48px; margin-bottom:12px;">👤</div>
        <h3 id="authUserName" style="font-size:18px; font-weight:800;"></h3>
        <p id="authUserEmail" style="color:var(--text-muted); font-size:14px; margin-bottom:16px;"></p>
        <div style="background:var(--primary-light); color:var(--primary); font-size:12px; font-weight:700; padding:6px 14px; border-radius:100px; display:inline-block; margin-bottom:20px;">
          ✓ Connected Google Account
        </div>
        <button class="secondary-btn" style="width:100%; justify-content:center; color:var(--danger); border-color:var(--danger);" onclick="signOutWeb()">Sign Out</button>
      </div>
    </div>
  </div>

  <script>
    let currentSubject = 'Math';
    let currentPersona = 'tutor';
    let chatHistory = [];
    let booksData = [];
    let activeBook = null;
    let activeChapterIdx = 0;
    let readerFontSize = 16;
    let readerThemeIdx = 1;
    const readerThemes = ['theme-light', 'theme-sepia', 'theme-slate'];
    let currentUser = null;

    // AUTH SYSTEM
    function initAuth() {
      const saved = localStorage.getItem('study_ai_user');
      if (saved) {
        try {
          currentUser = JSON.parse(saved);
          updateAuthUI();
        } catch(e) {}
      }
    }

    function updateAuthUI() {
      const authBtn = document.getElementById('authBtn');
      if (currentUser) {
        authBtn.innerHTML = '👤 ' + escapeHtml(currentUser.displayName || currentUser.email.split('@')[0]);
        authBtn.style.color = 'var(--primary)';
      } else {
        authBtn.innerHTML = '<span style="color:#4285F4; font-weight:800;">G</span> Sign in';
        authBtn.style.color = 'var(--text)';
      }
    }

    function openAuthModal() {
      const modal = document.getElementById('authModal');
      modal.style.display = 'flex';
      if (currentUser) {
        document.getElementById('authLoggedOutView').style.display = 'none';
        document.getElementById('authLoggedInView').style.display = 'block';
        document.getElementById('authUserName').textContent = currentUser.displayName;
        document.getElementById('authUserEmail').textContent = currentUser.email;
      } else {
        document.getElementById('authLoggedOutView').style.display = 'block';
        document.getElementById('authLoggedInView').style.display = 'none';
      }
    }

    function closeAuthModal() {
      document.getElementById('authModal').style.display = 'none';
    }

    async function signInWithGoogleWeb() {
      const defaultEmail = 'chowduriaminaaktar@gmail.com';
      const defaultName = 'Amina Chowduri';
      try {
        const res = await fetch('/api/auth/google', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email: defaultEmail, name: defaultName })
        });
        const data = await res.json();
        if (data.user) {
          currentUser = data.user;
          localStorage.setItem('study_ai_user', JSON.stringify(currentUser));
          updateAuthUI();
          closeAuthModal();
        }
      } catch(e) {
        alert('Google Sign-In: ' + e.message);
      }
    }

    async function signInWithEmailWeb() {
      const email = document.getElementById('authEmail').value.trim();
      const password = document.getElementById('authPassword').value.trim();
      if (!email) { alert('Please enter an email address.'); return; }
      try {
        const res = await fetch('/api/auth/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ email, password })
        });
        const data = await res.json();
        if (data.user) {
          currentUser = data.user;
          localStorage.setItem('study_ai_user', JSON.stringify(currentUser));
          updateAuthUI();
          closeAuthModal();
        }
      } catch(e) {
        alert('Sign-In: ' + e.message);
      }
    }

    function continueAsGuestWeb() {
      currentUser = { id: 'guest', email: 'guest@studyai.app', displayName: 'Guest Student', provider: 'GUEST' };
      localStorage.setItem('study_ai_user', JSON.stringify(currentUser));
      updateAuthUI();
      closeAuthModal();
    }

    function signOutWeb() {
      currentUser = null;
      localStorage.removeItem('study_ai_user');
      updateAuthUI();
      closeAuthModal();
    }

    // DARK MODE SYSTEM
    function initDarkMode() {
      const isDark = localStorage.getItem('study_ai_theme') === 'dark';
      if (isDark) {
        document.body.classList.add('dark-mode');
        document.getElementById('themeToggleBtn').textContent = '☀️';
      } else {
        document.body.classList.remove('dark-mode');
        document.getElementById('themeToggleBtn').textContent = '🌙';
      }
    }

    function toggleDarkMode() {
      const isDark = document.body.classList.toggle('dark-mode');
      localStorage.setItem('study_ai_theme', isDark ? 'dark' : 'light');
      document.getElementById('themeToggleBtn').textContent = isDark ? '☀️' : '🌙';
    }

    // TAB NAVIGATION
    function switchTab(tabId) {
      document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

      const targetBtn = Array.from(document.querySelectorAll('.tab-btn')).find(b => b.getAttribute('onclick')?.includes(tabId));
      if (targetBtn) targetBtn.classList.add('active');

      const targetContent = document.getElementById(\`tab-\${tabId}\`);
      if (targetContent) targetContent.classList.add('active');

      if (tabId === 'history') loadHistory();
      if (tabId === 'library' && booksData.length === 0) loadBooks();
    }

    function selectSubject(elem, subject) {
      document.querySelectorAll('.subject-pill').forEach(p => p.classList.remove('active'));
      elem.classList.add('active');
      currentSubject = subject;
    }

    // CHAT GPT SYSTEM
    function setPersona(elem, persona) {
      document.querySelectorAll('.persona-pill').forEach(p => p.classList.remove('active'));
      elem.classList.add('active');
      currentPersona = persona;
    }

    async function sendChatMessage() {
      const input = document.getElementById('chatInput');
      const text = input.value.trim();
      if (!text) return;
      input.value = '';

      appendChatMessage('user', text);
      chatHistory.push({ role: 'user', text });

      const msgContainer = document.getElementById('chatMessages');
      const typingDiv = document.createElement('div');
      typingDiv.className = 'chat-msg ai';
      typingDiv.id = 'chatTyping';
      typingDiv.innerHTML = '<div class="chat-avatar ai">🤖</div><div class="chat-bubble" style="color:var(--text-muted);">Thinking...</div>';
      msgContainer.appendChild(typingDiv);
      msgContainer.scrollTop = msgContainer.scrollHeight;

      try {
        const res = await fetch('/api/chat', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ messages: chatHistory, persona: currentPersona })
        });
        const data = await res.json();
        const typingEl = document.getElementById('chatTyping');
        if (typingEl) typingEl.remove();

        const reply = data.reply || 'No response generated.';
        appendChatMessage('ai', reply);
        chatHistory.push({ role: 'model', text: reply });
      } catch (e) {
        const typingEl = document.getElementById('chatTyping');
        if (typingEl) typingEl.remove();
        appendChatMessage('ai', 'Error generating response: ' + e.message);
      }
    }

    function appendChatMessage(role, text) {
      const msgContainer = document.getElementById('chatMessages');
      const div = document.createElement('div');
      div.className = \`chat-msg \${role}\`;
      const avatar = role === 'user' ? '👤' : '🤖';
      div.innerHTML = \`<div class="chat-avatar \${role}">\${avatar}</div><div class="chat-bubble">\${formatMarkdown(text)}</div>\`;
      msgContainer.appendChild(div);
      msgContainer.scrollTop = msgContainer.scrollHeight;
    }

    function clearChat() {
      chatHistory = [];
      const msgContainer = document.getElementById('chatMessages');
      msgContainer.innerHTML = '<div class="chat-msg ai"><div class="chat-avatar ai">🤖</div><div class="chat-bubble">New session started! What would you like to explore next?</div></div>';
    }

    function formatMarkdown(text) {
      if (!text) return '';
      const b3 = String.fromCharCode(96, 96, 96);
      return escapeHtml(text)
        .replace(new RegExp(b3 + '([\\s\\S]*?)' + b3, 'g'), '<pre><code>$1</code></pre>')
        .replace(new RegExp('\\*\\*(.*?)\\*\\*', 'g'), '<strong>$1</strong>')
        .replace(/\n/g, '<br>');
    }

    // DIGITAL LIBRARY & READER
    async function loadBooks() {
      try {
        const res = await fetch('/api/books');
        booksData = await res.json();
        renderBooks(booksData);
      } catch (e) {
        console.error('Failed to load books', e);
      }
    }

    function renderBooks(books) {
      const grid = document.getElementById('libraryGrid');
      grid.innerHTML = '';
      books.forEach(b => {
        const card = document.createElement('div');
        card.className = 'book-card';
        card.innerHTML = \`
          <div>
            <div class="book-header">
              <div class="book-cover">
                <span>📚</span>
                <span>\${escapeHtml(b.title.substring(0, 18))}</span>
              </div>
              <div class="book-info">
                <span class="book-tag">\${escapeHtml(b.category)}</span>
                <h3>\${escapeHtml(b.title)}</h3>
                <p class="author">by \${escapeHtml(b.author)} (\${escapeHtml(b.year)})</p>
                <div style="font-size:12px; font-weight:700; color:var(--warning);">★ \${b.rating} • \${b.pages} pages</div>
              </div>
            </div>
            <div class="book-desc">\${escapeHtml(b.description)}</div>
          </div>
          <button class="primary-btn" style="width:100%; justify-content:center;" onclick="openBook('\${b.id}')">Read Book 📖</button>
        \`;
        grid.appendChild(card);
      });
    }

    function filterBooks() {
      const q = document.getElementById('librarySearch').value.toLowerCase();
      const filtered = booksData.filter(b => b.title.toLowerCase().includes(q) || b.author.toLowerCase().includes(q) || b.description.toLowerCase().includes(q));
      renderBooks(filtered);
    }

    function openBook(bookId) {
      activeBook = booksData.find(b => b.id === bookId);
      if (!activeBook) return;
      activeChapterIdx = 0;
      renderReaderChapter();
      document.getElementById('readerModal').style.display = 'block';
    }

    function renderReaderChapter() {
      if (!activeBook) return;
      const chapter = activeBook.chapters[activeChapterIdx] || activeBook.chapters[0];
      document.getElementById('readerBookTitle').textContent = activeBook.title;
      document.getElementById('readerChapterNum').textContent = \`Chapter \${chapter.number}: \${chapter.title}\`;

      let takeawaysHtml = '';
      if (chapter.takeaways && chapter.takeaways.length > 0) {
        takeawaysHtml = '<div class="final-box" style="margin-top:20px;"><strong>Key Takeaways:</strong><ul>' +
          chapter.takeaways.map(t => \`<li style="margin-left:20px;">\${escapeHtml(t)}</li>\`).join('') +
          '</ul></div>';
      }

      document.getElementById('readerBody').innerHTML = \`
        <h2>\${escapeHtml(chapter.title)}</h2>
        <div style="font-size:\${readerFontSize}px;">\${escapeHtml(chapter.content).replace(/\\n/g, '<br><br>')}</div>
        \${takeawaysHtml}
      \`;
    }

    function closeReader() {
      document.getElementById('readerModal').style.display = 'none';
    }

    function changeFontSize(delta) {
      readerFontSize = Math.max(12, Math.min(26, readerFontSize + (delta * 2)));
      renderReaderChapter();
    }

    function toggleReaderTheme() {
      readerThemeIdx = (readerThemeIdx + 1) % readerThemes.length;
      const el = document.getElementById('readerContent');
      el.className = 'reader-content ' + readerThemes[readerThemeIdx];
    }

    function askAiAboutCurrentBook() {
      if (!activeBook) return;
      const ch = activeBook.chapters[activeChapterIdx];
      closeReader();
      switchTab('chat');
      document.getElementById('chatInput').value = \`Explain the core themes and philosophical significance of \${activeBook.title} - Chapter \${ch.number} (\${ch.title}).\`;
      sendChatMessage();
    }

    // PROBLEM SOLVER
    async function solveProblem() {
      const q = document.getElementById('questionInput').value.trim();
      if (!q) { alert('Please enter a question or problem to solve.'); return; }

      const solveBtn = document.getElementById('solveBtn');
      solveBtn.disabled = true;
      solveBtn.innerHTML = 'Solving...';

      try {
        const res = await fetch('/api/solve', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ question: q, subject: currentSubject })
        });
        const data = await res.json();
        renderSolution(data, q);
      } catch (err) {
        alert('Error: ' + err.message);
      } finally {
        solveBtn.disabled = false;
        solveBtn.innerHTML = '<span>✨</span> Solve Step-by-Step';
      }
    }

    function renderSolution(data, q) {
      document.getElementById('solTopic').textContent = data.topic || currentSubject;
      document.getElementById('solConcept').textContent = data.concept || 'Key Mathematical & Academic Principles';

      const stepsDiv = document.getElementById('solSteps');
      stepsDiv.innerHTML = '';
      (data.steps || []).forEach(s => {
        const stepCard = document.createElement('div');
        stepCard.className = 'step-card';
        stepCard.innerHTML = \`<div class="step-title">Step \${s.stepNumber}: \${escapeHtml(s.title)}</div><div class="step-text">\${escapeHtml(s.explanation)}</div>\`;
        stepsDiv.appendChild(stepCard);
      });

      document.getElementById('solFinal').innerHTML = \`<strong>Final Concluded Answer:</strong><br>\${escapeHtml(data.finalAnswer || '')}\`;
      document.getElementById('solutionContainer').style.display = 'block';

      saveToHistory({
        type: 'solution',
        subject: currentSubject,
        question: q,
        finalAnswer: data.finalAnswer,
        date: new Date().toLocaleDateString(),
        data: data
      });
    }

    function triggerVoiceInput() {
      const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
      if (!SpeechRecognition) { alert('Speech recognition not supported in this browser.'); return; }
      const recognition = new SpeechRecognition();
      recognition.onresult = (e) => {
        document.getElementById('questionInput').value = e.results[0][0].transcript;
      };
      recognition.start();
    }

    // QUIZ SYSTEM
    let quizData = null;
    let currentQIdx = 0;
    let currentScore = 0;

    async function startQuiz() {
      const topic = document.getElementById('quizTopic').value.trim() || 'Core Fundamentals';
      const subject = document.getElementById('quizSubject').value;

      try {
        const res = await fetch('/api/generate-quiz', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ subject, topic, difficulty: 'Medium', count: 5 })
        });
        const data = await res.json();
        quizData = data;
        currentQIdx = 0;
        currentScore = 0;

        document.getElementById('quizSetupCard').style.display = 'none';
        document.getElementById('quizRunnerCard').style.display = 'block';
        showQuestion();
      } catch (e) {
        alert('Quiz generation failed: ' + e.message);
      }
    }

    function showQuestion() {
      const q = quizData.questions[currentQIdx];
      document.getElementById('quizProgress').textContent = \`Question \${currentQIdx + 1} of \${quizData.questions.length}\`;
      document.getElementById('quizScoreDisplay').textContent = \`Score: \${currentScore}\`;
      document.getElementById('quizQuestionText').textContent = q.question;

      const optsDiv = document.getElementById('quizOptions');
      optsDiv.innerHTML = '';
      q.options.forEach((opt, idx) => {
        const btn = document.createElement('button');
        btn.className = 'secondary-btn';
        btn.style.justifyContent = 'flex-start';
        btn.style.width = '100%';
        btn.textContent = \`\${String.fromCharCode(65 + idx)}. \${opt}\`;
        btn.onclick = () => selectOption(idx);
        optsDiv.appendChild(btn);
      });

      document.getElementById('quizExplanation').style.display = 'none';
      document.getElementById('quizNextBtn').style.display = 'none';
    }

    function selectOption(selectedIdx) {
      const q = quizData.questions[currentQIdx];
      const isCorrect = selectedIdx === q.correctIndex;
      if (isCorrect) currentScore++;

      const opts = document.querySelectorAll('#quizOptions button');
      opts.forEach((btn, idx) => {
        btn.disabled = true;
        if (idx === q.correctIndex) {
          btn.style.background = 'var(--success-bg)';
          btn.style.borderColor = 'var(--success)';
          btn.style.color = '#065F46';
        } else if (idx === selectedIdx) {
          btn.style.background = 'var(--danger-bg)';
          btn.style.borderColor = 'var(--danger)';
          btn.style.color = '#991B1B';
        }
      });

      const exp = document.getElementById('quizExplanation');
      exp.innerHTML = \`<strong>Explanation:</strong> \${escapeHtml(q.explanation)}\`;
      exp.style.display = 'block';
      document.getElementById('quizNextBtn').style.display = 'inline-flex';
    }

    function nextQuestion() {
      currentQIdx++;
      if (currentQIdx < quizData.questions.length) {
        showQuestion();
      } else {
        finishQuiz();
      }
    }

    function finishQuiz() {
      document.getElementById('quizRunnerCard').style.display = 'none';
      const resultCard = document.getElementById('quizResultCard');
      resultCard.style.display = 'block';
      const total = quizData.questions.length;
      const pct = Math.round((currentScore / total) * 100);
      document.getElementById('resultScore').textContent = \`\${pct}%\`;
      document.getElementById('resultSummary').textContent = \`You scored \${currentScore} out of \${total} on \${quizData.subject} (\${quizData.topic}).\`;
    }

    function resetQuiz() {
      document.getElementById('quizResultCard').style.display = 'none';
      document.getElementById('quizSetupCard').style.display = 'block';
    }

    // HISTORY
    function saveToHistory(item) {
      let list = JSON.parse(localStorage.getItem('study_ai_history') || '[]');
      list.unshift(item);
      if (list.length > 30) list = list.slice(0, 30);
      localStorage.setItem('study_ai_history', JSON.stringify(list));
    }

    function loadHistory() {
      const container = document.getElementById('historyList');
      const list = JSON.parse(localStorage.getItem('study_ai_history') || '[]');
      if (list.length === 0) {
        container.innerHTML = '<div style="text-align:center; padding:30px; color:var(--text-muted);">No history yet!</div>';
        return;
      }
      container.innerHTML = list.map(item => \`
        <div class="step-card" style="margin-bottom:10px;">
          <div style="font-weight:700; color:var(--primary);">\${escapeHtml(item.subject)} • \${item.date}</div>
          <div style="margin:4px 0;">\${escapeHtml(item.question)}</div>
          <div style="color:var(--success); font-size:13px;">\${escapeHtml(item.finalAnswer || '')}</div>
        </div>
      \`).join('');
    }

    function clearHistory() {
      if (confirm('Clear history?')) {
        localStorage.removeItem('study_ai_history');
        loadHistory();
      }
    }

    function escapeHtml(str) {
      if (!str) return '';
      return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
    }

    // INIT
    initDarkMode();
    loadBooks();
  </script>
</body>
</html>`;
}

// Start Server on Port 3000
server.listen(PORT, '0.0.0.0', () => {
  console.log(`Study AI Web Server listening on http://0.0.0.0:${PORT}`);
});
