#!/usr/bin/env python3
"""
qianyu-app-service API 模拟测试脚本
=====================================
模拟真实用户行为，调用本地服务端 API，生成完整的社交互动数据。

前置条件：
  1. 本地服务已启动（默认 http://localhost:8080）
  2. MySQL、Redis、Nacos 均已就绪

使用方法：
  python api_simulator_test.py

场景流程：
  用户登录
    -> 发布纯文本动态
    -> 发布图文动态（含经纬度）
    -> 发布视频动态
    -> 查看推荐 Feed
    -> 查看卡片 Feed
    -> 查看动态详情
    -> 点赞动态
    -> 查看点赞状态
    -> 取消点赞
    -> 查看取消后的点赞状态
    -> 发表评论
    -> 查看评论列表
    -> 回复评论
    -> 查看回复列表
    -> 点赞评论
    -> 取消点赞评论
    -> 个人中心
    -> 个人内容列表
"""

import io
import json
import os
import sys
import time
import threading
import urllib.request
import urllib.error
from http.server import HTTPServer, SimpleHTTPRequestHandler
from pathlib import Path

# 导入 ComfyUI 图片生成客户端
_COMFY_DIR = Path(r"D:\zxy\ws\ai\test\world-2026-06-15")
if _COMFY_DIR.exists():
    sys.path.insert(0, str(_COMFY_DIR))
    from comfy import ComfyClient, ComfyConfig
else:
    ComfyClient = None  # fallback: 无 ComfyUI 时使用 CDN URL

# GBK 终端中文化兼容
if sys.stdout.encoding and sys.stdout.encoding.upper() != "UTF-8":
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")

BASE_URL = "http://localhost:8080"
COMFY_URL = "http://127.0.0.1:8188"

# 测试账号（仅一个可用账号）
TEST_PHONE = "+86-13800138000"
TEST_CODE = "123456"

# ── 本地图片服务 ──
IMAGE_SERVER_PORT = 9090
IMAGE_DIR = Path(__file__).parent / "gen_images"


# ========== HTTP 帮助函数 ==========

def api_request(method, path, token=None, data=None):
    """发送 HTTP 请求并返回解析后的 JSON 内容。"""
    url = f"{BASE_URL}{path}"
    headers = {"Content-Type": "application/json"}
    if token:
        headers["token"] = token

    body = None
    if data is not None:
        body = json.dumps(data, ensure_ascii=False).encode("utf-8")

    req = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            raw = resp.read().decode("utf-8")
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8")
        print(f"  [WARN] HTTP {e.code} for {method} {path}")
    except urllib.error.URLError as e:
        print(f"\n  [ERR] 连接失败: {e.reason}")
        print(f"     请确认服务已启动: {BASE_URL}")
        sys.exit(1)

    try:
        parsed = json.loads(raw)
    except json.JSONDecodeError:
        print(f"  响应不是合法 JSON:\n{raw}")
        return None
    return parsed


def check_ok(resp, label):
    """检查响应状态，成功则返回 content，失败返回 None。"""
    if resp is None:
        print(f"  [X] {label}: 无响应")
        return None
    status = resp.get("status", -1)
    # API 返回的 status 是字符串 "0"，统一转 int 比较
    ok = str(status) == "0" or status == 0
    if ok:
        return resp.get("content")
    else:
        print(f"  [X] {label}: status={status}, state={resp.get('state')}, message={resp.get('message')}")
        return None


def step(seq, title):
    """打印步骤标题。"""
    print(f"\n{'=' * 60}")
    print(f"  步骤 {seq}: {title}")
    print(f"{'=' * 60}")


def section(title):
    """打印章节标题。"""
    print(f"\n  --- {title} ---")


# ========== ComfyUI 图片生成 ==========

