import requests

def test_qiniu_asr():
    url = "https://asr.qiniuapi.com/v1/recognize"
    headers = {
        "Authorization": "QiniuStub your_access_token",  # 替换为你的真实鉴权
        "Content-Type": "application/octet-stream"
    }
    with open("test.wav", "rb") as f:
        audio_data = f.read()
    response = requests.post(url, headers=headers, data=audio_data)
    print("七牛云语音识别结果:", response.json())

if __name__ == "__main__":
    test_qiniu_asr()
