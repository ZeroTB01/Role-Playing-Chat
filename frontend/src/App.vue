<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElDrawer, ElAvatar, ElButton, ElInput, ElScrollbar, ElMessage } from 'element-plus'
import 'element-plus/dist/index.css'
import { getCharacters, chatText, chatVoice } from '@/api/client'
import { startRecording, stopRecordingToBase64, stopRecordingData } from '@/utils/audio'

const drawer = ref(false)
const roles = ref([])
const selectedRole = ref(null)
const messages = ref([]) // { from:'user'|'ai', content:string, time:number }
const input = ref('')
const chatRef = ref(null)

// 录音状态
const recording = ref(false)
const recordSeconds = ref(0)
const recordTimer = ref(null)
const isCancel = ref(false)
let pressStartY = 0
const recordStartTs = ref(0)
const MAX_REC_SEC = 60
const MIN_REC_SEC = 0.5
const previewUrl = ref('')
const previewVisible = ref(false)

async function loadRoles(){
  try{
    roles.value = await getCharacters()
    if(roles.value.length>0 && !selectedRole.value){
      selectedRole.value = roles.value[0]
    }
  }catch(e){ ElMessage.error(e.message) }
}

function scrollToBottom(){
  nextTick(()=>{
    if(chatRef.value){
      const el = chatRef.value.$el || chatRef.value
      el.scrollTop = el.scrollHeight
    }
  })
}

async function sendText(){
  if(!input.value || !selectedRole.value){ return }
  const mine = { from:'user', content: input.value, time: Date.now() }
  messages.value.push(mine)
  const text = input.value
  input.value = ''
  scrollToBottom()
  try{
    const res = await chatText({ characterId: selectedRole.value.id, message: text })
    messages.value.push({ from:'ai', content: res.reply || '', time: Date.now() })
  }catch(e){ ElMessage.error(e.message) }
  scrollToBottom()
}

// 语音长按占位：后续接入录音
async function onVoicePress(e){
  try{
    await startRecording()
    ElMessage.info('开始录音')
    recording.value = true
    recordSeconds.value = 0
    isCancel.value = false
    recordStartTs.value = Date.now()
    pressStartY = (e?.touches?.[0]?.clientY) || (e?.clientY) || 0
    if(recordTimer.value){ clearInterval(recordTimer.value) }
    recordTimer.value = setInterval(()=>{ 
      recordSeconds.value++ 
      if(recordSeconds.value >= MAX_REC_SEC){
        onVoiceReleaseOnce()
      }
    }, 1000)
    // 监听移动以判断取消手势
    window.addEventListener('mousemove', onMoveCheckCancel)
    window.addEventListener('touchmove', onMoveCheckCancel, { passive:false })
    // 全局监听松开
    window.addEventListener('mouseup', onVoiceReleaseOnce, { once:true })
    window.addEventListener('touchend', onVoiceReleaseOnce, { once:true })
  }catch(e){ ElMessage.error(e.message) }
}
function onVoiceReleaseOnce(){ onVoiceRelease() }
function onMoveCheckCancel(ev){
  const y = (ev?.touches?.[0]?.clientY) || (ev?.clientY) || 0
  if(!pressStartY) return
  const dy = pressStartY - y
  isCancel.value = dy > 60 // 上滑超过阈值即取消
}
async function onVoiceRelease(){
  try{
    if(recordTimer.value){ clearInterval(recordTimer.value); recordTimer.value=null }
    window.removeEventListener('mousemove', onMoveCheckCancel)
    window.removeEventListener('touchmove', onMoveCheckCancel)
    let cancelNow = isCancel.value
    recording.value = false
    const elapsedSec = recordStartTs.value ? (Date.now() - recordStartTs.value)/1000 : 0
    recordStartTs.value = 0
    const { base64: b64, url, mime } = await stopRecordingData()
    if(elapsedSec < MIN_REC_SEC){
      ElMessage.info('录音太短，已取消')
      return
    }
    if(cancelNow){
      ElMessage.info('已取消发送')
      return
    }
    if(!b64){ return }
    previewUrl.value = url
    previewVisible.value = true
    messages.value.push({ from:'user', content: '[语音]', time: Date.now() })
    scrollToBottom()
    const res = await chatVoice({ characterId: selectedRole.value?.id, audioBase64: b64 })
    messages.value.push({ from:'ai', content: res.reply || '(无回复)', time: Date.now() })
    scrollToBottom()
    if(res.audioBase64){
      const audio = new Audio('data:audio/wav;base64,' + res.audioBase64)
      audio.play().catch(()=>{})
    }
  }catch(e){ ElMessage.error(e.message) }
}

