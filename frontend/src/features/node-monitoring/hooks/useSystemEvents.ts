import type { SystemEvent } from '@common/types';
import type { IMessage } from '@stomp/stompjs';
import { Client } from '@stomp/stompjs';
import { useEffect, useRef, useState } from 'react';
import SockJS from 'sockjs-client';

const WS_URL = '/ws';
const EVENTS_TOPIC = '/topic/events';
const MAX_BUFFERED_EVENTS = 200;

/**
 * Connects to the coordinator WebSocket and subscribes to the events topic.
 *
 * <p>Uses a functional state update so that all events are appended even
 * when the browser delivers multiple WebSocket frames in a single JS task
 * (React 18 automatic batching would otherwise drop intermediate values
 * with a plain value-based setState call).
 *
 * @returns the accumulated event buffer and a flag indicating connection health.
 */
export function useSystemEvents(): {
  events: SystemEvent[];
  connected: boolean;
} {
  const [events, setEvents] = useState<SystemEvent[]>([]);
  const [connected, setConnected] = useState(false);
  const clientRef = useRef<Client | null>(null);

  useEffect(() => {
    function handleMessage(message: IMessage) {
      const event: SystemEvent = JSON.parse(message.body);
      setEvents((prev) => [...prev.slice(-(MAX_BUFFERED_EVENTS - 1)), event]);
    }

    const client = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      onConnect: () => {
        setConnected(true);
        client.subscribe(EVENTS_TOPIC, handleMessage);
      },
      onDisconnect: () => setConnected(false),
      onStompError: () => setConnected(false),
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  return { events, connected };
}
