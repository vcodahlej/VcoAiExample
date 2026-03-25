import { FormEvent, useEffect, useLayoutEffect, useMemo, useRef, useState } from 'react';

type MessageType = 'user' | 'assistant' | 'system' | 'error';

type ChatMessage = {
  type: MessageType;
  content: string;
  conversationId: string;
  timestamp: string;
};

type Session = {
  id: string;
  label: string;
  messages: ChatMessage[];
  createdAt: string;
  updatedAt: string;
};

const socketUrl =
  import.meta.env.VITE_CHAT_WS_URL ?? 'ws://localhost:8080/wss/api/v1/chat';
const storageKey = 'vco-chat-sessions';

const createSession = (): Session => {
  const timestamp = new Date().toISOString();

  return {
    id: crypto.randomUUID(),
    label: 'New Session',
    messages: [],
    createdAt: timestamp,
    updatedAt: timestamp,
  };
};

const deriveSessionLabel = (messages: ChatMessage[], fallbackLabel: string): string => {
  const firstUserMessage = messages.find((message) => message.type === 'user');
  if (!firstUserMessage) {
    return fallbackLabel;
  }

  const normalized = firstUserMessage.content.trim().replace(/\s+/g, ' ');
  return normalized.slice(0, 32) + (normalized.length > 32 ? '...' : '');
};

const formatSessionOptionLabel = (session: Session): string => {
  if (session.label !== 'New Session') {
    return session.label;
  }

  const createdAt = new Date(session.createdAt);
  return `New Session (${createdAt.toLocaleDateString()} ${createdAt.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })})`;
};

