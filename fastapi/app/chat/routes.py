from fastapi import APIRouter
from app.models import ChatRequest

from app.roles.data import roles
from openai import OpenAI

# 百度千帆应用配置
client = OpenAI(
    api_key="bce-v3/ALTAK-um3v8vhXVtEcQDXEdp9LB/74a116123c87f6b43fa8b9d8af1a10659d37266a",  # 请替换为你的 token
    base_url="https://qianfan.baidubce.com/v2",
    default_headers={"appid": ""}
)

router = APIRouter()


def call_qianfan(role_prompt, user_message):
    response = client.chat.completions.create(
        model="ernie-4.0-turbo-8k",
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
    # 2. 调用百度千帆AI接口
    reply = call_qianfan(role_prompt, req.message)
    return {"reply": reply}