def generate_images() -> dict[str, Path]:
    """
    用 ComfyUI Qwen 模型生成 3 张朋友圈风格图片。
    返回: { "scene_name": Path("...png") }
    """
    scenes = [
        ("cat",           "一只橘猫在窗台上晒太阳，温馨日常",         120230401),
        ("sunset",        "深圳湾公园的日落，海上夕阳，很美的风景",  120230402),
        ("coffee",        "一杯手冲咖啡放在木桌上，旁边有本书",      120230403),
    ]

    if ComfyClient is None:
        print("  [WARN] comfy.py 未找到，跳过图片生成")
        return {}

    cfg = ComfyConfig(
        output_dir=str(IMAGE_DIR),
        default_workflow="qwen_8steps",
    )
    client = ComfyClient(cfg)

    result = {}
    IMAGE_DIR.mkdir(parents=True, exist_ok=True)

    for name, prompt_text, seed in scenes:
        print(f"  生成: 「{prompt_text[:30]}」...", end=" ", flush=True)
        try:
            files = client.txt2img(
                prompt=prompt_text,
                negative="模糊，低质量，畸变，崩坏，水印",
                width=768,
                height=512,
                steps=8,
                cfg=1.0,
                seed=seed,
            )
            if files:
                result[name] = files[0]
                sz = files[0].stat().st_size / 1024
                print(f"[OK] {files[0].name} ({sz:.0f}KB)")
            else:
                print("[FAIL] 无输出")
        except Exception as e:
            print(f"[FAIL] {e}")

    print(f"  [SUM] 成功生成 {len(result)}/3 张图片")
    return result


# ========== 本地图片 HTTP 服务 ==========

_httpd: HTTPServer | None = None
_server_thread: threading.Thread | None = None


class _NoLogHandler(SimpleHTTPRequestHandler):
    """不输出日志的文件服务器（防刷屏）"""
    def log_message(self, fmt, *args):
        pass


def start_image_server(serve_dir: Path, port: int = IMAGE_SERVER_PORT) -> str:
    """
    在后台启动 HTTP 文件服务器，返回 base URL。
    图片通过 http://localhost:{port}/{filename} 访问。
    """
    global _httpd, _server_thread

    if _httpd:
        return f"http://localhost:{port}"

    os.chdir(str(serve_dir))
    _httpd = HTTPServer(("0.0.0.0", port), _NoLogHandler)
    _server_thread = threading.Thread(target=_httpd.serve_forever, daemon=True)
    _server_thread.start()
    print(f"  [OK] 图片服务器启动: http://localhost:{port}/  (目录: {serve_dir})")
    return f"http://localhost:{port}"


def stop_image_server():
    """关闭后台 HTTP 文件服务器。"""
    global _httpd, _server_thread
    if _httpd:
        _httpd.shutdown()
        _httpd = None
        _server_thread = None
        print("  [OK] 图片服务器已关闭")


# ========== 主流程 ==========

