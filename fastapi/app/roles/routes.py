from fastapi import APIRouter, Query
from app.roles.data import roles

router = APIRouter()

@router.get("/roles")
def get_roles():
    return {"roles": roles}

@router.get("/search_roles")
def search_roles(q: str = Query(..., description="角色关键词")):
    result = [r for r in roles if q in r["name"] or q in r["desc"]]
    return {"roles": result}
