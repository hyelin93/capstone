interface RegisterPushTokenRequest {
  username: string
  token: string
}

export const notificationApi = {
  async registerPushToken({ username, token }: RegisterPushTokenRequest) {
    const response = await fetch(
      `${import.meta.env.VITE_API_BASE_URL}/notifications/token`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          username,
          token,
        }),
      },
    )

    if (!response.ok) {
      throw new Error('푸시 토큰 등록에 실패했습니다.')
    }

    return response.text()
  },
}
