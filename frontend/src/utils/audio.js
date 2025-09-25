let mediaStream = null
let mediaRecorder = null
let chunks = []

export async function startRecording() {
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    throw new Error('当前浏览器不支持录音')
  }
  mediaStream = await navigator.mediaDevices.getUserMedia({ audio: true })
  const mime = MediaRecorder.isTypeSupported('audio/webm;codecs=opus') ? 'audio/webm;codecs=opus' : 'audio/webm'
  mediaRecorder = new MediaRecorder(mediaStream, { mimeType: mime })
  chunks = []
  mediaRecorder.ondataavailable = (e) => { if (e.data && e.data.size > 0) chunks.push(e.data) }
  mediaRecorder.start()
}

export async function stopRecordingToBase64() {
  if (!mediaRecorder) return ''
  await new Promise((resolve) => {
    mediaRecorder.onstop = resolve
    mediaRecorder.stop()
  })
  const blob = new Blob(chunks, { type: mediaRecorder.mimeType || 'audio/webm' })
  if (mediaStream) {
    mediaStream.getTracks().forEach(t => t.stop())
  }
  mediaRecorder = null
  mediaStream = null
  chunks = []
  const arrayBuffer = await blob.arrayBuffer()
  const bytes = new Uint8Array(arrayBuffer)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  return btoa(binary)
}

export async function stopRecordingData() {
  if (!mediaRecorder) return { base64: '', url: '', mime: 'audio/webm' }
  await new Promise((resolve) => {
    mediaRecorder.onstop = resolve
    mediaRecorder.stop()
  })
  const mime = mediaRecorder.mimeType || 'audio/webm'
  const blob = new Blob(chunks, { type: mime })
  if (mediaStream) {
    mediaStream.getTracks().forEach(t => t.stop())
  }
  mediaRecorder = null
  mediaStream = null
  chunks = []
  const arrayBuffer = await blob.arrayBuffer()
  const bytes = new Uint8Array(arrayBuffer)
  let binary = ''
  for (let i = 0; i < bytes.byteLength; i++) {
    binary += String.fromCharCode(bytes[i])
  }
  const base64 = btoa(binary)
  const url = URL.createObjectURL(blob)
  return { base64, url, mime }
}


