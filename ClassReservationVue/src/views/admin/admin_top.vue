<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>管理者トップページ</title>
    <link rel="stylesheet" th:href="@{/css/style.css}">
    <style>
        body { font-family: Arial, sans-serif; margin: 30px; }
        h1 { color: #333; }
        .menu { display: flex; flex-direction: column; gap: 15px; margin-top: 20px; }
        .menu a {
            display: inline-block;
            padding: 10px 20px;
            background-color: #007BFF;
            color: white;
            text-decoration: none;
            border-radius: 8px;
            width: fit-content;
        }
        .menu a:hover {
            background-color: #0056b3;
        }
    </style>
</head>
<body>

    <h1>管理者トップページ</h1>

    <div class="menu">
        <a th:href="@{/admin/register/create}">アカウント作成</a>
        <a th:href="@{/admin/accountlist}">アカウント一覧</a>
        <a th:href="@{/admin/charge}">授業時間チャージ</a>
        <a th:href="@{/notice}">お知らせ管理</a>
    </div>

</body>
</html>
