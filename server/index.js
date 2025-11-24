const express = require('express');
const path = require('path');
const Database = require('better-sqlite3');
const bcrypt = require('bcryptjs');
const { v4: uuidv4 } = require('uuid');
const { randomBytes } = require('crypto');

const DB_PATH = path.join(__dirname, 'server-data.db');
const PORT = process.env.PORT || 4000;

const app = express();
app.use(express.json());

const db = new Database(DB_PATH);

db.pragma('foreign_keys = ON');
db.exec(`
CREATE TABLE IF NOT EXISTS user_sessions (
    session_id TEXT PRIMARY KEY,
    user_id BLOB NOT NULL,
    device_id TEXT,
    access_token TEXT NOT NULL UNIQUE,
    created_at INTEGER NOT NULL,
    last_seen INTEGER NOT NULL,
    is_revoked INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX IF NOT EXISTS idx_user_sessions_user ON user_sessions(user_id);
`);

const statements = {
    getUserByEmail: db.prepare('SELECT id, email, password_hash FROM users WHERE LOWER(email) = LOWER(?)'),
    insertUser: db.prepare(
        'INSERT INTO users (id, email, password_hash, display_name, created_at) VALUES (?, ?, ?, ?, ?)'
    ),
    revokeSessionsByUser: db.prepare('UPDATE user_sessions SET is_revoked = 1, last_seen = ? WHERE user_id = ? AND is_revoked = 0'),
    revokeSessionByToken: db.prepare('UPDATE user_sessions SET is_revoked = 1, last_seen = ? WHERE access_token = ? AND is_revoked = 0'),
    insertSession: db.prepare(`INSERT INTO user_sessions
        (session_id, user_id, device_id, access_token, created_at, last_seen, is_revoked)
        VALUES (?, ?, ?, ?, ?, ?, 0)`),
    getSessionByToken: db.prepare('SELECT session_id, user_id, device_id, access_token, created_at, last_seen, is_revoked FROM user_sessions WHERE access_token = ? AND is_revoked = 0'),
    touchSession: db.prepare('UPDATE user_sessions SET last_seen = ? WHERE access_token = ?')
};

function bufferToHex(buffer) {
    return Buffer.isBuffer(buffer) ? buffer.toString('hex') : buffer;
}

function authenticate(req, res, next) {
    const header = req.headers.authorization || '';
    const [type, token] = header.split(' ');
    if (type !== 'Bearer' || !token) {
        return res.status(401).json({ error: 'Missing or invalid Authorization header' });
    }

    const session = statements.getSessionByToken.get(token);
    if (!session) {
        return res.status(401).json({ error: 'Session not found or revoked' });
    }

    statements.touchSession.run(Date.now(), token);
    req.session = session;
    next();
}

app.get('/health', (_req, res) => {
    res.json({ status: 'ok', time: new Date().toISOString() });
});

app.post('/register', async (req, res) => {
    const { email, password, displayName } = req.body || {};
    if (!email || !password) {
        return res.status(400).json({ error: 'Email and password required' });
    }

    const normalizedEmail = String(email).trim().toLowerCase();
    const existing = statements.getUserByEmail.get(normalizedEmail);
    if (existing) {
        return res.status(409).json({ error: 'User already exists' });
    }

    const now = Date.now();
    let hashed;
    try {
        hashed = await bcrypt.hash(password, 12);
    } catch (err) {
        console.error('Failed to hash password', err);
        return res.status(500).json({ error: 'Failed to hash password' });
    }

    const userId = randomBytes(16);
    try {
        statements.insertUser.run(
            userId,
            normalizedEmail,
            hashed,
            displayName || normalizedEmail,
            now
        );
    } catch (err) {
        console.error('Failed to insert user', err);
        return res.status(500).json({ error: 'Failed to create user' });
    }

    res.status(201).json({
        id: bufferToHex(userId),
        email: normalizedEmail
    });
});

app.post('/login', async (req, res) => {
    const { email, password, deviceId } = req.body || {};
    if (!email || !password) {
        return res.status(400).json({ error: 'Email and password required' });
    }

    const normalizedEmail = String(email).trim().toLowerCase();
    const user = statements.getUserByEmail.get(normalizedEmail);
    if (!user) {
        return res.status(401).json({ error: 'Invalid credentials' });
    }

    const passwordMatch = await bcrypt.compare(password, user.password_hash);
    if (!passwordMatch) {
        return res.status(401).json({ error: 'Invalid credentials' });
    }

    const now = Date.now();
    const sessionId = uuidv4();
    const accessToken = randomBytes(32).toString('base64url');

    const transaction = db.transaction(() => {
        statements.revokeSessionsByUser.run(now, user.id);
        statements.insertSession.run(sessionId, user.id, deviceId || null, accessToken, now, now);
    });

    try {
        transaction();
    } catch (err) {
        console.error('Failed to create session', err);
        return res.status(500).json({ error: 'Failed to create session' });
    }

    res.json({
        sessionId,
        token: accessToken,
        user: {
            id: bufferToHex(user.id),
            email: user.email
        }
    });
});

app.post('/logout', authenticate, (req, res) => {
    const token = req.session.access_token;
    statements.revokeSessionByToken.run(Date.now(), token);
    res.json({ success: true });
});

app.get('/session', authenticate, (req, res) => {
    const session = req.session;
    res.json({
        sessionId: session.session_id,
        userId: bufferToHex(session.user_id),
        deviceId: session.device_id,
        createdAt: session.created_at,
        lastSeen: session.last_seen
    });
});

app.listen(PORT, () => {
    console.log(`Auth server listening on port ${PORT}`);
});
