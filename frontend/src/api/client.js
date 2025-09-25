import http from './http'

export async function getCharacters() {
  const { data } = await http.get('/api/character/list')
  if (data?.code !== 200) throw new Error(data?.msg || 'Failed to fetch characters')
  return data.data || []
}

export async function chatText({ characterId, message }) {
  const { data } = await http.post('/api/chat/text', { characterId, message })
  if (data?.code !== 200) throw new Error(data?.msg || 'Chat text failed')
  return data.data // { reply }
}

export async function chatVoice({ characterId, audioBase64 }) {
  const { data } = await http.post('/api/chat/voice', { characterId, audioBase64 })
  if (data?.code !== 200) throw new Error(data?.msg || 'Chat voice failed')
  return data.data // { text, reply, audioBase64 }
}


