import { Client } from '@stomp/stompjs';
import { act, renderHook } from '@testing-library/react';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { useSystemEvents } from '@features/node-monitoring/hooks/useSystemEvents';

vi.mock('@stomp/stompjs');
vi.mock('sockjs-client', () => ({ default: vi.fn() }));

type StompConnectHandler = () => void;
type StompDisconnectHandler = () => void;
type StompErrorHandler = () => void;
type StompMessageCallback = (msg: { body: string }) => void;

interface StompClientConfig {
  onConnect?: StompConnectHandler;
  onDisconnect?: StompDisconnectHandler;
  onStompError?: StompErrorHandler;
}

let capturedConfig: StompClientConfig = {};

const mockSubscribe = vi.fn();
const mockActivate = vi.fn();
const mockDeactivate = vi.fn().mockResolvedValue(undefined);

vi.mocked(Client).mockImplementation((config?) => {
  capturedConfig = (config ?? {}) as StompClientConfig;
  return {
    subscribe: mockSubscribe,
    activate: mockActivate,
    deactivate: mockDeactivate,
  } as unknown as Client;
});

describe('useSystemEvents', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    capturedConfig = {};
    vi.mocked(Client).mockImplementation((config?) => {
      capturedConfig = (config ?? {}) as StompClientConfig;
      return {
        subscribe: mockSubscribe,
        activate: mockActivate,
        deactivate: mockDeactivate,
      } as unknown as Client;
    });
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  it('initialState_givenMount_returnsEmptyEventsAndDisconnected', () => {
    const { result } = renderHook(() => useSystemEvents());
    expect(result.current.events).toEqual([]);
    expect(result.current.connected).toBe(false);
  });

  it('activate_givenMount_activatesStompClient', () => {
    renderHook(() => useSystemEvents());
    expect(mockActivate).toHaveBeenCalledOnce();
  });

  it('onConnect_givenConnectionEstablished_setsConnectedTrue', () => {
    const { result } = renderHook(() => useSystemEvents());
    act(() => {
      capturedConfig.onConnect?.();
    });
    expect(result.current.connected).toBe(true);
  });

  it('onConnect_givenConnectionEstablished_subscribesToEventsTopic', () => {
    renderHook(() => useSystemEvents());
    act(() => {
      capturedConfig.onConnect?.();
    });
    expect(mockSubscribe).toHaveBeenCalledWith('/topic/events', expect.any(Function));
  });

  it('onMessage_givenEventArrives_appendsToEvents', () => {
    const testEvent = {
      type: 'VOTE_RECEIVED',
      nodeId: 'server-1',
      transactionId: 'tx-1',
      message: 'YES',
    };
    mockSubscribe.mockImplementation((_topic: string, callback: StompMessageCallback) => {
      callback({ body: JSON.stringify(testEvent) });
    });
    const { result } = renderHook(() => useSystemEvents());
    act(() => {
      capturedConfig.onConnect?.();
    });
    expect(result.current.events).toContainEqual(testEvent);
  });

  it('onDisconnect_givenConnectionLost_setsConnectedFalse', () => {
    const { result } = renderHook(() => useSystemEvents());
    act(() => {
      capturedConfig.onConnect?.();
    });
    act(() => {
      capturedConfig.onDisconnect?.();
    });
    expect(result.current.connected).toBe(false);
  });

  it('onStompError_givenError_setsConnectedFalse', () => {
    const { result } = renderHook(() => useSystemEvents());
    act(() => {
      capturedConfig.onConnect?.();
    });
    act(() => {
      capturedConfig.onStompError?.();
    });
    expect(result.current.connected).toBe(false);
  });

  it('unmount_givenCleanup_deactivatesClient', () => {
    const { unmount } = renderHook(() => useSystemEvents());
    unmount();
    expect(mockDeactivate).toHaveBeenCalledOnce();
  });
});
