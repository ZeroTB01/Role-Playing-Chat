
from fastapi import FastAPI
from app.roles.routes import router as roles_router
from app.chat.routes import router as chat_router
from app.chat.voice_chat import router as voice_chat_router

app = FastAPI()

# 注册各功能模块路由
app.include_router(roles_router)
app.include_router(chat_router)
app.include_router(voice_chat_router)


if __name__ == "__main__":
	import uvicorn
	uvicorn.run("app.main:app", host="127.0.0.1", port=8000, reload=True)