onMounted(loadRoles)
</script>

<template>
  <div class="page">
    <header class="topbar">
      <ElButton link @click="drawer = true">选择角色</ElButton>
      <div class="role-name">{{ selectedRole?.name || '未选择角色' }}</div>
    </header>

    <ElDrawer v-model="drawer" title="角色列表" size="300px" :with-header="true">
      <div class="role-list">
        <div v-for="r in roles" :key="r.id" class="role-item" @click="selectedRole = r; drawer=false">
          <ElAvatar :size="48" :src="r.avatarUrl" />
          <div class="meta">
            <div class="name">{{ r.name }}</div>
            <div class="desc">{{ r.description }}</div>
          </div>
        </div>
      </div>
    </ElDrawer>

    <main class="chat">
      <ElScrollbar class="chat-scroll" ref="chatRef">
        <div class="messages">
          <div v-for="(m,idx) in messages" :key="idx" class="msg" :class="m.from">
            <ElAvatar :size="36" :src="m.from==='ai'? selectedRole.avatarUrl : '/favicon.ico'" />
            <div class="bubble">{{ m.content }}</div>
          </div>
        </div>
      </ElScrollbar>
    </main>

    <!-- 录音浮层提示 -->
    <div v-if="recording" class="record-hud" :class="{ cancel: isCancel }">
      <div class="sec">{{ recordSeconds }}s</div>
      <div class="hint">{{ isCancel ? '松开手指，取消发送' : '上滑取消' }}</div>
    </div>

    <!-- 语音预览 -->
    <div v-if="previewVisible" class="preview">
      <audio :src="previewUrl" controls autoplay></audio>
      <ElButton size="small" @click="previewVisible=false">关闭预览</ElButton>
    </div>

    <footer class="inputbar">
      <ElInput v-model="input" placeholder="输入消息..." @keyup.enter="sendText" />
      <ElButton type="primary" @click="sendText">发送</ElButton>
      <ElButton @mousedown.native="onVoicePress" @touchstart.stop.prevent="onVoicePress">按住语音</ElButton>
    </footer>
  </div>
</template>

<style scoped>
.page{ display:flex; flex-direction:column; height:100vh; }
.topbar{ display:flex; align-items:center; gap:12px; padding:8px 12px; border-bottom:1px solid var(--color-border); }
.role-name{ font-weight:600; }
.role-list{ display:flex; flex-direction:column; gap:12px; }
.role-item{ display:flex; align-items:center; gap:12px; padding:8px; border-radius:8px; cursor:pointer; }
.role-item:hover{ background: rgba(0,0,0,0.04); }
.role-item .meta .name{ font-weight:600; }
.role-item .meta .desc{ font-size:12px; opacity:0.7; }

.chat{ flex:1; }
.chat-scroll{ height:100%; }
.messages{ display:flex; flex-direction:column; gap:10px; padding:12px; }
.msg{ display:flex; align-items:flex-end; gap:8px; }
.msg.ai{ justify-content:flex-start; }
.msg.user{ justify-content:flex-end; flex-direction:row-reverse; }
.bubble{ max-width:68%; padding:10px 12px; border-radius:12px; background:#f2f3f5; }
.msg.user .bubble{ background:#409EFF; color:#fff; }

.inputbar{ display:flex; gap:8px; padding:8px 12px; border-top:1px solid var(--color-border); }

.record-hud{ position:fixed; left:50%; bottom:100px; transform:translateX(-50%); background:#333; color:#fff; padding:12px 16px; border-radius:12px; text-align:center; opacity:0.9; }
.record-hud.cancel{ background:#a94442; }
.record-hud .sec{ font-size:18px; font-weight:700; }
.record-hud .hint{ font-size:12px; opacity:0.9; }

.preview{ position:fixed; left:50%; bottom:24px; transform:translateX(-50%); background:#fff; border:1px solid var(--color-border); box-shadow:0 4px 12px rgba(0,0,0,0.1); padding:8px 12px; border-radius:12px; display:flex; align-items:center; gap:8px; }
</style>
