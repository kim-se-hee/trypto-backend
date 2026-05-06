import { useEffect, useRef } from "react";
import {
  connect,
  subscribeUserEvents,
  isConnected,
  type UserEvent,
} from "@/lib/api/websocket";

interface UseUserEventsOptions {
  userId: number | null;
  onOrderFilled?: (event: UserEvent) => void;
}

export function useUserEvents({ userId, onOrderFilled }: UseUserEventsOptions): void {
  const onOrderFilledRef = useRef(onOrderFilled);
  onOrderFilledRef.current = onOrderFilled;

  useEffect(() => {
    if (!userId) return;

    if (!isConnected()) {
      connect();
    }

    const unsubscribe = subscribeUserEvents(userId, (event) => {
      if (event.eventType === "ORDER_FILLED" && onOrderFilledRef.current) {
        onOrderFilledRef.current(event);
      }
    });

    return unsubscribe;
  }, [userId]);
}
