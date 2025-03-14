from fastapi import FastAPI
from enum import Enum

def build_poll(name, options):
    return {
        "name": name,
        "options": [{"option": option, "count": 0} for option in options]
    }

app=FastAPI( )

polls = [build_poll("najlepsze filmy", ["Wladca pierscieni", "Mis", "Dzien swira"])]

@app.get("/polls")
async def root():
    return {"polls" : polls}

@app.get("/polls/{poll_id}")
async def get_model(poll_id: int):
    return polls[poll_id]

from pydantic import BaseModel

class Item(BaseModel):
    name: str
    options: list

@app.post("/polls")
async def create_poll(item: Item):
    name = item.name
    options = item.options
    polls.append(build_poll(name, options))
    return len(polls) - 1

class Option(BaseModel):
    name: str

@app.post("/polls/{poll_id}")
async def create_poll(poll_id: int, option: Option):
    polls[poll_id]["options"].append({"name": option.name, "count": 0})
    return len(polls[poll_id]["options"])

@app.put("/polls/{poll_id}/vote/{option_id}")
async def create_item(poll_id: int, option_id: int):
    polls[poll_id]["options"][option_id]["count"] = polls[poll_id]["options"][option_id]["count"] + 1
    return polls[poll_id]["options"][option_id]["count"]

@app.put("/polls/{poll_id}/unvote/{option_id}")
async def create_item(poll_id: int, option_id: int):
    polls[poll_id]["options"][option_id]["count"] = max(0, polls[poll_id]["options"][option_id]["count"] - 1)
    return polls[poll_id]["options"][option_id]["count"]
