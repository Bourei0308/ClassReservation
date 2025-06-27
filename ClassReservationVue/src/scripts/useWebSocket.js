import { ref } from 'vue'
import SockJS from 'sockjs-client/dist/sockjs.min.js'
import { Client } from '@stomp/stompjs'
import { useAuth } from './useAuth'

const stompClient = ref(null)
const isConnected = ref(false)

export function useWebSocket() {
    const { user } = useAuth()

    const connect = (onConnectedCallback) => {
        if (!user.value) {
            console.warn('No user, skip websocket connection')
            return
        }

        stompClient.value = new Client({
            webSocketFactory: () => new SockJS('/api/ws'),
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            onConnect: () => {
                console.log('WebSocket connected')
                isConnected.value = true

                if (onConnectedCallback) {
                    onConnectedCallback({
                        subscribe,
                        send,
                    })
                }
            },
            onStompError: (frame) => {
                console.error('Broker error', frame)
            }
        })

        stompClient.value.activate()
    }

    const disconnect = () => {
        if (stompClient.value) {
            stompClient.value.deactivate()
            isConnected.value = false
        }
    }

    const waitForConnection = () => {
        return new Promise((resolve) => {
            if (isConnected.value) {
                resolve()
            } else {
                const interval = setInterval(() => {
                    if (isConnected.value) {
                        clearInterval(interval)
                        resolve()
                    }
                }, 100)
            }
        })
    }

    const subscribe = async (destination, callback) => {
        await waitForConnection()
        return stompClient.value.subscribe(destination, callback)
    }

    const send = async (destination, body) => {
        await waitForConnection()
        stompClient.value.publish({ destination, body: JSON.stringify(body) })
    }

    return {
        connect,
        disconnect,
        subscribe,
        send,
        isConnected,
    }

    // 既存
subscribe(`/api/topic/unread/${newUser.id}`, () => {
  hasUnreadMessage.value = true
})

// 🔽 追加（通知が来たら赤丸 true にする）
subscribe(`/api/topic/notice/${newUser.id}`, () => {
  hasUnreadNotification.value = true
})

}