function App() {
  const [input, setInput] = useState('');
  const [connected, setConnected] = useState(false);
  const [sessions, setSessions] = useState<Session[]>(() => {
    if (typeof window === 'undefined') {
      return [createSession()];
    }

    const saved = window.localStorage.getItem(storageKey);
    if (!saved) {
      return [createSession()];
    }

    try {
      const parsed = JSON.parse(saved) as Session[];
      return parsed.length > 0 ? parsed : [createSession()];
    }
    catch {
      return [createSession()];
    }
  });
  const [activeSessionId, setActiveSessionId] = useState(() => {
    if (typeof window === 'undefined') {
      return '';
    }

    return window.localStorage.getItem(`${storageKey}:active`) ?? '';
  });
  const socketRef = useRef<WebSocket | null>(null);
  const initializedRef = useRef(false);
  const chatHistoryRef = useRef<HTMLElement | null>(null);
  const chatHistoryEndRef = useRef<HTMLDivElement | null>(null);

  const orderedSessions = useMemo(
    () =>
      [...sessions].sort(
        (left, right) => new Date(right.updatedAt).getTime() - new Date(left.updatedAt).getTime(),
      ),
    [sessions],
  );

  const activeSession =
    sessions.find((session) => session.id === activeSessionId) ?? orderedSessions[0] ?? null;

  useEffect(() => {
    const socket = new WebSocket(socketUrl);
    socketRef.current = socket;

    socket.addEventListener('open', () => setConnected(true));
    socket.addEventListener('close', () => setConnected(false));
    socket.addEventListener('message', (event) => {
      const message = JSON.parse(event.data) as ChatMessage;
      setSessions((current) => {
        const existingSession = current.find((session) => session.id === message.conversationId);
        if (existingSession) {
          return current.map((session) =>
            session.id === message.conversationId
              ? {
                  ...session,
                  messages: [...session.messages, message],
                  label: deriveSessionLabel([...session.messages, message], session.label),
                  updatedAt: message.timestamp,
                }
              : session,
          );
        }

        return [
          {
            id: message.conversationId,
            label: deriveSessionLabel([message], 'New Session'),
            messages: [message],
            createdAt: message.timestamp,
            updatedAt: message.timestamp,
          },
          ...current,
        ];
      });
      setActiveSessionId((current) => current || message.conversationId);
    });

    return () => {
      socket.close();
    };
  }, []);

  useEffect(() => {
    if (!initializedRef.current && sessions.length > 0 && !activeSessionId) {
      initializedRef.current = true;
      setActiveSessionId(sessions[0].id);
    }
  }, [activeSessionId, sessions]);

  useEffect(() => {
    window.localStorage.setItem(storageKey, JSON.stringify(sessions));
  }, [sessions]);

  useEffect(() => {
    if (activeSessionId) {
      window.localStorage.setItem(`${storageKey}:active`, activeSessionId);
    }
  }, [activeSessionId]);

  useLayoutEffect(() => {
    if (!chatHistoryRef.current || !chatHistoryEndRef.current) {
      return;
    }

    const animationFrame = window.requestAnimationFrame(() => {
      chatHistoryEndRef.current?.scrollIntoView({
        block: 'end',
        behavior: 'smooth',
      });
    });

    return () => window.cancelAnimationFrame(animationFrame);
  }, [activeSession?.id, activeSession?.messages.length]);

  const createNewSession = () => {
    const session = createSession();
    setSessions((current) => [session, ...current]);
    setActiveSessionId(session.id);
  };

  const sendMessage = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const trimmed = input.trim();
    if (
      !trimmed ||
      !activeSession ||
      !socketRef.current ||
      socketRef.current.readyState !== WebSocket.OPEN
    ) {
      return;
    }

    const userMessage: ChatMessage = {
      type: 'user',
      content: trimmed,
      conversationId: activeSession.id,
      timestamp: new Date().toISOString(),
    };

    setSessions((current) =>
      current.map((session) =>
        session.id === activeSession.id
          ? {
              ...session,
              messages: [...session.messages, userMessage],
              label: deriveSessionLabel([...session.messages, userMessage], session.label),
              updatedAt: userMessage.timestamp,
            }
          : session,
      ),
    );
    socketRef.current.send(
      JSON.stringify({
        type: 'user',
        content: trimmed,
        conversationId: activeSession.id,
      }),
    );
    setInput('');
  };

  return (
    <main className="app-shell">
      <section className="chat-card">
        <header className="chat-header">
          <div>
            <p className="eyebrow">Spring AI 1.0.0</p>
            <h1>WebSocket Chat</h1>
          </div>
          <div className="header-controls">
            <label className="session-picker">
              <span>Session</span>
              <select
                value={activeSession?.id ?? ''}
                onChange={(event) => setActiveSessionId(event.target.value)}
              >
                {orderedSessions.map((session) => (
                  <option key={session.id} value={session.id}>
                    {formatSessionOptionLabel(session)}
                  </option>
                ))}
              </select>
            </label>
            <button type="button" className="new-session-button" onClick={createNewSession}>
              New Session
            </button>
            <span className={connected ? 'status online' : 'status offline'}>
              {connected ? 'Connected' : 'Disconnected'}
            </span>
          </div>
        </header>

        <section className="chat-log">
          <section className="chat-history" ref={chatHistoryRef} aria-live="polite">
            {!activeSession || activeSession.messages.length === 0 ? (
              <div className="empty-state">
                Pick a previous session or start a new one and ask the backend a question.
              </div>
            ) : (
              <>
                {activeSession.messages.map((message, index) => (
                  <article key={`${message.timestamp}-${index}`} className={`bubble ${message.type}`}>
                    <span className="bubble-role">{message.type}</span>
                    <p>{message.content}</p>
                  </article>
                ))}
                <div ref={chatHistoryEndRef} className="chat-history-end" aria-hidden="true" />
              </>
            )}
          </section>
        </section>

        <form className="composer" onSubmit={sendMessage}>
          <textarea
            value={input}
            onChange={(event) => setInput(event.target.value)}
            placeholder="Ask something..."
            rows={3}
          />
          <button type="submit" disabled={!connected || !input.trim() || !activeSession}>
            Send
          </button>
        </form>
      </section>
    </main>
  );
}

export default App;
