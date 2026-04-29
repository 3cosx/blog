# 博客系统设计

## 1. 项目概述

- **类型**: 个人博客系统
- **功能**: 个人主页展示 + 文章发布
- **访问范围**: 公开访问，点赞/评论需登录

## 2. 表结构设计

### 2.1 users（已有）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| nick_name | varchar | 昵称 |
| email | varchar | 邮箱 |
| telephone | varchar | 手机号 |
| profile_photo_url | varchar | 头像URL |
| state | varchar | 状态(NORMAL/FROZEN) |
| user_role | varchar | 角色(CUSTOM/ADMIN) |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 2.2 user_profile（新增）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| user_id | bigint | 关联users表 |
| bio | varchar(500) | 个人简介 |
| skills | text | 技能标签（JSON数组格式） |
| projects | text | 项目经历（JSON数组格式） |
| education | text | 教育背景（JSON数组格式） |
| social_links | text | 社交链接（JSON：github, weibo, email等） |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 2.3 article（新增）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| author_id | bigint | 关联users表 |
| title | varchar(200) | 标题 |
| content | text | 文章内容 |
| cover_image_url | varchar(500) | 封面图URL |
| view_count | int | 浏览量 |
| like_count | int | 点赞数 |
| comment_count | int | 评论数 |
| status | tinyint | 0=草稿, 1=已发布 |
| create_time | datetime | 创建时间 |
| update_time | datetime | 更新时间 |

### 2.4 article_image（新增）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| article_id | bigint | 关联article表 |
| image_url | varchar(500) | 图片地址 |
| create_time | datetime | 上传时间 |

### 2.5 comment（新增）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| article_id | bigint | 关联article表 |
| user_id | bigint | 评论者 |
| content | varchar(1000) | 评论内容 |
| parent_id | bigint | 父评论ID（空=顶层评论） |
| create_time | datetime | 评论时间 |

### 2.6 article_like（新增）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| article_id | bigint | 关联article表 |
| user_id | bigint | 点赞用户 |
| create_time | datetime | 点赞时间 |

### 2.7 article_collect（新增）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | bigint | 主键 |
| article_id | bigint | 关联article表 |
| user_id | bigint | 收藏用户 |
| create_time | datetime | 收藏时间 |

## 3. 模块划分

| 模块 | 职责 |
|------|------|
| blog-user | 用户基础信息 + 扩展信息（user_profile） |
| blog-article（新建） | 文章、评论、点赞、收藏、图片上传 |

## 4. API 设计

### 4.1 公开接口（无需登录）
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /article/list | 文章列表（分页） |
| GET | /article/{id} | 文章详情 |
| GET | /article/{id}/comments | 评论列表 |
| GET | /user/{id}/profile | 用户个人主页信息 |

### 4.2 需登录接口
| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /article/{id}/like | 点赞 |
| POST | /article/{id}/collect | 收藏 |
| POST | /article/{id}/comment | 发表评论 |
| POST | /article/create | 创建文章 |
| POST | /article/{id}/update | 更新文章 |
| POST | /article/{id}/delete | 删除文章 |
| POST | /article/image/upload | 图片上传 |
| POST | /user/profile/update | 更新个人主页信息 |

## 5. 实施计划

分两个阶段：
1. 用户模块扩展（user_profile）
2. 文章模块（新建 blog-article）

## 6. 技术说明

- 图片上传：服务器中转，上传到云存储（OSS/COS）
- 评论支持二级回复（parent_id）
- 点赞/收藏去重（同一用户对同一文章）