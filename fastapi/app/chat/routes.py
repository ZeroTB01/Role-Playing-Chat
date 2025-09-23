from fastapi import APIRouter
from app.models import ChatRequest
from app.roles.data import roles
import openai

router = APIRouter()

def call_openai(role_prompt, user_message):
    response = openai.ChatCompletion.create(
        model="gpt-3.5-turbo",
        messages=[
            {"role": "system", "content": role_prompt},
            {"role": "user", "content": user_message}
        ]
    )
    return response.choices[0].message.content

@router.post("/chat")
def chat(req: ChatRequest):
    # 1. 获取角色设定
    role = next((r for r in roles if r["id"] == req.role_id), None)
    if not role:
        return {"reply": "角色未找到。"}
    role_prompt = f"你是{role['name']}，{role['desc']}"
    # 2. 调用大厂AI接口
    reply = call_openai(role_prompt, req.message)
    return {"reply": reply}