def main():
    print("=" * 60)
    print("  qianyu-app-service API 模拟测试")
    print(f"  目标服务: {BASE_URL}")
    print(f"  ComfyUI:  {COMFY_URL}")
    print(f"  测试账号: {TEST_PHONE}")
    print(f"  开始时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print("=" * 60)

    # ── 第 0 步: 生成 AI 图片 ──
    step(0, "ComfyUI 图片生成")
    print(f"  模型: Qwen-Image-2512 (8步闪电版)")
    print(f"  尺寸: 768x512")
    image_files = generate_images()

    # ── 启动本地图片服务器 ──
    if image_files:
        IMAGE_DIR.mkdir(parents=True, exist_ok=True)
        start_image_server(IMAGE_DIR)
        print(f"  图片 URL 前缀: http://localhost:{IMAGE_SERVER_PORT}/")
    else:
        print("  [WARN] 使用 CDN URL 作为备用")

    # 1. 登录
    step(1, "用户登录")
    token, user_info = login(TEST_PHONE, TEST_CODE)

    # 2. 发布动态
    step(2, "发布动态")
    moments = publish_moments(token, user_info["userId"], image_files)

    if not moments:
        print("  [WARN] 没有成功发布的动态，后续流程可能受影响")

    # 3. 推荐 Feed
    step(3, "推荐 Feed")
    browse_feed(token)

    # 4. 卡片 Feed
    step(4, "卡片 Feed（轻量版）")
    browse_cards(token)

    # 具体操作需要至少一条动态
    moment_id = moments[0]["momentId"] if moments else None

    if moment_id:
        # 5. 动态详情
        step(5, "查看动态详情")
        view_moment_detail(token, moment_id)

        # 6. 点赞
        step(6, "点赞动态")
        like_moment(token, moment_id)

        # 7. 查询点赞状态
        step(7, "查询点赞状态")
        check_like_status(token, moment_id, expected=True)

        # 8. 取消点赞
        step(8, "取消点赞")
        unlike_moment(token, moment_id)

        # 9. 查询取消后的状态
        step(9, "查询取消后的点赞状态")
        check_like_status(token, moment_id, expected=False)

        # 10. 发布评论
        step(10, "发布评论")
        comment = publish_comment(token, moment_id)

        # 11. 评论列表
        step(11, "查看评论列表")
        view_comment_list(token, moment_id)

        # 12. 回复评论
        step(12, "回复评论")
        if comment:
            reply_to_comment(token, moment_id, comment["commentId"])

        # 13. 回复列表
        step(13, "查看回复列表")
        if comment:
            view_reply_list(token, comment["commentId"])

        # 14. 点赞评论
        step(14, "点赞评论")
        if comment:
            like_comment(token, comment["commentId"])

        # 15. 取消点赞评论
        step(15, "取消点赞评论")
        if comment:
            unlike_comment(token, comment["commentId"])

        # 16. 再次点赞（恢复数据）
        step(16, "恢复点赞")
        like_moment(token, moment_id)

    # 17. 个人中心
    step(17, "个人中心")
    personal_center(token)

    # 18. 个人内容列表
    step(18, "个人内容列表")
    personal_contents(token)

    # 统计
    print(f"\n{'=' * 60}")
    print(f"  [OK] 全部模拟流程执行完成")
    print(f"  完成时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"  用户: userId={user_info['userId']}, nickname={user_info['nickname']}")
    print(f"  发布 {len(moments)} 条动态 (含 {len(image_files)} 张 ComfyUI 生成图)")
    print(f"{'=' * 60}")

    # 关闭图片服务器
    stop_image_server()


# ========== 登录 ==========

def login(phone, code):
    """手机验证码登录，返回 (token, userInfo)。"""
    payload = {
        "phone": phone,
        "code": code,
        "authMode": "CODE"
    }
    print(f"  登录: phone={phone}")
    resp = api_request("POST", "/api/user/login/phone", data=payload)
    content = check_ok(resp, "登录")
    if content:
        token = content["token"]
        user_info = content["userInfo"]
        print(f"  [OK] 登录成功: userId={user_info['userId']}, nickname={user_info['nickname']}")
        return token, user_info
    else:
        print(f"  [FAIL] 登录失败，请检查服务是否正常运行")
        sys.exit(1)


# ========== 发布动态 ==========

def publish_moments(token, author_id, image_files: dict[str, Path] | None = None):
    """发布不同类型的动态。"""
    published = []
    img_base = f"http://localhost:{IMAGE_SERVER_PORT}"

    # ── 2.1 纯文本 ──
    section("2.1 发布纯文本动态")
    text_moment = {
        "content": {
            "type": "text",
            "text": {
                "text": "Hello World! 测试一下 ComfyUI + 朋友圈 联动",
                "atIds": []
            }
        }
    }
    resp = api_request("POST", "/api/social/moment/publish", token=token, data=text_moment)
    content = check_ok(resp, "发布纯文本")
    if content:
        print(f"  [OK] 发布成功: momentId={content['momentId']}, type=text")
        published.append(content)
    else:
        print(f"  [WARN] 纯文本发布未成功")

    cat_w, cat_h = 768, 512

    # ── 2.2 图文（ComfyUI 猫图） ──
    section("2.2 发布图文动态（橘猫?）")
    cat_url = f"{img_base}/Qwen-8steps_00001_.png"
    if image_files and "cat" in image_files:
        cat_url = f"{img_base}/{image_files['cat'].name}"
    image_moment = {
        "content": {
            "type": "image",
            "text": {
                "text": "AI 生成的橘猫，暖洋洋的下午?",
                "atIds": []
            },
            "image": [
                {
                    "imageId": "img_ai_cat",
                    "imageUrl": cat_url,
                    "width": cat_w,
                    "height": cat_h
                }
            ]
        },
        "latitude": 22.5431,
        "longitude": 114.0579,
        "country": "CN"
    }
    resp = api_request("POST", "/api/social/moment/publish", token=token, data=image_moment)
    content = check_ok(resp, "发布图文")
    if content:
        print(f"  [OK] 发布成功: momentId={content['momentId']}, type=image")
        published.append(content)
    else:
        print(f"  [WARN] 图文发布未成功")

    # ── 2.3 图文（ComfyUI 日落） ──
    section("2.3 发布图文动态（日落?）")
    sunset_url = f"{img_base}/Qwen-8steps_00002_.png"
    if image_files and "sunset" in image_files:
        sunset_url = f"{img_base}/{image_files['sunset'].name}"
    sunset_moment = {
        "content": {
            "type": "image",
            "text": {
                "text": "深圳湾的日落，AI 画出来的也很美",
                "atIds": []
            },
            "image": [
                {
                    "imageId": "img_ai_sunset",
                    "imageUrl": sunset_url,
                    "width": cat_w,
                    "height": cat_h
                }
            ]
        },
        "latitude": 22.5431,
        "longitude": 114.0579,
        "country": "CN"
    }
    resp = api_request("POST", "/api/social/moment/publish", token=token, data=sunset_moment)
    content = check_ok(resp, "发布日落图")
    if content:
        print(f"  [OK] 发布成功: momentId={content['momentId']}, type=image")
        published.append(content)
    else:
        print(f"  [WARN] 日落图发布未成功")

    # ── 2.4 视频（CDN URL） ──
    section("2.4 发布视频动态")
    video_moment = {
        "content": {
            "type": "video",
            "text": {
                "text": "今天的日落，太美了 #视频",
                "atIds": []
            },
            "video": {
                "videoId": "v_001",
                "videoUrl": "https://cdn.clmcat.com/test/sunset.mp4",
                "coverUrl": "https://cdn.clmcat.com/test/sunset_cover.jpg",
                "width": 1920,
                "height": 1080,
                "duration": 15
            }
        },
        "latitude": 22.5431,
        "longitude": 114.0579
    }
    resp = api_request("POST", "/api/social/moment/publish", token=token, data=video_moment)
    content = check_ok(resp, "发布视频")
    if content:
        print(f"  [OK] 发布成功: momentId={content['momentId']}, type=video")
        published.append(content)
    else:
        print(f"  [WARN] 视频发布未成功")

    print(f"\n  [SUM] 共发布 {len(published)} 条动态（含 {len([p for p in published if p.get('content',{}).get('type')=='image'])} 张 AI 图）")
    return published


# ========== Feed ==========

def browse_feed(token):
    """查看推荐和关注 Feed。"""
    # 3.1 推荐 Feed
    section("3.1 推荐 Feed（完整版）")
    resp = api_request("GET", "/api/social/feed/recommend?cursor=0&limit=10", token=token)
    content = check_ok(resp, "推荐 Feed")
    if content:
        datas = content.get("datas", [])
        print(f"  返回 {len(datas)} 条, hasMore={content.get('hasMore')}")
        for m in datas:
            print(f"    mId={m['momentId']}, type={m['content']['type']}, "
                  f"lat={m.get('latitude')}, lng={m.get('longitude')}, "
                  f"likes={m.get('likes')}, comments={m.get('comments')}")

    # 3.2 关注 Feed
    section("3.2 关注 Feed")
    resp = api_request("GET", "/api/social/feed/following?cursor=0&limit=10", token=token)
    content = check_ok(resp, "关注 Feed")
    if content:
        datas = content.get("datas", [])
        print(f"  返回 {len(datas)} 条, hasMore={content.get('hasMore')}")


def browse_cards(token):
    """查看卡片摘要 Feed。"""
    section("4.1 推荐卡片摘要")
    resp = api_request("GET", "/api/social/feed/recommend/cards?cursor=0&limit=10", token=token)
    content = check_ok(resp, "推荐卡片")
    if content:
        datas = content.get("datas", [])
        print(f"  返回 {len(datas)} 张卡片, hasMore={content.get('hasMore')}")
        for c in datas:
            print(f"    mId={c['momentId']}, type={c.get('type')}, "
                  f"title={c.get('title','')[:20]}, likeCount={c.get('likeCount')}, "
                  f"commentCount={c.get('commentCount')}, hasLike={c.get('hasLike')}")


# ========== 动态详情 ==========

def view_moment_detail(token, moment_id):
    """查看动态详情。"""
    resp = api_request("GET", f"/api/social/moment/get?momentId={moment_id}", token=token)
    content = check_ok(resp, "查看详情")
    if content:
        print(f"  [OK] momentId={content['momentId']}, type={content['content']['type']}, "
              f"lat={content.get('latitude')}, lng={content.get('longitude')}, "
              f"likes={content.get('likes')}, comments={content.get('comments')}")


# ========== 点赞 / 取消 ==========

def like_moment(token, moment_id):
    """点赞动态。"""
    resp = api_request("POST", f"/api/social/like/moment?momentId={moment_id}", token=token)
    content = check_ok(resp, "点赞")
    if content is not None:
        print(f"  [OK] 点赞成功: momentId={moment_id}")
    else:
        print(f"  [WARN] 点赞可能已存在")


def unlike_moment(token, moment_id):
    """取消点赞动态。"""
    resp = api_request("POST", f"/api/social/like/moment/cancel?momentId={moment_id}", token=token)
    content = check_ok(resp, "取消点赞")
    if content is not None:
        print(f"  [OK] 取消点赞成功: momentId={moment_id}")


def check_like_status(token, moment_id, expected=True):
    """查询点赞状态并验证。"""
    resp = api_request("GET", f"/api/social/like/moment/status?momentId={moment_id}", token=token)
    content = check_ok(resp, "点赞状态")
    if content:
        liked = content["liked"]
        status_text = "已点赞" if liked else "未点赞"
        print(f"  [OK] momentId={content['targetId']} -> {status_text}")
        if liked == expected:
            print(f"  [OK] 状态符合预期")
        else:
            print(f"  [WARN] 状态与预期不符: 期望={expected}, 实际={liked}")


def like_comment(token, comment_id):
    """点赞评论。"""
    resp = api_request("POST", f"/api/social/like/comment?commentId={comment_id}", token=token)
    content = check_ok(resp, "点赞评论")
    if content is not None:
        print(f"  [OK] 点赞评论成功: commentId={comment_id}")


def unlike_comment(token, comment_id):
    """取消点赞评论。"""
    resp = api_request("POST", f"/api/social/like/comment/cancel?commentId={comment_id}", token=token)
    content = check_ok(resp, "取消点赞评论")
    if content is not None:
        print(f"  [OK] 取消点赞评论成功: commentId={comment_id}")


# ========== 评论 ==========

def publish_comment(token, moment_id):
    """发布一条评论。"""
    comment_data = {
        "momentId": moment_id,
        "parentCommentId": 0,
        "replyCommentId": 0,
        "content": {
            "text": {
                "text": "拍得真好看！这是在哪里拍的？",
                "atIds": []
            }
        }
    }
    resp = api_request("POST", "/api/social/comment/publish", token=token, data=comment_data)
    content = check_ok(resp, "发布评论")
    if content:
        print(f"  [OK] 评论成功: commentId={content['commentId']}, "
              f"text='{content['content']['text']['text']}'")
        return content
    return None


def reply_to_comment(token, moment_id, parent_comment_id):
    """回复一条评论。"""
    reply_data = {
        "momentId": moment_id,
        "parentCommentId": parent_comment_id,
        "replyCommentId": parent_comment_id,
        "content": {
            "text": {
                "text": "谢谢！在深圳拍的",
                "atIds": []
            }
        }
    }
    resp = api_request("POST", "/api/social/comment/publish", token=token, data=reply_data)
    content = check_ok(resp, "回复评论")
    if content:
        print(f"  [OK] 回复成功: commentId={content['commentId']}, "
              f"level={content.get('commentLevel')}, "
              f"text='{content['content']['text']['text']}'")


def view_comment_list(token, moment_id):
    """查看动态的评论列表。"""
    resp = api_request("GET",
        f"/api/social/comment/moment/list?momentId={moment_id}&nextCommentId=0&limit=20",
        token=token)
    content = check_ok(resp, "评论列表")
    if content:
        comments = content.get("commentList", [])
        print(f"  动态 {content['momentId']} 共有 {len(comments)} 条评论")
        for c in comments:
            print(f"    cId={c['commentId']}, level={c.get('commentLevel')}, "
                  f"likes={c.get('likes')}, replies={c.get('replies')}, "
                  f"text='{c['content']['text']['text']}'")


def view_reply_list(token, parent_comment_id):
    """查看一级评论下的回复列表。"""
    resp = api_request("GET",
        f"/api/social/comment/reply/list?parentCommentId={parent_comment_id}&nextCommentId=0&limit=20",
        token=token)
    content = check_ok(resp, "回复列表")
    if content:
        comments = content.get("commentList", [])
        print(f"  评论 {content['parentCommentId']} 下有 {len(comments)} 条回复")
        for c in comments:
            print(f"    cId={c['commentId']}, replyUserId={c.get('replyUserId')}, "
                  f"text='{c['content']['text']['text']}'")


# ========== 个人中心 ==========

def personal_center(token):
    """获取个人中心数据。"""
    resp = api_request("GET", "/api/app/personal/center", token=token)
    content = check_ok(resp, "个人中心")
    if content:
        profile = content["userProfile"]
        stats = content["userStats"]
        shortcuts = content.get("shortcuts", [])
        print(f"  昵称: {profile['nickname']}")
        print(f"  编号: {profile['userNo']}")
        print(f"  获赞: {stats['likeCount']}, 关注: {stats['followCount']}, 粉丝: {stats['fansCount']}")
        print(f"  快捷入口: {', '.join(s['name'] for s in shortcuts if s.get('visible'))}")


def personal_contents(token):
    """查看个人发布的内容列表。"""
    for tab in ["moment", "work", "like"]:
        section(f"7.{['','','',''][['moment','work','like'].index(tab)] if tab in ['moment','work','like'] else ''} 内容列表: {tab}")
        resp = api_request("GET",
            f"/api/app/personal/center/contents?tab={tab}&cursor=0&limit=5",
            token=token)
        content = check_ok(resp, f"内容列表({tab})")
        if content:
            items = content.get("items", [])
            print(f"  返回 {len(items)} 条, hasMore={content.get('hasMore')}")
            for i in items:
                print(f"    mId={i['momentId']}, type={i.get('type')}, "
                      f"likeCount={i.get('likeCount')}, commentCount={i.get('commentCount')}")


if __name__ == "__main__":
    main()
